#!/usr/bin/env python3
"""
외부 소스 기반 단어 DB 생성 파이프라인 (v5 스키마)
- wordfreq: 영어 빈도순 20,000 단어
- MUSE en-ko: 영→한 번역
- kengdic: 영→한 보충
- 빈도 기반 단계 자동 분류
- 교육부 3,000 단어 (edu_words 테이블)

주의: 이 스크립트를 실행하면 기존 DB를 백업 후 새로 생성합니다.
Room 호환을 위해 user_version, room_master_table을 자동 설정합니다.
"""

import sqlite3
import os
import json
import xlrd
from wordfreq import top_n_list, zipf_frequency

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "databases", "engstudy.db")
MUSE_PATH = os.path.join(SCRIPT_DIR, "en-ko-muse.txt")
KENGDIC_PATH = os.path.join(SCRIPT_DIR, "kengdic", "kengdic.tsv")
EDU_XLS_PATH = os.path.join(SCRIPT_DIR, "edu_3000.xls")
UNMATCHED_PATH = os.path.join(SCRIPT_DIR, "unmatched_words.json")

# =====================================================================
# Room 호환 설정 - AppDatabase.version과 일치시켜야 함
# AppDatabase.kt의 version을 변경하면 여기도 함께 변경할 것
# =====================================================================
ROOM_DB_VERSION = 6
# Room이 생성하는 identity hash (app/build/.../AppDatabase_Impl.java에서 확인 가능)
# 스키마가 바뀌면 빌드 후 이 값을 업데이트해야 함
ROOM_IDENTITY_HASH = "4e9d43daf3ba0b98d947977dcf6e07d9"


def get_stage(zipf_score):
    if zipf_score >= 5.5:
        return 1
    elif zipf_score >= 4.5:
        return 2
    elif zipf_score >= 3.8:
        return 3
    elif zipf_score >= 3.0:
        return 4
    elif zipf_score >= 2.2:
        return 5
    else:
        return 6


def load_muse():
    d = {}
    if not os.path.exists(MUSE_PATH):
        print("  WARNING: MUSE 파일 없음")
        return d
    with open(MUSE_PATH, encoding="utf-8") as f:
        for line in f:
            parts = line.strip().split('\t')
            if len(parts) == 2:
                en, ko = parts[0].lower().strip(), parts[1].strip()
                if en and ko and ko != en:
                    if en not in d:
                        d[en] = []
                    if ko not in d[en] and len(d[en]) < 3:
                        d[en].append(ko)
    return d


def load_kengdic():
    d = {}
    if not os.path.exists(KENGDIC_PATH):
        print("  WARNING: kengdic 파일 없음")
        return d
    with open(KENGDIC_PATH, encoding="utf-8") as f:
        next(f)
        for line in f:
            parts = line.strip().split('\t')
            if len(parts) >= 4:
                korean = parts[1].strip()
                english = parts[3].strip().lower()
                if english and ' ' not in english and english.isalpha() and len(korean) < 20:
                    if english not in d:
                        d[english] = []
                    if korean not in d[english] and len(d[english]) < 3:
                        d[english].append(korean)
    return d


def get_meaning(word, muse, kengdic):
    meanings = []
    if word in muse:
        meanings.extend(muse[word])
    if word in kengdic:
        for m in kengdic[word]:
            if m not in meanings:
                meanings.append(m)
    if meanings:
        return ", ".join(meanings[:3]), "ko"
    return None, None


def guess_pos(word):
    if word.endswith(('tion', 'sion', 'ment', 'ness', 'ity', 'ence', 'ance', 'ism', 'ology', 'ship', 'dom')):
        return 'noun'
    elif word.endswith(('ous', 'ive', 'ful', 'less', 'able', 'ible', 'ical', 'ent', 'ant')):
        return 'adjective'
    elif word.endswith(('ly',)):
        return 'adverb'
    elif word.endswith(('ize', 'ise', 'ify')):
        return 'verb'
    return 'noun'


