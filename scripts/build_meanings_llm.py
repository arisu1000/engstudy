#!/usr/bin/env python3
"""
단어 대표 뜻 LLM 재생성 스크립트 (로컬 Ollama + gemma4).

kengdic.tsv가 가나다순 정렬이라 교육부 뜻으로 보정되지 않은 단어
(~9,200개)는 여전히 희귀 의미가 대표 뜻으로 노출된다. 이 스크립트는
로컬 Ollama(gemma4)로 "가장 흔한 한국어 뜻 1~3개 + 품사"를 생성해
words.meaning과 word_meanings를 갱신한다.

대상: edu_words와 겹치지 않는 단어 (겹치는 단어는 apply_edu_meanings.py가 처리)
제외: build_word_db.py의 FUNCTION_WORDS (수동 큐레이션 뜻 보유)

빌드 파이프라인 순서:
    build_word_db.py → build_meanings.py → apply_edu_meanings.py → build_meanings_llm.py

사용법:
    ollama serve  # (실행 중이 아니면)
    python3 scripts/build_meanings_llm.py --gate       # 품질 게이트: 교육부 뜻 100개와 대조
    python3 scripts/build_meanings_llm.py              # 전체 생성 (중단 시 재실행하면 이어서 진행)
    python3 scripts/build_meanings_llm.py --apply      # 저장된 결과만 재적용 (생성 없음)
    python3 scripts/build_meanings_llm.py --edu-merge  # 교육부 겹침 단어 교차 검증·병합
                                                       #   (--apply와 함께 쓰면 재적용만)
    python3 scripts/build_meanings_llm.py --selftest   # meanings_overlap 회귀 테스트

--edu-merge: 교육부 뜻이 다의어의 덜 흔한 뜻인 경우(예: just → "올바른")를 보정한다.
LLM 생성 뜻과 교육부 뜻이 겹치면 교육부 뜻을 대표로 유지(검수 데이터 신뢰),
겹치지 않으면 LLM 뜻을 대표로 올리고 교육부 뜻은 word_meanings 뒤 순서에 보존한다.

생성 결과는 meanings_llm_results.jsonl에 즉시 기록되므로 중단해도 안전하다.
소요 시간: 9,240단어 × ~4초 ≈ 10~12시간 (M4 Pro 기준)
"""

import ast
import json
import os
import random
import re
import sqlite3
import sys
import time
import urllib.request

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "databases", "engstudy.db")
RESULTS_PATH = os.path.join(SCRIPT_DIR, "meanings_llm_results.jsonl")
EDU_MERGE_RESULTS_PATH = os.path.join(SCRIPT_DIR, "edu_merge_results.jsonl")

OLLAMA_URL = "http://localhost:11434/api/generate"
MODEL = "gemma4:latest"
MAX_SENSES = 5  # word_meanings 상한 (build_meanings.py와 동일)
VALID_POS = {"noun", "verb", "adjective", "adverb", "preposition", "conjunction",
             "pronoun", "interjection", "article", "determiner"}
GATE_SAMPLE_SIZE = 100
GATE_SEED = 42  # 게이트 샘플 재현성

# 숫자·약어 등 모델이 한글 없는 응답을 내기 쉬운 단어의 수동 뜻 (소문자 키)
MANUAL_SENSES = {
    "tv": [{"ko": "텔레비전, 티비", "pos": "noun"}],
    "television": [{"ko": "텔레비전", "pos": "noun"}],
    "ai": [{"ko": "인공지능(AI)", "pos": "noun"}],
    "forty": [{"ko": "40, 마흔", "pos": "noun"}],
    "circus": [{"ko": "서커스, 곡예단", "pos": "noun"}],
    "sixty": [{"ko": "60, 예순", "pos": "noun"}],
    "pi": [{"ko": "원주율(π), 파이", "pos": "noun"}],
    "seventeen": [{"ko": "17, 열일곱", "pos": "noun"}],
    "lp": [{"ko": "LP판, 레코드판", "pos": "noun"}],
    "eighteen": [{"ko": "18, 열여덟", "pos": "noun"}],
    "eighty": [{"ko": "80, 여든", "pos": "noun"}],
    "nineteen": [{"ko": "19, 열아홉", "pos": "noun"}],
    "ppp": [{"ko": "구매력 평가(PPP)", "pos": "noun"}],
    "sept": [{"ko": "씨족, 종족", "pos": "noun"}],
    "ss": [{"ko": "스크린샷 (속어)", "pos": "noun"}],
}

