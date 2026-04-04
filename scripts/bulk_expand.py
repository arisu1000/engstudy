#!/usr/bin/env python3
"""
기존 DB에 대량 단어를 추가하여 5,000+ 목표를 달성합니다.
scripts/word_data/ 디렉토리의 JSON 파일에서 단어를 로드합니다.
"""

import sqlite3
import os
import json
import glob

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "databases", "engstudy.db")
DATA_DIR = os.path.join(SCRIPT_DIR, "word_data")

DIFFICULTY_MAP = {"ELEMENTARY": 1, "MIDDLE_SCHOOL": 2, "HIGH_SCHOOL": 3, "COLLEGE": 4, "PROFESSIONAL": 5}


def get_next_id(cursor):
    cursor.execute("SELECT MAX(id) FROM words")
    result = cursor.fetchone()[0]
    return (result or 0) + 1


def load_word_files():
    """word_data/ 디렉토리의 모든 JSON 파일을 로드합니다."""
    all_words = []
    pattern = os.path.join(DATA_DIR, "*.json")
    for filepath in sorted(glob.glob(pattern)):
        with open(filepath, "r", encoding="utf-8") as f:
            data = json.load(f)
            all_words.extend(data)
        print(f"  로드: {os.path.basename(filepath)} ({len(data)}개)")
    return all_words


def expand_database():
    if not os.path.exists(DB_PATH):
        print(f"ERROR: DB 파일이 없습니다: {DB_PATH}")
        print("먼저 generate_word_db.py와 expand_words.py를 실행하세요.")
        return

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    word_id = get_next_id(cursor)

    # 기존 단어 목록
    cursor.execute("SELECT word FROM words")
    existing = set(row[0] for row in cursor.fetchall())
    print(f"기존 단어 수: {len(existing)}")

    # JSON 파일에서 단어 로드
    print("\n단어 데이터 로드 중...")
    all_words = load_word_files()
    print(f"총 로드된 단어: {len(all_words)}개")

    added = 0
    skipped = 0
    for w in all_words:
        # Support both old (abbreviated) and new (full) key formats
        word = w.get("word") or w.get("w")
        if not word:
            skipped += 1
            continue
        if word in existing:
            skipped += 1
            continue
        existing.add(word)

        age_group = w.get("age_group") or w.get("a", "HIGH_SCHOOL")
        difficulty = DIFFICULTY_MAP.get(age_group, 3)

        cursor.execute("""
            INSERT INTO words (id, word, pronunciation, meaning_ko, part_of_speech, example_en, example_ko,
                              domain, age_group, frequency_rank, difficulty, synonyms, antonyms, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            word_id, word,
            w.get("pron") or w.get("p", ""),
            w.get("meaning") or w.get("m", ""),
            w.get("pos") or w.get("t", "noun"),
            w.get("ex_en") or w.get("e", ""),
            w.get("ex_ko") or w.get("k", ""),
            w.get("domain") or w.get("d", "GENERAL"),
            age_group, word_id, difficulty,
            w.get("syn"), w.get("ant"), w.get("notes")
        ))
        word_id += 1
        added += 1

    conn.commit()

    # 통계 출력
    cursor.execute("SELECT COUNT(*) FROM words")
    total = cursor.fetchone()[0]
    cursor.execute("SELECT age_group, COUNT(*) FROM words GROUP BY age_group ORDER BY age_group")
    by_age = cursor.fetchall()
    cursor.execute("SELECT domain, COUNT(*) FROM words GROUP BY domain ORDER BY domain")
    by_domain = cursor.fetchall()

    print(f"\n{'='*50}")
    print(f"대량 확장 완료: {added}개 추가 (중복 스킵: {skipped}개)")
    print(f"{'='*50}")
    print(f"총 단어 수: {total}")
    print(f"\n연령대별:")
    for age, count in by_age:
        print(f"  {age}: {count}개")
    print(f"\n분야별:")
    for domain, count in by_domain:
        print(f"  {domain}: {count}개")
    print(f"\n파일: {DB_PATH}")
    print(f"크기: {os.path.getsize(DB_PATH) / 1024:.1f} KB")

    conn.close()


if __name__ == "__main__":
    expand_database()