# 기능어 수동 뜻
FUNCTION_WORDS = {
    "the": ("관사: 그, 저 (정관사)", "ko", "article"),
    "to": ("~에, ~으로, ~하기 위해", "ko", "preposition"),
    "and": ("그리고, ~와/과", "ko", "conjunction"),
    "of": ("~의, ~중에서", "ko", "preposition"),
    "in": ("~안에, ~에서", "ko", "preposition"),
    "is": ("~이다 (be의 3인칭 단수)", "ko", "verb"),
    "for": ("~을 위해, ~동안", "ko", "preposition"),
    "that": ("그, 저; ~라는 것", "ko", "pronoun"),
    "you": ("당신, 너", "ko", "pronoun"),
    "it": ("그것", "ko", "pronoun"),
    "on": ("~위에, ~에 대해", "ko", "preposition"),
    "with": ("~와 함께, ~으로", "ko", "preposition"),
    "this": ("이것, 이", "ko", "pronoun"),
    "was": ("~이었다 (be의 과거)", "ko", "verb"),
    "be": ("~이다, 있다, 되다", "ko", "verb"),
    "as": ("~로서, ~만큼, ~할 때", "ko", "conjunction"),
    "are": ("~이다 (be의 복수형)", "ko", "verb"),
    "have": ("가지다, 먹다, ~한 적이 있다", "ko", "verb"),
    "at": ("~에서, ~에", "ko", "preposition"),
    "he": ("그 (남성)", "ko", "pronoun"),
    "not": ("~아닌, ~않다", "ko", "adverb"),
    "by": ("~에 의해, ~까지, ~옆에", "ko", "preposition"),
    "but": ("그러나, 하지만", "ko", "conjunction"),
    "from": ("~에서, ~부터", "ko", "preposition"),
    "my": ("나의", "ko", "pronoun"),
    "or": ("또는, ~이나", "ko", "conjunction"),
    "we": ("우리", "ko", "pronoun"),
    "your": ("너의, 당신의", "ko", "pronoun"),
    "all": ("모든, 전부", "ko", "adjective"),
    "so": ("그래서, 그렇게, 매우", "ko", "adverb"),
    "his": ("그의", "ko", "pronoun"),
    "they": ("그들", "ko", "pronoun"),
    "me": ("나를, 나에게", "ko", "pronoun"),
    "if": ("만약 ~라면", "ko", "conjunction"),
    "can": ("~할 수 있다", "ko", "verb"),
    "will": ("~할 것이다", "ko", "verb"),
    "just": ("단지, 바로, 방금", "ko", "adverb"),
    "like": ("좋아하다; ~같은", "ko", "verb"),
    "about": ("~에 대해, 약", "ko", "preposition"),
    "up": ("위로, 일어나서", "ko", "adverb"),
    "out": ("밖으로", "ko", "adverb"),
    "what": ("무엇, 어떤", "ko", "pronoun"),
    "when": ("언제, ~할 때", "ko", "adverb"),
    "more": ("더 많은, 더", "ko", "adjective"),
    "do": ("하다", "ko", "verb"),
    "who": ("누구", "ko", "pronoun"),
    "there": ("거기에", "ko", "adverb"),
    "her": ("그녀의, 그녀를", "ko", "pronoun"),
    "no": ("아니오; 없는", "ko", "adverb"),
    "an": ("하나의 (부정관사)", "ko", "article"),
    "go": ("가다", "ko", "verb"),
    "how": ("어떻게, 얼마나", "ko", "adverb"),
    "its": ("그것의", "ko", "pronoun"),
    "then": ("그 다음에", "ko", "adverb"),
    "where": ("어디에", "ko", "adverb"),
    "also": ("또한", "ko", "adverb"),
}