# --edu-merge에서 교육부 뜻을 강제 유지하는 단어 (소문자 키).
# 교육부 뜻이 가장 흔한 용법인데 LLM 뜻과 동의어라 토큰이 겹치지 않거나
# (well: "잘" vs "좋은"), LLM 첫 뜻이 덜 흔한 의미인 경우
# (pat: "쓰다", even: "짝수", table: "책상"). 전체 교체 목록 수동 검토로 선정.
EDU_MERGE_KEEP = {"well", "even", "table", "pat"}

PROMPT_TEMPLATE = """\
English word: "{word}"

Give the 1-3 MOST COMMON Korean meanings of this word for a Korean learner's vocabulary app, ordered from most common to less common. Use concise dictionary-style Korean (e.g. "흰, 하얀" for white, "물" for water). Do not repeat the same meaning. Include the part of speech for each.

Respond with only valid JSON (no markdown):
{{"senses": [{{"ko": "뜻", "pos": "noun"}}]}}

pos must be one of: noun, verb, adjective, adverb, preposition, conjunction, pronoun, interjection, determiner"""


def load_function_words() -> set[str]:
    """build_word_db.py의 FUNCTION_WORDS 키를 AST로 추출 (의존성 없이 임포트 회피)."""
    path = os.path.join(SCRIPT_DIR, "build_word_db.py")
    tree = ast.parse(open(path, encoding="utf-8").read())
    for node in ast.walk(tree):
        if isinstance(node, ast.Assign) and any(
            isinstance(t, ast.Name) and t.id == "FUNCTION_WORDS" for t in node.targets
        ):
            return set(ast.literal_eval(node.value).keys())
    raise RuntimeError("build_word_db.py에서 FUNCTION_WORDS를 찾지 못함")


def get_target_words(conn) -> list[dict]:
    """LLM 재생성 대상: 교육부 뜻 미적용 + 기능어 제외."""
    function_words = load_function_words()
    rows = conn.execute(
        "SELECT id, word FROM words "
        "WHERE lower(word) NOT IN (SELECT lower(word) FROM edu_words) "
        "ORDER BY id"
    ).fetchall()
    return [{"id": r[0], "word": r[1]} for r in rows if r[1].lower() not in function_words]


