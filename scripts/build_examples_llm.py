#!/usr/bin/env python3
"""
word_examples 테이블 보완 스크립트 (Phase 2: Claude Haiku Batch API).

build_examples.py 실행 후 남은 미커버 단어에 대해
Anthropic Message Batches API로 예문을 생성한다.

사용법:
    pip install anthropic
    export ANTHROPIC_API_KEY=sk-ant-...
    python3 scripts/build_examples_llm.py

비용 예상 (Haiku 기준):
    - 입력: ~60 토큰/단어, 출력: ~50 토큰/단어
    - Batch API 50% 할인 적용
    - 6,000단어 기준: 약 $0.10~0.15
"""

import sqlite3
import json
import os
import time

try:
    import anthropic
except ImportError:
    print("anthropic 패키지 없음: pip install anthropic")
    raise

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "databases", "engstudy.db")
UNCOVERED_PATH = os.path.join(SCRIPT_DIR, "uncovered_words.json")
BATCH_RESULTS_PATH = os.path.join(SCRIPT_DIR, "batch_results.jsonl")

BATCH_SIZE = 2000  # 배치당 요청 수 (API 최대 10,000)
MODEL = "claude-haiku-4-5-20251001"

PROMPT_TEMPLATE = """\
Word: "{word}" ({pos}, meaning: {meaning})

Give ONE short, natural English example sentence using this word, with its Korean translation.
The sentence should clearly show the word's meaning.

Respond with only valid JSON (no markdown):
{{"en": "example sentence here", "ko": "한국어 번역"}}"""


def load_uncovered() -> list[dict]:
    with open(UNCOVERED_PATH, encoding="utf-8") as f:
        return json.load(f)


def create_batch(client, words_chunk: list[dict]) -> str:
    requests = []
    for w in words_chunk:
        requests.append(
            anthropic.types.message_create_params.Request(
                custom_id=str(w["id"]),
                params=anthropic.types.MessageCreateParamsNonStreaming(
                    model=MODEL,
                    max_tokens=100,
                    messages=[
                        {
                            "role": "user",
                            "content": PROMPT_TEMPLATE.format(
                                word=w["word"],
                                pos=w.get("pos", "noun"),
                                meaning=w["meaning"],
                            ),
                        }
                    ],
                ),
            )
        )
    batch = client.messages.batches.create(requests=requests)
    print(f"배치 생성: {batch.id} ({len(requests)}개 요청)")
    return batch.id


def wait_for_batch(client, batch_id: str) -> None:
    print(f"배치 처리 대기 중: {batch_id}")
    while True:
        batch = client.messages.batches.retrieve(batch_id)
        status = batch.processing_status
        counts = batch.request_counts
        print(
            f"  상태: {status} | "
            f"성공: {counts.succeeded} / 처리중: {counts.processing} / 오류: {counts.errored}"
        )
        if status == "ended":
            break
        time.sleep(30)


def collect_results(client, batch_id: str) -> dict[int, dict]:
    """배치 결과 수집 → {word_id: {en, ko}}"""
    results = {}
    for result in client.messages.batches.results(batch_id):
        if result.result.type != "succeeded":
            continue
        word_id = int(result.custom_id)
        content = result.result.message.content
        if not content:
            continue
        text = content[0].text.strip()
        try:
            data = json.loads(text)
            if "en" in data and "ko" in data:
                results[word_id] = data
        except (json.JSONDecodeError, KeyError):
            pass
    return results


def insert_results(results: dict[int, dict]) -> None:
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    count = 0
    for word_id, data in results.items():
        cursor.execute(
            "INSERT INTO word_examples (word_id, sentence_en, sentence_ko, source) "
            "VALUES (?, ?, ?, 'llm')",
            (word_id, data["en"], data["ko"]),
        )
        count += 1
    conn.commit()
    conn.close()
    print(f"DB 삽입: {count}개")


def main():
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        raise EnvironmentError("ANTHROPIC_API_KEY 환경변수를 설정하세요")

    client = anthropic.Anthropic(api_key=api_key)
    uncovered = load_uncovered()
    print(f"미커버 단어: {len(uncovered):,}개")

    total_inserted = 0
    for i in range(0, len(uncovered), BATCH_SIZE):
        chunk = uncovered[i : i + BATCH_SIZE]
        print(f"\n--- 배치 {i // BATCH_SIZE + 1}: {len(chunk)}개 단어 ---")
        batch_id = create_batch(client, chunk)
        wait_for_batch(client, batch_id)
        results = collect_results(client, batch_id)
        insert_results(results)
        total_inserted += len(results)
        print(f"누적 삽입: {total_inserted:,}개")

    print(f"\n완료: 총 {total_inserted:,}개 예문 추가")


if __name__ == "__main__":
    main()