# 도메인 분류 키워드
DOMAIN_KEYWORDS = {
    "MEDICINE": {"disease", "symptom", "patient", "doctor", "hospital", "surgery", "medicine",
                 "blood", "brain", "heart", "cancer", "pain", "therapy", "fever", "immune", "vaccine", "drug"},
    "TECHNOLOGY": {"computer", "software", "hardware", "internet", "digital", "data", "algorithm",
                   "network", "server", "database", "cloud", "cyber", "robot", "device", "pixel", "encrypt", "code"},
    "BUSINESS": {"company", "market", "economy", "finance", "invest", "stock", "trade", "profit",
                 "revenue", "budget", "tax", "contract", "corporate", "customer", "brand", "commerce"},
    "SCIENCE": {"science", "research", "experiment", "theory", "atom", "molecule", "chemical",
                "physics", "biology", "ecology", "evolution", "species", "climate", "energy", "solar", "planet", "cell", "gene"},
    "LAW": {"law", "legal", "court", "judge", "trial", "jury", "crime", "criminal",
            "prison", "arrest", "attorney", "lawyer", "lawsuit", "regulation"},
    "EDUCATION": {"school", "university", "student", "teacher", "education", "learn",
                  "classroom", "curriculum", "exam", "lecture", "scholarship", "library"},
    "ARTS": {"art", "music", "paint", "dance", "theater", "film", "movie", "song",
             "poetry", "novel", "sculpture", "gallery", "museum", "orchestra"},
    "SPORTS": {"sport", "game", "team", "player", "coach", "champion", "race", "match",
               "goal", "score", "stadium", "athlete", "marathon"},
    "FOOD": {"food", "cook", "recipe", "meal", "restaurant", "kitchen", "vegetable",
             "fruit", "meat", "bread", "rice", "dessert", "nutrition"},
    "TRAVEL": {"travel", "flight", "hotel", "airport", "passport", "tourism", "destination",
               "luggage", "cruise"},
}


def classify_domain(word):
    for domain, keywords in DOMAIN_KEYWORDS.items():
        if word in keywords:
            return domain
    return "GENERAL"


def create_schema(cursor):
    """v5 스키마에 맞는 테이블 생성"""

    # words 테이블 (메인 단어)
    cursor.execute("""
        CREATE TABLE words (
            id INTEGER PRIMARY KEY NOT NULL,
            word TEXT NOT NULL,
            pronunciation TEXT NOT NULL DEFAULT '',
            meaning TEXT NOT NULL,
            meaning_type TEXT NOT NULL DEFAULT 'ko',
            part_of_speech TEXT NOT NULL DEFAULT 'noun',
            example_en TEXT NOT NULL DEFAULT '',
            example_ko TEXT NOT NULL DEFAULT '',
            stage INTEGER NOT NULL,
            domain TEXT NOT NULL DEFAULT 'GENERAL',
            frequency_rank INTEGER NOT NULL,
            difficulty INTEGER NOT NULL DEFAULT 3,
            synonyms TEXT,
            antonyms TEXT,
            notes TEXT
        )
    """)
    cursor.execute("CREATE INDEX index_words_stage ON words(stage)")
    cursor.execute("CREATE INDEX index_words_domain ON words(domain)")
    cursor.execute("CREATE INDEX index_words_frequency_rank ON words(frequency_rank)")
    cursor.execute("CREATE INDEX index_words_word ON words(word)")

    # edu_words 테이블 (교육부 3000)
    cursor.execute("""
        CREATE TABLE edu_words (
            id INTEGER PRIMARY KEY NOT NULL,
            word TEXT NOT NULL,
            meaning TEXT NOT NULL,
            level TEXT NOT NULL,
            part_of_speech TEXT NOT NULL DEFAULT '',
            variant1 TEXT NOT NULL DEFAULT '',
            variant2 TEXT NOT NULL DEFAULT ''
        )
    """)
    cursor.execute("CREATE INDEX index_edu_words_level ON edu_words(level)")
    cursor.execute("CREATE INDEX index_edu_words_word ON edu_words(word)")

    # bookmarks 테이블
    cursor.execute("""
        CREATE TABLE bookmarks (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            word_id INTEGER NOT NULL,
            created_at INTEGER NOT NULL,
            FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
        )
    """)
    cursor.execute("CREATE UNIQUE INDEX index_bookmarks_word_id ON bookmarks(word_id)")

    # learning_progress 테이블
    cursor.execute("""
        CREATE TABLE learning_progress (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            word_id INTEGER NOT NULL,
            ease_factor REAL NOT NULL DEFAULT 2.5,
            interval_days INTEGER NOT NULL DEFAULT 0,
            repetitions INTEGER NOT NULL DEFAULT 0,
            next_review_date INTEGER NOT NULL,
            last_reviewed_date INTEGER,
            times_correct INTEGER NOT NULL DEFAULT 0,
            times_incorrect INTEGER NOT NULL DEFAULT 0,
            is_learned INTEGER NOT NULL DEFAULT 0,
            FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
        )
    """)
    cursor.execute("CREATE UNIQUE INDEX index_learning_progress_word_id ON learning_progress(word_id)")
    cursor.execute("CREATE INDEX index_learning_progress_next_review_date ON learning_progress(next_review_date)")

    # wrong_answers 테이블
    cursor.execute("""
        CREATE TABLE wrong_answers (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            word_id INTEGER NOT NULL,
            wrong_answer TEXT NOT NULL,
            correct_answer TEXT NOT NULL,
            quiz_type TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
        )
    """)
    cursor.execute("CREATE INDEX index_wrong_answers_word_id ON wrong_answers(word_id)")

    # idioms 테이블 (숙어/구동사)
    cursor.execute("""
        CREATE TABLE idioms (
            id INTEGER PRIMARY KEY NOT NULL,
            phrase TEXT NOT NULL,
            meaning TEXT NOT NULL,
            meaning_type TEXT NOT NULL DEFAULT 'en',
            type TEXT NOT NULL DEFAULT 'idiom',
            example_en TEXT NOT NULL DEFAULT '',
            example_ko TEXT NOT NULL DEFAULT '',
            difficulty INTEGER NOT NULL DEFAULT 3,
            category TEXT NOT NULL DEFAULT 'daily'
        )
    """)
    cursor.execute("CREATE INDEX index_idioms_type ON idioms(type)")
    cursor.execute("CREATE INDEX index_idioms_category ON idioms(category)")
    cursor.execute("CREATE INDEX index_idioms_phrase ON idioms(phrase)")


