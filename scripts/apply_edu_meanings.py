#!/usr/bin/env python3
"""
교육부 뜻 우선 적용 스크립트.

kengdic.tsv가 한국어 가나다순으로 정렬되어 있어 words.meaning에
희귀 의미가 먼저 노출되는 문제(예: white → "공백의")를 보정한다.
교육부 edu_words(전문가 검수 데이터)와 겹치는 단어는 교육부 뜻을
대표 뜻으로 사용한다.

- words.meaning       → 교육부 뜻으로 교체 (meaning_type='ko')
- word_meanings       → 교육부 뜻을 sense_order 0으로 삽입/재배치 (source='edu', 최대 5개 유지)

기능어(build_word_db.py의 FUNCTION_WORDS)는 제외한다: 수동 큐레이션 뜻이
교육부 단일 뜻보다 낫다 (예: just → "단지, 바로, 방금" vs 교육부 "올바른").
과거 실행으로 덮인 기능어는 실행 시 큐레이션 뜻으로 복원한다.

빌드 파이프라인 순서: build_word_db.py → build_meanings.py → apply_edu_meanings.py
여러 번 실행해도 안전하다 (멱등).

사용법:
    python3 scripts/apply_edu_meanings.py            # 적용 + 검증
    python3 scripts/apply_edu_meanings.py --verify   # 검증만
"""

import ast
import os
import sqlite3
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "databases", "engstudy.db")

MAX_SENSES = 5  # build_meanings.py와 동일한 상한

# 적용 후 반드시 성립해야 하는 대표 케이스 (실패 시 롤백)
EXPECTED = {
    "white": "흰",
    "water": "물",
    "home": "가정",
    "change": "변화",
}


def load_edu_meanings(conn):
    """edu_words에서 {소문자 단어: (뜻, 품사)} 매핑 생성."""
    rows = conn.execute(
        "SELECT lower(word), meaning, part_of_speech FROM edu_words"
    ).fetchall()
    return {word: (meaning.strip(), pos.strip()) for word, meaning, pos in rows if meaning.strip()}


def load_function_words():
    """build_word_db.py의 FUNCTION_WORDS를 AST로 추출: {단어: (뜻, meaning_type, 품사)}."""
    path = os.path.join(SCRIPT_DIR, "build_word_db.py")
    tree = ast.parse(open(path, encoding="utf-8").read())
    for node in ast.walk(tree):
        if isinstance(node, ast.Assign) and any(
            isinstance(t, ast.Name) and t.id == "FUNCTION_WORDS" for t in node.targets
        ):
            return ast.literal_eval(node.value)
    raise RuntimeError("build_word_db.py에서 FUNCTION_WORDS를 찾지 못함")


def restore_function_words(conn, function_words):
    """과거 실행이 덮어쓴 기능어를 큐레이션 뜻으로 복원.

    word_meanings에 삽입됐던 교육부 뜻(source='edu')도 제거한다 —
    기능어는 대표 뜻이 words.meaning의 큐레이션 값이어야 한다.
    """
    restored = 0
    for word, (meaning, meaning_type, pos) in function_words.items():
        row = conn.execute(
            "SELECT id, meaning FROM words WHERE lower(word) = ?", (word,)
        ).fetchone()
        if row is None:
            continue
        word_id, current = row
        edu_rows = conn.execute(
            "SELECT COUNT(*) FROM word_meanings WHERE word_id = ? AND source = 'edu'",
            (word_id,),
        ).fetchone()[0]
        if current == meaning and edu_rows == 0:
            continue
        conn.execute(
            "UPDATE words SET meaning = ?, meaning_type = ?, part_of_speech = ? WHERE id = ?",
            (meaning, meaning_type, pos, word_id),
        )
        conn.execute(
            "DELETE FROM word_meanings WHERE word_id = ? AND source = 'edu'", (word_id,)
        )
        remaining = conn.execute(
            "SELECT meaning, pos, meaning_type, source FROM word_meanings "
            "WHERE word_id = ? ORDER BY sense_order",
            (word_id,),
        ).fetchall()
        conn.execute("DELETE FROM word_meanings WHERE word_id = ?", (word_id,))
        for order, (m, p, mt, src) in enumerate(remaining):
            conn.execute(
                "INSERT INTO word_meanings (word_id, meaning, pos, meaning_type, sense_order, source) "
                "VALUES (?, ?, ?, ?, ?, ?)",
                (word_id, m, p, mt, order, src),
            )
        restored += 1
    return restored


def apply_word_meaning(conn, edu_map):
    """words.meaning을 교육부 뜻으로 교체."""
    updated = 0
    rows = conn.execute("SELECT id, lower(word), meaning, meaning_type FROM words").fetchall()
    for word_id, word, meaning, meaning_type in rows:
        if word not in edu_map:
            continue
        edu_meaning, _ = edu_map[word]
        if meaning == edu_meaning and meaning_type == "ko":
            continue
        conn.execute(
            "UPDATE words SET meaning = ?, meaning_type = 'ko' WHERE id = ?",
            (edu_meaning, word_id),
        )
        updated += 1
    return updated


