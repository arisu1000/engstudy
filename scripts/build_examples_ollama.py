#!/usr/bin/env python3
"""
word_examples 보완 스크립트 (Phase 2 대안: 로컬 Ollama + gemma4).

build_examples.py(Tatoeba) 실행 후 word_examples가 없는 단어에 대해
로컬 LLM으로 "영어 예문 + 한국어 번역" 1개를 생성한다.
build_examples_llm.py(Claude Batch API)와 같은 역할이며 API 비용이 없다.

사용법:
    ollama serve  # (실행 중이 아니면)
    python3 scripts/build_examples_ollama.py --gate      # 샘플 20개 생성 후 품질 눈검사
    python3 scripts/build_examples_ollama.py             # 전체 생성 (중단 시 재실행하면 이어서 진행)
    python3 scripts/build_examples_ollama.py --apply     # 저장된 결과만 재적용 (생성 없음)
    python3 scripts/build_examples_ollama.py --selftest  # 검증 로직 회귀 테스트

생성 결과는 examples_ollama_results.jsonl에 즉시 기록되므로 중단해도 안전하다.
소요 시간: 9,200단어 × ~2초 ≈ 5~6시간 (M4 Pro 기준)
"""

import json
import os
import re
import sqlite3
import sys
import time
import urllib.request

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "databases", "engstudy.db")
RESULTS_PATH = os.path.join(SCRIPT_DIR, "examples_ollama_results.jsonl")

OLLAMA_URL = "http://localhost:11434/api/generate"
MODEL = "gemma4:latest"
GATE_SAMPLE_SIZE = 20

PROMPT_TEMPLATE = """\
Word: "{word}" (meaning in Korean: {meaning})

Write ONE short, natural English example sentence (max 12 words) using the word "{word}", with its Korean translation. The sentence must clearly show this meaning.

Respond with only valid JSON (no markdown):
{{"en": "example sentence", "ko": "한국어 번역"}}"""


def get_uncovered(conn) -> list[dict]:
    """word_examples가 없는 단어 (대표 뜻 포함)."""
    rows = conn.execute(
        "SELECT w.id, w.word, w.meaning FROM words w "
        "WHERE NOT EXISTS (SELECT 1 FROM word_examples e WHERE e.word_id = w.id) "
        "ORDER BY w.id"
    ).fetchall()
    return [{"id": r[0], "word": r[1], "meaning": r[2]} for r in rows]