def set_room_metadata(conn):
    """Room 호환을 위한 메타데이터 설정"""
    cursor = conn.cursor()

    # Room DB 버전 설정
    cursor.execute(f"PRAGMA user_version = {ROOM_DB_VERSION}")

    # Room identity hash 테이블
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS room_master_table (
            id INTEGER PRIMARY KEY,
            identity_hash TEXT
        )
    """)
    cursor.execute(
        "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, ?)",
        (ROOM_IDENTITY_HASH,)
    )

    conn.commit()
    print(f"  Room 메타데이터: version={ROOM_DB_VERSION}, hash={ROOM_IDENTITY_HASH}")


def insert_edu_words(cursor):
    """교육부 3000 단어 삽입"""
    if not os.path.exists(EDU_XLS_PATH):
        print("  WARNING: edu_3000.xls 파일 없음 - 교육부 단어 스킵")
        return 0

    wb = xlrd.open_workbook(EDU_XLS_PATH)
    sheet = wb.sheet_by_index(0)

    inserted = 0
    for r in range(1, sheet.nrows):
        word = sheet.cell_value(r, 1).strip()
        meaning = sheet.cell_value(r, 2).strip()
        level = sheet.cell_value(r, 3).strip()
        var1 = sheet.cell_value(r, 4).strip()
        var2 = sheet.cell_value(r, 5).strip()

        if not word or not meaning:
            continue

        cursor.execute("""
            INSERT INTO edu_words (id, word, meaning, level, part_of_speech, variant1, variant2)
            VALUES (?, ?, ?, ?, '', ?, ?)
        """, (r, word, meaning, level, var1, var2))
        inserted += 1

    return inserted


def build_database(target_count=20000):
    print("=" * 60)
    print("외부 소스 기반 단어 DB 생성 (v5 스키마)")
    print("=" * 60)

    # 1. 외부 소스 로드
    print("\n[1/6] 소스 로드 중...")
    muse = load_muse()
    print(f"  MUSE: {len(muse)} 영어 단어")
    kengdic = load_kengdic()
    print(f"  kengdic: {len(kengdic)} 영어 단어")

    # 2. wordfreq에서 영어 단어 가져오기
    print("\n[2/6] wordfreq에서 영어 단어 추출 중...")
    raw_words = top_n_list('en', target_count + 5000)
    filtered = []
    seen = set()
    for w in raw_words:
        wl = w.lower()
        if len(wl) > 1 and wl.isalpha() and wl not in seen:
            seen.add(wl)
            filtered.append(wl)
        if len(filtered) >= target_count:
            break
    print(f"  필터 후: {len(filtered)} 단어")

    # 3. 한국어 뜻 매칭
    print("\n[3/6] 한국어 뜻 매칭 중...")
    matched = []
    unmatched = []
    for word in filtered:
        zipf = zipf_frequency(word, 'en')
        stage = get_stage(zipf)
        difficulty = min(stage, 5)

        if word in FUNCTION_WORDS:
            meaning, meaning_type, pos = FUNCTION_WORDS[word]
        else:
            meaning, meaning_type = get_meaning(word, muse, kengdic)
            pos = guess_pos(word)
            if meaning is None:
                unmatched.append({'word': word, 'stage': stage, 'zipf': zipf})
                continue

        domain = classify_domain(word)
        matched.append({
            'word': word, 'meaning': meaning, 'meaning_type': meaning_type,
            'pos': pos, 'domain': domain, 'stage': stage, 'difficulty': difficulty,
        })

    print(f"  매칭 성공: {len(matched)} ({len(matched) / len(filtered) * 100:.1f}%)")
    print(f"  미매칭: {len(unmatched)} ({len(unmatched) / len(filtered) * 100:.1f}%)")

    with open(UNMATCHED_PATH, 'w', encoding='utf-8') as f:
        json.dump(unmatched, f, ensure_ascii=False, indent=1)

    # 4. DB 생성
    print("\n[4/6] 새 DB 생성 중...")
    if os.path.exists(DB_PATH):
        backup = DB_PATH + ".backup"
        if os.path.exists(backup):
            os.remove(backup)
        os.rename(DB_PATH, backup)
        print(f"  기존 DB 백업: {backup}")

    os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    create_schema(cursor)
    conn.commit()

    # 5. 단어 데이터 삽입
    print("\n[5/6] 데이터 삽입 중...")
    word_id = 1
    stage_counts = {}

    for entry in matched:
        cursor.execute("""
            INSERT INTO words (id, word, pronunciation, meaning, meaning_type, part_of_speech,
                example_en, example_ko, stage, domain, frequency_rank, difficulty)
            VALUES (?, ?, '', ?, ?, ?, '', '', ?, ?, ?, ?)
        """, (
            word_id, entry['word'], entry['meaning'], entry['meaning_type'],
            entry['pos'], entry['stage'], entry['domain'], word_id, entry['difficulty'],
        ))
        stage_counts[entry['stage']] = stage_counts.get(entry['stage'], 0) + 1
        word_id += 1

    # 교육부 3000 삽입
    edu_count = insert_edu_words(cursor)
    conn.commit()

    # 6. Room 메타데이터 설정
    print("\n[6/6] Room 메타데이터 설정 중...")
    set_room_metadata(conn)

    # 통계 출력
    cursor.execute("SELECT COUNT(*) FROM words")
    total_words = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM edu_words")
    total_edu = cursor.fetchone()[0]

    print(f"\n{'=' * 60}")
    print(f"DB 생성 완료!")
    print(f"{'=' * 60}")
    print(f"메인 단어: {total_words}개")
    print(f"교육부 단어: {total_edu}개")

    stage_labels = {
        1: "Foundation (A1-A2)", 2: "Intermediate (B1)",
        3: "Upper-Intermediate (B2)", 4: "Advanced (C1)",
        5: "Proficient (C2)", 6: "Near-Native",
    }
    print(f"\n단계별:")
    for s in sorted(stage_counts.keys()):
        if stage_counts[s] > 0:
            print(f"  Stage {s} - {stage_labels.get(s, '')}: {stage_counts[s]}개")

    cursor.execute("SELECT domain, COUNT(*) FROM words GROUP BY domain ORDER BY COUNT(*) DESC LIMIT 15")
    print(f"\n도메인별:")
    for domain, count in cursor.fetchall():
        print(f"  {domain}: {count}개")

    print(f"\nRoom version: {ROOM_DB_VERSION}")
    print(f"Room hash: {ROOM_IDENTITY_HASH}")
    print(f"DB 크기: {os.path.getsize(DB_PATH) / 1024:.0f} KB")
    print(f"미매칭 단어: {len(unmatched)}개 → {UNMATCHED_PATH}")

    conn.close()


if __name__ == "__main__":
    build_database(20000)