def apply_sense_order(conn, edu_map):
    """word_meanings에서 교육부 뜻을 sense_order 0으로 삽입/재배치."""
    updated = 0
    word_rows = conn.execute(
        "SELECT id, lower(word), part_of_speech FROM words"
    ).fetchall()
    for word_id, word, word_pos in word_rows:
        if word not in edu_map:
            continue
        edu_meaning, edu_pos = edu_map[word]

        senses = conn.execute(
            "SELECT meaning, pos, meaning_type, source FROM word_meanings "
            "WHERE word_id = ? ORDER BY sense_order",
            (word_id,),
        ).fetchall()

        if senses and senses[0][0] == edu_meaning:
            continue  # 이미 적용됨

        # 교육부 뜻을 맨 앞으로: 기존에 있으면 재배치, 없으면 삽입
        remaining = [s for s in senses if s[0] != edu_meaning]
        first_pos = edu_pos or (senses[0][1] if senses else word_pos)
        new_senses = [(edu_meaning, first_pos, "ko", "edu")] + remaining

        conn.execute("DELETE FROM word_meanings WHERE word_id = ?", (word_id,))
        for order, (meaning, pos, meaning_type, source) in enumerate(new_senses[:MAX_SENSES]):
            conn.execute(
                "INSERT INTO word_meanings (word_id, meaning, pos, meaning_type, sense_order, source) "
                "VALUES (?, ?, ?, ?, ?, ?)",
                (word_id, meaning, pos, meaning_type, order, source),
            )
        updated += 1
    return updated


def verify(conn, function_words):
    """대표 케이스와 무결성 검증. 실패 시 AssertionError."""
    for word, expected in EXPECTED.items():
        meaning = conn.execute(
            "SELECT meaning FROM words WHERE lower(word) = ?", (word,)
        ).fetchone()
        assert meaning and meaning[0] == expected, (
            f"words.meaning 불일치: {word} → {meaning and meaning[0]!r} (기대: {expected!r})"
        )
        first_sense = conn.execute(
            "SELECT m.meaning FROM word_meanings m JOIN words w ON w.id = m.word_id "
            "WHERE lower(w.word) = ? AND m.sense_order = 0",
            (word,),
        ).fetchone()
        assert first_sense and first_sense[0] == expected, (
            f"word_meanings 첫 의미 불일치: {word} → {first_sense and first_sense[0]!r}"
        )

    # 기능어는 큐레이션 뜻이 유지되어야 함
    for word, (curated, _, _) in function_words.items():
        row = conn.execute(
            "SELECT meaning FROM words WHERE lower(word) = ?", (word,)
        ).fetchone()
        assert row is None or row[0] == curated, (
            f"기능어 큐레이션 뜻 훼손: {word} → {row[0]!r} (기대: {curated!r})"
        )

    # 겹치는 단어(기능어 제외)는 교육부 뜻이 대표이거나 word_meanings에 보존되어야 함.
    # (build_meanings_llm.py --edu-merge가 일부 단어의 대표 뜻을 LLM 뜻으로
    #  교체하므로 words.meaning 완전 일치는 요구하지 않는다)
    placeholders = ",".join("?" * len(function_words))
    mismatch = conn.execute(
        "SELECT COUNT(*) FROM words w JOIN edu_words e ON lower(w.word) = lower(e.word) "
        f"WHERE trim(e.meaning) != '' AND lower(w.word) NOT IN ({placeholders}) "
        "AND w.meaning != trim(e.meaning) "
        "AND NOT EXISTS (SELECT 1 FROM word_meanings m "
        "                WHERE m.word_id = w.id AND m.meaning = trim(e.meaning))",
        list(function_words),
    ).fetchone()[0]
    assert mismatch == 0, f"교육부 뜻 미적용·미보존 단어 {mismatch}개"

    # sense_order 무결성: 0부터 연속, 상한 이내
    bad_order = conn.execute(
        "SELECT COUNT(*) FROM (SELECT word_id, MIN(sense_order) mn, MAX(sense_order) mx, COUNT(*) c "
        "FROM word_meanings GROUP BY word_id HAVING mn != 0 OR mx != c - 1 OR c > ?)",
        (MAX_SENSES,),
    ).fetchone()[0]
    assert bad_order == 0, f"sense_order 무결성 위반 {bad_order}개 단어"

    print("검증 통과")


def main():
    function_words = load_function_words()
    conn = sqlite3.connect(DB_PATH)
    try:
        if "--verify" in sys.argv:
            verify(conn, function_words)
            return

        edu_map = load_edu_meanings(conn)
        edu_map = {w: v for w, v in edu_map.items() if w not in function_words}
        print(f"edu_words 로드: {len(edu_map):,}개 (기능어 {len(function_words)}개 제외)")

        restored = restore_function_words(conn, function_words)
        words_updated = apply_word_meaning(conn, edu_map)
        senses_updated = apply_sense_order(conn, edu_map)
        verify(conn, function_words)
        conn.commit()
        conn.execute("VACUUM")
        print(f"words.meaning 교체: {words_updated:,}개 / word_meanings 재배치: {senses_updated:,}개 / "
              f"기능어 복원: {restored:,}개")
    except AssertionError:
        conn.rollback()
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
