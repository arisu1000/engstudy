#!/usr/bin/env python3
"""
word_examples 테이블 생성 스크립트 (Phase 1: Tatoeba 매칭).

Tatoeba 영한 문장에서 단어를 포함하는 문장을 찾아 word_examples 테이블을 채운다.
LLM 토큰 0개 사용.

사용법:
    pip install nltk
    python3 -c "import nltk; nltk.download('wordnet')"
    python3 scripts/build_examples.py

출력:
    - app/src/main/assets/databases/engstudy.db 의 word_examples 테이블
    - scripts/uncovered_words.json (LLM 보완이 필요한 단어 목록)

다음 단계:
    python3 scripts/build_examples_llm.py  # Claude Haiku Batch API로 미커버 단어 보완
"""

import sqlite3
import re
import os
import json
from collections import defaultdict

try:
    from nltk.stem import WordNetLemmatizer
    _lemmatizer = WordNetLemmatizer()
    def lemmatize(word):
        v = _lemmatizer.lemmatize(word, pos="v")
        n = _lemmatizer.lemmatize(word, pos="n")
        return {word, v, n}
except ImportError:
    def lemmatize(word):
        forms = {word}
        if word.endswith("ing") and len(word) > 5:
            forms.add(word[:-3])
        if word.endswith("ed") and len(word) > 4:
            forms.add(word[:-2])
        if word.endswith("es") and len(word) > 3:
            forms.add(word[:-2])
        if word.endswith("s") and len(word) > 3:
            forms.add(word[:-1])
        return forms

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "databases", "engstudy.db")
TATOEBA_PATH = os.path.join(SCRIPT_DIR, "tatoeba_kor_eng", "kor.txt")
UNCOVERED_PATH = os.path.join(SCRIPT_DIR, "uncovered_words.json")

MAX_PER_WORD = 3          # 단어당 최대 예문 수
MIN_WORDS = 4             # 예문 최소 단어 수
MAX_WORDS = 25            # 예문 최대 단어 수


def main():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # 기존 데이터 초기화 후 재생성
    cursor.execute("DROP TABLE IF EXISTS word_examples")
    cursor.execute(
        """CREATE TABLE word_examples (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            word_id INTEGER NOT NULL,
            sentence_en TEXT NOT NULL,
            sentence_ko TEXT NOT NULL,
            source TEXT NOT NULL DEFAULT 'tatoeba',
            FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
        )"""
    )
    cursor.execute(
        "CREATE INDEX index_word_examples_word_id ON word_examples(word_id)"
    )

    # 단어 목록 로드
    cursor.execute("SELECT id, word, meaning, part_of_speech FROM words")
    rows = cursor.fetchall()
    # {base_form: word_id} — 단어 및 그 변형으로 매핑
    word_to_id: dict[str, int] = {}
    word_info: dict[int, dict] = {}  # word_id -> {word, meaning, pos}
    for word_id, word, meaning, pos in rows:
        word_lower = word.lower()
        word_to_id[word_lower] = word_id
        word_info[word_id] = {"word": word, "meaning": meaning, "pos": pos or "noun"}

    print(f"단어 목록 로드: {len(rows):,}개")

    # Tatoeba 파싱 및 매칭
    example_counts: dict[int, int] = defaultdict(int)
    total_inserted = 0
    tatoeba_count = 0

    with open(TATOEBA_PATH, encoding="utf-8") as f:
        for line in f:
            parts = line.strip().split("\t")
            if len(parts) < 2:
                continue
            sentence_en = parts[0].strip()
            sentence_ko = parts[1].strip()
            if not sentence_en or not sentence_ko:
                continue

            word_count = len(sentence_en.split())
            if word_count < MIN_WORDS or word_count > MAX_WORDS:
                continue

            tatoeba_count += 1

            # 영문 토큰 + 표제어 추출
            tokens = set(re.findall(r"\b[a-z]+\b", sentence_en.lower()))
            all_forms: set[str] = set()
            for t in tokens:
                all_forms.update(lemmatize(t))

            matched = all_forms & word_to_id.keys()
            for match in matched:
                word_id = word_to_id[match]
                if example_counts[word_id] < MAX_PER_WORD:
                    cursor.execute(
                        "INSERT INTO word_examples (word_id, sentence_en, sentence_ko, source) "
                        "VALUES (?, ?, ?, 'tatoeba')",
                        (word_id, sentence_en, sentence_ko),
                    )
                    example_counts[word_id] += 1
                    total_inserted += 1

    conn.commit()

    covered = set(example_counts.keys())
    uncovered_ids = set(word_info.keys()) - covered
    coverage_pct = len(covered) / len(rows) * 100

    print(f"\nTatoeba 문장 처리: {tatoeba_count:,}개")
    print(f"예문 삽입: {total_inserted:,}개")
    print(f"커버리지: {len(covered):,}/{len(rows):,} ({coverage_pct:.1f}%)")
    print(f"미커버 단어: {len(uncovered_ids):,}개 → uncovered_words.json 저장")

    # 미커버 단어 저장 (LLM 보완용)
    uncovered = [
        {"id": wid, **word_info[wid]}
        for wid in sorted(uncovered_ids)
    ]
    with open(UNCOVERED_PATH, "w", encoding="utf-8") as f:
        json.dump(uncovered, f, ensure_ascii=False, indent=2)

    conn.close()
    print(f"\n완료. LLM 보완이 필요하면: python3 scripts/build_examples_llm.py")


if __name__ == "__main__":
    main()