def generate(word: str, temperature: float = 0.2) -> str:
    body = json.dumps({
        "model": MODEL,
        "prompt": PROMPT_TEMPLATE.format(word=word),
        "stream": False,
        "format": "json",
        "options": {"temperature": temperature, "num_predict": 200},
    }).encode()
    req = urllib.request.Request(OLLAMA_URL, body, {"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, timeout=300))["response"]


def parse_senses(text: str) -> list[dict] | None:
    """모델 응답에서 유효한 senses 목록 추출. 유효하지 않으면 None."""
    try:
        data = json.loads(text.strip())
    except json.JSONDecodeError:
        return None
    senses = data.get("senses") if isinstance(data, dict) else None
    if not isinstance(senses, list) or not senses:
        return None
    cleaned, seen = [], set()
    for s in senses[:3]:
        ko = s.get("ko", "").strip() if isinstance(s, dict) else ""
        pos = s.get("pos", "").strip().lower() if isinstance(s, dict) else ""
        # 한국어가 실제로 포함됐는지 확인 (영어만 온 응답 거부)
        if not ko or not any("가" <= ch <= "힣" for ch in ko):
            return None
        if ko in seen:
            continue
        seen.add(ko)
        cleaned.append({"ko": ko, "pos": pos if pos in VALID_POS else "noun"})
    return cleaned or None


def generate_senses(word: str) -> list[dict] | None:
    """생성 + 파싱. 무효 응답이면 온도를 올려 1회 재시도."""
    senses = parse_senses(generate(word))
    if senses is None:
        senses = parse_senses(generate(word, temperature=0.5))
    return senses


# ---------------------------------------------------------------------------
# 품질 게이트: 교육부 검수 뜻과 대조
# ---------------------------------------------------------------------------

def _korean_tokens(text: str) -> set[str]:
    """비교용 한국어 토큰 추출: 한글 연속체 + 어간 근사(마지막 글자 제거)."""
    tokens = set()
    for t in re.findall(r"[가-힣]+", text):
        tokens.add(t)
        if len(t) >= 3:
            tokens.add(t[:-1])  # "회복하다" ↔ "회복" 매칭용
    return tokens


def _tokens_match(a: str, b: str) -> bool:
    """토큰 일치 판정. 1글자 토큰("흰", "물")은 부분 문자열 오탐이 커서
    정확 일치·접두 일치("흰"↔"흰색의")만 인정한다."""
    if len(a) == 1 or len(b) == 1:
        return a == b or a.startswith(b) or b.startswith(a)
    return a == b or a in b or b in a


def meanings_overlap(edu_meaning: str, senses: list[dict]) -> bool:
    """교육부 뜻과 생성 뜻의 토큰이 하나라도 겹치면 일치로 판정 (보수적 하한)."""
    edu_tokens = _korean_tokens(edu_meaning)
    gen_tokens = _korean_tokens(" ".join(s["ko"] for s in senses))
    return any(_tokens_match(e, g) for e in edu_tokens for g in gen_tokens)


def run_gate(conn) -> None:
    """교육부 뜻이 있는 단어 샘플로 생성 품질 측정. 불일치 목록 출력."""
    rows = conn.execute(
        "SELECT w.word, e.meaning FROM words w JOIN edu_words e ON lower(w.word) = lower(e.word) "
        "WHERE trim(e.meaning) != '' ORDER BY w.id"
    ).fetchall()
    random.seed(GATE_SEED)
    sample = random.sample(rows, min(GATE_SAMPLE_SIZE, len(rows)))

    matches, mismatches, invalid = 0, [], 0
    t0 = time.time()
    for i, (word, edu_meaning) in enumerate(sample, 1):
        senses = generate_senses(word)
        if senses is None:
            invalid += 1
            mismatches.append((word, edu_meaning, "<무효 응답>"))
            continue
        gen_text = ", ".join(s["ko"] for s in senses)
        if meanings_overlap(edu_meaning, senses):
            matches += 1
        else:
            mismatches.append((word, edu_meaning, gen_text))
        if i % 20 == 0:
            print(f"  진행: {i}/{len(sample)} ({time.time()-t0:.0f}초)")

    rate = matches / len(sample) * 100
    print(f"\n토큰 일치율: {matches}/{len(sample)} = {rate:.0f}% (무효 응답 {invalid}개)")
    print("주의: 문자열 기반 하한값 — 동의어(예: 두목↔우두머리)는 불일치로 집계되므로 아래 목록을 눈으로 검토할 것\n")
    if mismatches:
        print(f"{'단어':<16} {'교육부':<24} 생성")
        for word, edu, gen in mismatches:
            print(f"{word:<16} {edu:<24} {gen}")


# ---------------------------------------------------------------------------
# 전체 생성 / 적용
# ---------------------------------------------------------------------------

def load_done_ids() -> set[int]:
    if not os.path.exists(RESULTS_PATH):
        return set()
    with open(RESULTS_PATH, encoding="utf-8") as f:
        return {json.loads(line)["word_id"] for line in f if line.strip()}


def run_generation(conn) -> None:
    words = get_target_words(conn)
    done = load_done_ids()
    todo = [w for w in words if w["id"] not in done]
    print(f"대상 {len(words):,}개 중 완료 {len(done):,}개 → 남은 작업 {len(todo):,}개 (모델: {MODEL})")

    failed = 0
    t0 = time.time()
    with open(RESULTS_PATH, "a", encoding="utf-8") as f:
        for i, w in enumerate(todo, 1):
            senses = generate_senses(w["word"])
            if senses is None:
                failed += 1
            else:
                f.write(json.dumps({"word_id": w["id"], "senses": senses}, ensure_ascii=False) + "\n")
                f.flush()
            if i % 100 == 0:
                elapsed = time.time() - t0
                remaining = elapsed / i * (len(todo) - i)
                print(f"  진행: {i:,}/{len(todo):,} | 실패 {failed} | "
                      f"경과 {elapsed/60:.0f}분 | 남은 예상 {remaining/3600:.1f}시간")
    print(f"생성 완료: 성공 {len(todo)-failed:,} / 실패 {failed}")


def load_entries(conn) -> list[dict]:
    """jsonl 결과 + MANUAL_SENSES(생성 결과 없는 단어만) 병합."""
    entries = []
    if os.path.exists(RESULTS_PATH):
        with open(RESULTS_PATH, encoding="utf-8") as f:
            entries = [json.loads(line) for line in f if line.strip()]
    done = {e["word_id"] for e in entries}
    for word, senses in MANUAL_SENSES.items():
        row = conn.execute("SELECT id FROM words WHERE lower(word) = ?", (word,)).fetchone()
        if row and row[0] not in done:
            entries.append({"word_id": row[0], "senses": senses})
    return entries


def apply_results(conn) -> None:
    """저장된 결과를 words.meaning + word_meanings에 적용 (멱등)."""
    updated = 0
    for entry in load_entries(conn):
        word_id, senses = entry["word_id"], entry["senses"]

        meanings = [s["ko"] for s in senses]
        conn.execute(
            "UPDATE words SET meaning = ?, meaning_type = 'ko', part_of_speech = ? "
            "WHERE id = ?",
            (", ".join(meanings), senses[0]["pos"], word_id),
        )

        # LLM 의미를 앞에, 겹치지 않는 기존 kengdic 의미를 뒤에 유지
        existing = conn.execute(
            "SELECT meaning, pos, meaning_type, source FROM word_meanings "
            "WHERE word_id = ? AND source != 'llm' ORDER BY sense_order",
            (word_id,),
        ).fetchall()
        new_senses = [(s["ko"], s["pos"], "ko", "llm") for s in senses]
        new_senses += [e for e in existing if e[0] not in meanings]

        conn.execute("DELETE FROM word_meanings WHERE word_id = ?", (word_id,))
        for order, (meaning, pos, meaning_type, source) in enumerate(new_senses[:MAX_SENSES]):
            conn.execute(
                "INSERT INTO word_meanings (word_id, meaning, pos, meaning_type, sense_order, source) "
                "VALUES (?, ?, ?, ?, ?, ?)",
                (word_id, meaning, pos, meaning_type, order, source),
            )
        updated += 1
    conn.commit()
    conn.execute("VACUUM")
    print(f"적용 완료: {updated:,}개 단어")


def verify(conn) -> None:
    """무결성 검증: sense_order 연속성과 상한, 한국어 뜻 잔여 현황."""
    no_korean = conn.execute(
        "SELECT COUNT(*) FROM words WHERE meaning_type != 'ko'"
    ).fetchone()[0]
    bad_order = conn.execute(
        "SELECT COUNT(*) FROM (SELECT word_id, MIN(sense_order) mn, MAX(sense_order) mx, COUNT(*) c "
        "FROM word_meanings GROUP BY word_id HAVING mn != 0 OR mx != c - 1 OR c > ?)",
        (MAX_SENSES,),
    ).fetchone()[0]
    assert bad_order == 0, f"sense_order 무결성 위반 {bad_order}개 단어"
    print(f"검증 통과 (meaning_type != 'ko' 잔여: {no_korean}개)")


# ---------------------------------------------------------------------------
# 교육부 겹침 단어 교차 검증·병합 (--edu-merge)
# ---------------------------------------------------------------------------

def get_edu_targets(conn) -> list[dict]:
    """교육부 뜻이 적용된 단어 (기능어 제외)."""
    function_words = load_function_words()
    rows = conn.execute(
        "SELECT w.id, w.word, trim(e.meaning) FROM words w "
        "JOIN edu_words e ON lower(w.word) = lower(e.word) "
        "WHERE trim(e.meaning) != '' ORDER BY w.id"
    ).fetchall()
    return [
        {"id": r[0], "word": r[1], "edu_meaning": r[2]}
        for r in rows if r[1].lower() not in function_words
    ]


def run_edu_merge_generation(conn) -> None:
    targets = get_edu_targets(conn)
    done = set()
    if os.path.exists(EDU_MERGE_RESULTS_PATH):
        with open(EDU_MERGE_RESULTS_PATH, encoding="utf-8") as f:
            done = {json.loads(line)["word_id"] for line in f if line.strip()}
    todo = [t for t in targets if t["id"] not in done]
    print(f"교육부 겹침 대상 {len(targets):,}개 중 완료 {len(done):,}개 → 남은 작업 {len(todo):,}개")

    failed = 0
    t0 = time.time()
    with open(EDU_MERGE_RESULTS_PATH, "a", encoding="utf-8") as f:
        for i, t in enumerate(todo, 1):
            senses = generate_senses(t["word"])
            if senses is None:
                failed += 1
            else:
                f.write(json.dumps(
                    {"word_id": t["id"], "senses": senses, "edu_meaning": t["edu_meaning"]},
                    ensure_ascii=False) + "\n")
                f.flush()
            if i % 100 == 0:
                elapsed = time.time() - t0
                remaining = elapsed / i * (len(todo) - i)
                print(f"  진행: {i:,}/{len(todo):,} | 실패 {failed} | "
                      f"경과 {elapsed/60:.0f}분 | 남은 예상 {remaining/60:.0f}분")
    print(f"생성 완료: 성공 {len(todo)-failed:,} / 실패 {failed}")


def apply_edu_merge(conn) -> None:
    """교차 검증 병합 적용 (멱등).

    - 일치(교육부 뜻이 LLM 흔한 뜻과 겹침): 교육부 뜻을 대표로 유지, LLM 뜻은 보조 의미로 추가
    - 불일치(교육부 뜻이 덜 흔한 뜻으로 판단): LLM 뜻을 대표로, 교육부 뜻은 뒤 순서에 보존
    """
    kept, flipped = 0, []
    with open(EDU_MERGE_RESULTS_PATH, encoding="utf-8") as f:
        for line in f:
            if not line.strip():
                continue
            entry = json.loads(line)
            word_id, senses, edu = entry["word_id"], entry["senses"], entry["edu_meaning"]
            llm_meanings = [s["ko"] for s in senses]

            existing_kengdic = conn.execute(
                "SELECT meaning, pos, meaning_type, source FROM word_meanings "
                "WHERE word_id = ? AND source = 'kengdic' ORDER BY sense_order",
                (word_id,),
            ).fetchall()
            edu_pos = conn.execute(
                "SELECT pos FROM word_meanings WHERE word_id = ? AND source = 'edu'", (word_id,)
            ).fetchone()
            edu_sense = (edu, edu_pos[0] if edu_pos else senses[0]["pos"], "ko", "edu")
            llm_senses = [(s["ko"], s["pos"], "ko", "llm") for s in senses if s["ko"] != edu]
            word = conn.execute("SELECT word FROM words WHERE id = ?", (word_id,)).fetchone()[0]

            if word.lower() in EDU_MERGE_KEEP or meanings_overlap(edu, senses):
                # 교육부 뜻이 흔한 뜻과 일치 → 대표 유지, LLM 뜻은 보조로
                new_senses = [edu_sense] + llm_senses
                conn.execute(
                    "UPDATE words SET meaning = ? WHERE id = ?", (edu, word_id)
                )
                kept += 1
            else:
                # 교육부 뜻이 덜 흔한 뜻 → LLM 뜻을 대표로 승격
                new_senses = llm_senses + [edu_sense]
                conn.execute(
                    "UPDATE words SET meaning = ?, part_of_speech = ? WHERE id = ?",
                    (", ".join(llm_meanings), senses[0]["pos"], word_id),
                )
                flipped.append((word, edu, ", ".join(llm_meanings)))

            seen = {s[0] for s in new_senses}
            new_senses += [e for e in existing_kengdic if e[0] not in seen]
            conn.execute("DELETE FROM word_meanings WHERE word_id = ?", (word_id,))
            for order, (meaning, pos, meaning_type, source) in enumerate(new_senses[:MAX_SENSES]):
                conn.execute(
                    "INSERT INTO word_meanings (word_id, meaning, pos, meaning_type, sense_order, source) "
                    "VALUES (?, ?, ?, ?, ?, ?)",
                    (word_id, meaning, pos, meaning_type, order, source),
                )
    conn.commit()
    conn.execute("VACUUM")
    print(f"병합 완료: 교육부 뜻 유지 {kept:,}개 / LLM 뜻으로 교체 {len(flipped):,}개")
    if flipped:
        print(f"\n{'단어':<16} {'교육부(뒤로 이동)':<24} 새 대표 뜻")
        for word, edu, llm in flipped:
            print(f"{word:<16} {edu:<24} {llm}")


def run_selftest() -> None:
    """meanings_overlap 회귀 테스트 (DB·LLM 불필요)."""
    cases = [
        # (기대값, 교육부 뜻, LLM 생성 뜻) — 실제 병합에서 문제가 됐던 케이스들
        (False, "올바른", ["그저, 단지", "막, 방금"]),        # just: 덜 흔한 뜻 → 교체
        (False, "시내", ["태우다", "화상"]),                  # burn: 희귀 뜻 → 교체
        (True, "변화", ["변화", "바꾸다"]),                   # change: 일치 → 유지
        (True, "쓴", ["쓴", "쓰다", "씁쓸한"]),               # 1글자 정확 일치
        (True, "물", ["물", "수분"]),                        # 1글자 정확 일치
        (True, "흰", ["흰색의", "하얀"]),                     # 1글자 접두 일치
        (False, "일", ["사건", "연애"]),                      # 1글자 부분 문자열 오탐 방지
        (True, "회복하다", ["회복", "복구하다"]),              # 어간 근사 매칭
    ]
    for expected, edu, kos in cases:
        got = meanings_overlap(edu, [{"ko": ko} for ko in kos])
        assert got == expected, f"overlap({edu!r}, {kos}) = {got}, 기대 {expected}"
    print(f"selftest 통과: {len(cases)}개 케이스")


def main():
    if "--selftest" in sys.argv:
        run_selftest()
        return
    conn = sqlite3.connect(DB_PATH)
    try:
        if "--gate" in sys.argv:
            run_gate(conn)
            return
        if "--edu-merge" in sys.argv:
            if "--apply" not in sys.argv:
                run_edu_merge_generation(conn)
            apply_edu_merge(conn)
            verify(conn)
            return
        if "--apply" not in sys.argv:
            run_generation(conn)
        apply_results(conn)
        verify(conn)
    finally:
        conn.close()


if __name__ == "__main__":
    main()
