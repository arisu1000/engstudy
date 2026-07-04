#!/usr/bin/env python3
"""배치 단어 추가를 위한 공통 유틸리티."""

import sqlite3
import os

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "databases", "engstudy.db")


def get_connection():
    return sqlite3.connect(DB_PATH)


def get_existing_words(conn):
    """기존 단어 목록을 소문자 set으로 반환."""
    cursor = conn.cursor()
    cursor.execute("SELECT LOWER(word) FROM words")
    return set(row[0] for row in cursor.fetchall())


def get_next_id(conn):
    cursor = conn.cursor()
    cursor.execute("SELECT MAX(id) FROM words")
    result = cursor.fetchone()[0]
    return (result or 0) + 1


def add_words(conn, words_data, existing):
    """
    words_data: list of tuples:
      (word, pronunciation, meaning_ko, part_of_speech, example_en, example_ko,
       domain, age_group, difficulty, synonyms, antonyms, notes)

    Returns: number of words added
    """
    cursor = conn.cursor()
    next_id = get_next_id(conn)
    added = 0
    freq_rank = next_id  # 빈도 순위는 ID 순으로

    rows = []
    for w in words_data:
        word_lower = w[0].lower()
        if word_lower in existing:
            continue
        existing.add(word_lower)

        row = (
            next_id,        # id
            w[0],           # word
            w[1],           # pronunciation
            w[2],           # meaning_ko
            w[3],           # part_of_speech
            w[4],           # example_en
            w[5],           # example_ko
            w[6],           # domain
            w[7],           # age_group
            freq_rank,      # frequency_rank
            w[8],           # difficulty
            w[9],           # synonyms
            w[10],          # antonyms
            w[11] if len(w) > 11 else None  # notes
        )
        rows.append(row)
        next_id += 1
        freq_rank += 1
        added += 1

    if rows:
        cursor.executemany("""
            INSERT INTO words (id, word, pronunciation, meaning_ko, part_of_speech,
                             example_en, example_ko, domain, age_group, frequency_rank,
                             difficulty, synonyms, antonyms, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, rows)
        conn.commit()

    return added


def print_stats(conn, batch_name):
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM words")
    total = cursor.fetchone()[0]

    cursor.execute("SELECT age_group, COUNT(*) FROM words GROUP BY age_group ORDER BY age_group")
    by_age = cursor.fetchall()

    cursor.execute("SELECT domain, COUNT(*) FROM words GROUP BY domain ORDER BY domain")
    by_domain = cursor.fetchall()

    print(f"\n{'='*50}")
    print(f"{batch_name} 완료")
    print(f"{'='*50}")
    print(f"총 단어 수: {total}")
    print(f"\n연령대별:")
    for age, count in by_age:
        print(f"  {age}: {count}개")
    print(f"\n분야별:")
    for domain, count in by_domain:
        print(f"  {domain}: {count}개")
    print(f"\nDB 크기: {os.path.getsize(DB_PATH) / 1024:.1f} KB")
