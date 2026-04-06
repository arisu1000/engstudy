#!/usr/bin/env python3
"""
word_meanings 테이블 생성 스크립트.

WordNet(NLTK)으로 의미 빈도 순서를 결정하고,
kengdic.tsv에서 한국어 의미를 가져와 word_meanings 테이블을 채운다.

사용법:
    pip install nltk
    python3 -c "import nltk; nltk.download('wordnet'); nltk.download('omw-1.4')"
    python3 scripts/build_meanings.py

출력:
    app/src/main/assets/databases/engstudy.db 의 word_meanings 테이블
"""

import sqlite3
import csv
import os
from collections import defaultdict

try:
    from nltk.corpus import wordnet as wn
    WORDNET_AVAILABLE = True
except ImportError:
    WORDNET_AVAILABLE = False
    print("NLTK WordNet 없음 — kengdic 원래 순서로 대체")

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "databases", "engstudy.db")
KENGDIC_PATH = os.path.join(SCRIPT_DIR, "kengdic", "kengdic.tsv")

WN_POS_MAP = {
    "n": "noun",
    "v": "verb",
    "a": "adjective",
    "s": "adjective",  # satellite adjective
    "r": "adverb",
}


def load_kengdic():
    """kengdic.tsv 로드 → {english_gloss: [korean_surface, ...]}"""
    en_to_ko = defaultdict(list)
    with open(KENGDIC_PATH, encoding="utf-8") as f:
        reader = csv.reader(f, delimiter="\t")
        for row in reader:
            if len(row) < 4:
                continue
            korean = row[1].strip()
            english = row[3].strip().lower()
            if not english or not korean:
                continue
            if korean not in en_to_ko[english]:
                en_to_ko[english].append(korean)
    return en_to_ko


def get_wordnet_pos_order(word):
    """WordNet에서 해당 단어의 synset POS를 빈도순으로 반환."""
    if not WORDNET_AVAILABLE:
        return []
    synsets = wn.synsets(word)
    seen = []
    for s in synsets:
        pos = WN_POS_MAP.get(s.pos(), "noun")
        if pos not in seen:
            seen.append(pos)
    return seen


def build_ordered_meanings(word, current_meaning, current_pos, kengdic):
    """
    단어에 대한 (meaning, pos, sense_order) 목록 반환.
    WordNet 순서 우선, 없으면 kengdic 원래 순서.
    최대 5개.
    """
    ko_meanings = kengdic.get(word.lower(), [])
    if not ko_meanings:
        # kengdic에 없으면 기존 meaning 필드를 분리해서 사용
        ko_meanings = [m.strip() for m in current_meaning.split(",") if m.strip()]

    pos_order = get_wordnet_pos_order(word)
    default_pos = pos_order[0] if pos_order else (current_pos or "noun")

    results = []
    for i, meaning in enumerate(ko_meanings[:5]):
        # WordNet POS 순서를 의미 인덱스에 맞춰 적용
        pos = pos_order[min(i, len(pos_order) - 1)] if pos_order else default_pos
        results.append((meaning, pos, i))

    return results


def main():
    kengdic = load_kengdic()
    print(f"kengdic 로드 완료: {sum(len(v) for v in kengdic.values()):,}개 의미")

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # 기존 데이터 초기화 후 재생성
    cursor.execute("DROP TABLE IF EXISTS word_meanings")
    cursor.execute(
        """CREATE TABLE word_meanings (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            word_id INTEGER NOT NULL,
            meaning TEXT NOT NULL,
            pos TEXT NOT NULL DEFAULT 'noun',
            meaning_type TEXT NOT NULL DEFAULT 'ko',
            sense_order INTEGER NOT NULL DEFAULT 0,
            source TEXT NOT NULL DEFAULT 'kengdic',
            FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
        )"""
    )
    cursor.execute(
        "CREATE INDEX index_word_meanings_word_id ON word_meanings(word_id)"
    )

    cursor.execute("SELECT id, word, meaning, part_of_speech FROM words")
    words = cursor.fetchall()
    total = len(words)
    print(f"처리할 단어: {total:,}개")

    inserted = 0
    for i, (word_id, word, current_meaning, current_pos) in enumerate(words):
        ordered = build_ordered_meanings(word, current_meaning, current_pos, kengdic)
        for meaning_text, pos, sense_order in ordered:
            cursor.execute(
                "INSERT INTO word_meanings (word_id, meaning, pos, meaning_type, sense_order, source) "
                "VALUES (?, ?, ?, 'ko', ?, 'kengdic')",
                (word_id, meaning_text, pos, sense_order),
            )
            inserted += 1

        if (i + 1) % 2000 == 0:
            conn.commit()
            print(f"  {i + 1:,}/{total:,} 처리 중... ({inserted:,}개 의미 삽입)")

    conn.commit()
    conn.close()
    print(f"\n완료: {total:,}개 단어, {inserted:,}개 의미 삽입")
    print(f"평균 {inserted / total:.1f}개 의미/단어")


if __name__ == "__main__":
    main()