def generate(word: str, meaning: str, temperature: float = 0.3) -> str:
    body = json.dumps({
        "model": MODEL,
        "prompt": PROMPT_TEMPLATE.format(word=word, meaning=meaning),
        "stream": False,
        "format": "json",
        "options": {"temperature": temperature, "num_predict": 150},
    }).encode()
    req = urllib.request.Request(OLLAMA_URL, body, {"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, timeout=300))["response"]


def _word_in_sentence(word: str, sentence: str) -> bool:
    """예문에 단어(굴절형 포함)가 실제로 쓰였는지 근사 확인.

    어간 근사: 4글자 이상 단어는 마지막 글자를 뗀 접두로도 인정
    (study→studies, run→running 류). 구두점은 무시한다.
    """
    tokens = re.findall(r"[a-z]+", sentence.lower())
    w = word.lower()
    if w in tokens:
        return True
    stem = w[:-1] if len(w) >= 4 else w
    return any(t.startswith(stem) for t in tokens)


def parse_example(word: str, text: str) -> dict | None:
    """모델 응답에서 유효한 예문 추출. 유효하지 않으면 None."""
    try:
        data = json.loads(text.strip())
    except json.JSONDecodeError:
        return None
    if not isinstance(data, dict):
        return None
    en = str(data.get("en", "")).strip()
    ko = str(data.get("ko", "")).strip()
    if not en or not ko:
        return None
    # 영어 예문에 대상 단어가 실제로 등장해야 하고, 번역에는 한글이 있어야 함
    if not _word_in_sentence(word, en):
        return None
    if not any("가" <= ch <= "힣" for ch in ko):
        return None
    # 한글이 영어 문장에 섞여 오는 오염 응답 거부
    if any("가" <= ch <= "힣" for ch in en):
        return None
    return {"en": en, "ko": ko}


def generate_example(word: str, meaning: str) -> dict | None:
    """생성 + 파싱. 무효 응답이면 온도를 올려 1회 재시도."""
    example = parse_example(word, generate(word, meaning))
    if example is None:
        example = parse_example(word, generate(word, meaning, temperature=0.7))
    return example


def load_done_ids() -> set[int]:
    if not os.path.exists(RESULTS_PATH):
        return set()
    with open(RESULTS_PATH, encoding="utf-8") as f:
        return {json.loads(line)["word_id"] for line in f if line.strip()}


def run_generation(conn) -> None:
    targets = get_uncovered(conn)
    done = load_done_ids()
    todo = [t for t in targets if t["id"] not in done]
    print(f"미커버 {len(targets):,}개 중 완료 {len(done):,}개 → 남은 작업 {len(todo):,}개")

    failed = 0
    t0 = time.time()
    with open(RESULTS_PATH, "a", encoding="utf-8") as f:
        for i, t in enumerate(todo, 1):
            example = generate_example(t["word"], t["meaning"])
            if example is None:
                failed += 1
            else:
                f.write(json.dumps(
                    {"word_id": t["id"], "en": example["en"], "ko": example["ko"]},
                    ensure_ascii=False) + "\n")
                f.flush()
            if i % 200 == 0:
                elapsed = time.time() - t0
                remaining = elapsed / i * (len(todo) - i)
                print(f"  진행: {i:,}/{len(todo):,} | 실패 {failed} | "
                      f"경과 {elapsed/60:.0f}분 | 남은 예상 {remaining/3600:.1f}시간")
    print(f"생성 완료: 성공 {len(todo)-failed:,} / 실패 {failed}")


def apply_results(conn) -> None:
    """저장된 결과를 word_examples에 적용 (멱등: 기존 llm 행 교체)."""
    entries = []
    with open(RESULTS_PATH, encoding="utf-8") as f:
        entries = [json.loads(line) for line in f if line.strip()]
    for e in entries:
        conn.execute(
            "DELETE FROM word_examples WHERE word_id = ? AND source = 'llm'", (e["word_id"],)
        )
        conn.execute(
            "INSERT INTO word_examples (word_id, sentence_en, sentence_ko, source) "
            "VALUES (?, ?, ?, 'llm')",
            (e["word_id"], e["en"], e["ko"]),
        )
    conn.commit()
    conn.execute("VACUUM")
    covered = conn.execute(
        "SELECT COUNT(DISTINCT word_id) FROM word_examples"
    ).fetchone()[0]
    total = conn.execute("SELECT COUNT(*) FROM words").fetchone()[0]
    print(f"적용 완료: {len(entries):,}개 예문 | 커버리지 {covered:,}/{total:,} "
          f"({covered/total*100:.1f}%)")


def run_gate(conn) -> None:
    """샘플 생성 후 품질 눈검사용 출력."""
    import random
    targets = get_uncovered(conn)
    random.seed(42)
    sample = random.sample(targets, min(GATE_SAMPLE_SIZE, len(targets)))
    failed = 0
    for t in sample:
        example = generate_example(t["word"], t["meaning"])
        if example is None:
            failed += 1
            print(f"{t['word']:<16} <무효 응답>")
        else:
            print(f"{t['word']:<16} {example['en']}")
            print(f"{'':<16} {example['ko']}")
    print(f"\n게이트: {len(sample)-failed}/{len(sample)} 유효")


def run_selftest() -> None:
    """파싱·검증 로직 회귀 테스트 (DB·LLM 불필요)."""
    cases = [
        # (기대 유효 여부, 단어, 모델 응답)
        (True, "apple", '{"en": "I ate an apple.", "ko": "나는 사과를 먹었다."}'),
        (True, "study", '{"en": "She studies math.", "ko": "그녀는 수학을 공부한다."}'),  # 굴절형
        (False, "apple", '{"en": "I ate a pear.", "ko": "나는 배를 먹었다."}'),          # 단어 미사용
        (False, "apple", '{"en": "I ate an apple.", "ko": "I ate an apple."}'),          # 한글 없음
        (False, "apple", '{"en": "나는 apple 먹었다.", "ko": "나는 사과를 먹었다."}'),   # 영문에 한글 오염
        (False, "apple", 'not json'),
        (False, "apple", '{"en": "", "ko": "사과"}'),
    ]
    for expected, word, text in cases:
        got = parse_example(word, text) is not None
        assert got == expected, f"parse_example({word!r}, {text!r}) 유효={got}, 기대 {expected}"
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
        if "--apply" not in sys.argv:
            run_generation(conn)
        apply_results(conn)
    finally:
        conn.close()


if __name__ == "__main__":
    main()
