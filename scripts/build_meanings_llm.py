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
    python3 scripts/build_meanings_llm.py --gate     # 품질 게이트: 교육부 뜻 100개와 대조
    python3 scripts/build_meanings_llm.py            # 전체 생성 (중단 시 재실행하면 이어서 진행)
    python3 scripts/build_meanings_llm.py --apply    # 저장된 결과만 재적용 (생성 없음)

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
    """비교용 한국어 토큰 추출: 2글자 이상 한글 연속체 + 어간 근사(마지막 글자 제거)."""
    tokens = set()
    for t in re.findall(r"[가-힣]{2,}", text):
        tokens.add(t)
        if len(t) >= 3:
            tokens.add(t[:-1])  # "회복하다" ↔ "회복" 매칭용
    return tokens


def meanings_overlap(edu_meaning: str, senses: list[dict]) -> bool:
    """교육부 뜻과 생성 뜻의 토큰이 하나라도 겹치면 일치로 판정 (보수적 하한)."""
    edu_tokens = _korean_tokens(edu_meaning)
    gen_tokens = _korean_tokens(" ".join(s["ko"] for s in senses))
    return any(
        e == g or e in g or g in e
        for e in edu_tokens for g in gen_tokens
    )


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


def main():
    conn = sqlite3.connect(DB_PATH)
    try:
        if "--gate" in sys.argv:
            run_gate(conn)
            return
        if "--apply" not in sys.argv:
            run_generation(conn)
        apply_results(conn)
        verify(conn)
    finally:
        conn.close()


if __name__ == "__main__":
    main()
