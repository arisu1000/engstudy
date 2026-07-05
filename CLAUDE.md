# EngStudy - AI 에이전트 컨텍스트

## 프로젝트 개요
한국어 사용자를 위한 오프라인 영어 단어장 Android 앱. 21,268개 콘텐츠(단어·관용구·예문)를 앱 내 SQLite DB에 내장하고, Stage 기반 학습, 교육부 필수 단어, 숙어/구동사, 문법 예문, SM-2 간격반복, 일일 챌린지, 배지/게임화, 홈 위젯 등을 제공한다.

모든 데이터 소스는 상업적 이용 가능 라이선스로 검증 완료.

## 빌드 & 테스트 명령어

```bash
# 디버그 빌드
./gradlew assembleDebug

# Kotlin 컴파일만
./gradlew compileDebugKotlin

# 유닛 테스트
./gradlew testDebugUnitTest

# 전체 테스트 리포트
# app/build/reports/tests/testDebugUnitTest/index.html
```

## 기술 스택

| 항목 | 선택 |
|------|------|
| 언어 | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| 아키텍처 | MVVM + Clean Architecture |
| DB | Room (pre-populated from assets), version 12 |
| DI | Hilt |
| Navigation | Compose Navigation (type-safe routes) |
| 비동기 | Coroutines + Flow |
| TTS | Android 내장 TextToSpeech |
| 설정 | DataStore Preferences |
| Min SDK | 26 / Target SDK 35 |

## 프로젝트 구조

```
com.wcjung.engstudy/
├── data/
│   ├── local/          # Room DB, DAOs, Entities (WordEntity, EduWordEntity, IdiomEntity,
│   │                   # ExampleSentenceEntity, KnownItemEntity 등)
│   ├── repository/     # Repository 구현체 (EduWordRepository, IdiomRepository, SentenceRepository 포함)
│   └── datastore/      # DataStore UserPreferences
├── domain/
│   ├── model/          # Domain 모델 (Word, EduWord, EduLevel, Idiom, IdiomType,
│   │                   # ExampleSentence, GrammarLevel 등)
│   ├── repository/     # Repository 인터페이스
│   └── usecase/        # SM-2 알고리즘 등 UseCase
├── ui/
│   ├── navigation/     # NavGraph, Screen routes
│   ├── theme/          # Material 3 테마 (커스텀 브랜드 컬러, Dynamic Color 미사용)
│   ├── components/     # 공유 컴포넌트 (WordCard, EduWordCard, DomainChip, ComboEffect, LevelUpEffect)
│   └── screen/         # 26개 화면 (아래 목록 참조)
├── util/               # TtsManager, NotificationHelper, Receivers
└── di/                 # Hilt 모듈 (App, Database, Repository)
```

### 화면 목록 (28개)

| 화면 | 설명 |
|------|------|
| home | 홈 (오늘의 단어, Stage 요약, 일일 챌린지 진입) |
| study | 단어 학습 (Stage 선택) |
| flashcard | 플래시카드 |
| quiz | 4지선다 퀴즈 (`listening = true`면 듣기 퀴즈: TTS 듣고 뜻 고르기) |
| spelling | 스펠링 퀴즈 |
| review | SM-2 복습 |
| wordlist | 단어 목록 |
| worddetail | 단어 상세 |
| bookmarks | 북마크 (탭: 단어 / 교육부) |
| search | 검색 (탭: 단어 / 교육부) |
| statistics | 학습 통계 + 리포트 공유 |
| settings | 설정 |
| profile | 프로필 |
| eduhome | 교육부 단어 홈 (레벨별 플래시카드/퀴즈/스펠링/듣기 진입) |
| eduwordlist | 교육부 단어 목록 (북마크, 다중 선택 완전 제외 지원) |
| eduflashcard | 교육부 플래시카드 |
| eduquiz | 교육부 퀴즈 (`listening = true`면 듣기 퀴즈) |
| eduspellingquiz | 교육부 스펠링 퀴즈 |
| wronganswers | 오답 노트 |
| idiomhome | 숙어/구동사 홈 |
| idiomlist | 숙어/구동사 목록 |
| idiomquiz | 숙어/구동사 퀴즈 |
| grammarhome | 문법 예문 홈 |
| grammarlist | 문법 예문 목록 |
| dailychallenge | 일일 챌린지 |
| placementtest | 배치 테스트 |
| excludedwords | 제외된 단어 관리 (탭: 기본 단어 / 교육부 단어, 복원 가능) |
| addword | 사용자 단어 추가 (단어 목록 "+" / 검색 결과 없음에서 진입, 사전 링크 제공) |

## DB 전략

- `assets/databases/engstudy.db`에 사전 생성된 SQLite DB 탑재
- `Room.databaseBuilder().createFromAsset()` 사용
- DB version 12 / 명시적 마이그레이션(4→12)으로 업그레이드 처리 (사용자 데이터 보존)
- Room identity hash: 스키마 JSON `app/schemas/12.json` 참조 (asset DB의 room_master_table과 build_word_db.py의 ROOM_IDENTITY_HASH도 함께 갱신할 것)
- DB 생성 스크립트: `scripts/build_word_db.py` — kengdic + wordfreq + 교육부 xls
  (과거 일회성 스크립트는 `scripts/archive/` 참조)
- 다중 의미/예문 생성: `scripts/build_meanings.py`, `scripts/build_examples.py`, `scripts/build_examples_llm.py`
- 대표 뜻 보정 (kengdic이 가나다순 정렬이라 희귀 의미가 먼저 노출되는 문제):
  - `scripts/apply_edu_meanings.py` — 교육부와 겹치는 단어는 교육부 검수 뜻 사용 (멱등).
    기능어 56개(FUNCTION_WORDS)는 제외하고 수동 큐레이션 뜻을 보호·복원한다
  - `scripts/build_meanings_llm.py` — 나머지 단어는 로컬 LLM(Ollama gemma4)으로 재생성.
    `--gate`(교육부 뜻 100개와 품질 대조), `--apply`(저장된 jsonl 재적용),
    `--edu-merge`(교육부 뜻이 덜 흔한 의미인 단어를 LLM 교차 검증으로 보정, 예: just→"올바른"),
    `--selftest`(겹침 판정 회귀 테스트) 모드 지원.
    동의어 불일치로 잘못 교체되는 단어는 `EDU_MERGE_KEEP`에 수동 등록.
    파이프라인 순서: build_word_db → build_meanings → apply_edu_meanings → build_meanings_llm

### 스키마 요약 (13개 테이블, DB v12)

**`words` 테이블** — 12,068개 (kengdic MPL 2.0 + Free Dictionary API)
- `stage` INT (1-6): 빈도 기반 학습 단계 (1=최고빈도 ~ 6=저빈도)
- `meaning` TEXT: 단어 의미 — 교육부 겹침 2,772개는 교차 검증 병합(교육부 뜻 유지 1,965 / LLM 뜻 교체 807), 나머지 9,240개는 로컬 LLM(gemma4) 재생성 뜻, 기능어 56개는 수동 큐레이션
- `meaning_type` TEXT: 의미 언어 구분 (`'ko'` 또는 `'en'`)
- **사용자 추가 단어**: id >= 1,000,000 예약 구간(`UserWordDao.USER_WORD_ID_START`)은 사용자가
  직접 추가한 단어(사용자 데이터). 별도 테이블 없이 같은 테이블을 쓰므로 목록·검색·퀴즈·복습·
  북마크·오답이 자동 통합됨. `frequency_rank = 0`(목록·학습 최우선 노출), `domain = 'GENERAL'`.
  콘텐츠 갱신(`RefreshWordContentMigration`)은 asset id(< 1M) 기준 UPDATE라 유실되지 않음.
  일일 챌린지 쿼리는 기기 간 동일 세트 보장을 위해 이 구간을 제외. 백업/복원 대상 포함.
  build_word_db.py는 콘텐츠 id가 이 구간에 도달하면 assert로 실패한다

**`edu_words` 테이블** — 3,000개 (교육부 공공데이터)
- 초중고 교육과정 필수 영단어
- `edu_level`: `EduLevel` enum (`초등` 800개, `중고` 1,800개, `전문` 400개)

**`idioms` 테이블** — 1,092개 (Semigradsky MIT)
- 영어 숙어(idiom)와 구동사(phrasal_verb)
- `type`: `'idiom'` 또는 `'phrasal_verb'`
- `meaning_type`: `'en'` 또는 `'ko'`
- `category`: 분류 (`'daily'` 등)

**`example_sentences` 테이블** — 5,108개 (Tatoeba CC BY 2.0 FR)
- 문법 주제별 영한 예문
- `grammar_topic`: 영문 문법 주제, `grammar_topic_ko`: 한국어 주제명
- `level`: 난이도 (`'초급'`, `'중급'`, `'고급'`)

**`learning_progress` 테이블**
- SM-2 간격반복 진행 상태 (ease factor, interval, next review date 등)
- `is_excluded` BOOLEAN (v8 추가): 단어 완전 제외 플래그 — 제외된 단어는 학습/퀴즈/복습에서 제외됨

**`bookmarks` 테이블**
- 사용자 북마크 단어

**`wrong_answers` 테이블** (v5 추가)
- 퀴즈/스펠링에서 오답 기록 저장
- `word_id`, `wrong_answer`, `correct_answer`, `quiz_type`, `created_at`

**`known_items` 테이블** (v7 추가)
- edu_words, idioms 등에 대한 "이미 알아요" 범용 추적
- `item_id` + `item_type`(unique): `'edu_word'`, `'idiom'` 등
- `marked_at`: 마킹 시점 timestamp

**`word_meanings` 테이블** (v9 추가)
- words 테이블의 다중 의미를 별도 저장 (WordNet 빈도 기반 정렬)
- `word_id` FK → words.id, `sense_order` INT (0=가장 흔한 의미)
- `pos`: 품사 문자열 (noun/verb/adjective/adverb)
- `meaning_type`: `'ko'` (한국어)
- 생성: `scripts/build_meanings.py` (kengdic + NLTK WordNet)

**`user_word_meanings` 테이블** (v11 추가) — 사용자 데이터
- 사용자가 직접 추가("내 의미")하거나 변경(대표 뜻 교체, `is_primary=1`)한 단어 의미
- 콘텐츠 테이블(words/word_meanings)은 콘텐츠 마이그레이션 때 교체되므로 사용자 편집본은 반드시 이 테이블에 저장
- 대표 뜻 override는 WordRepositoryImpl/LearningRepositoryImpl에서 조회 시점에 합성되어 목록·퀴즈·복습 전역에 반영
- 백업/복원 대상 포함

**`word_examples` 테이블** (v9 추가) — 15,030개, 커버리지 99.8% (12,041/12,068 단어)
- words 테이블의 단어별 예문 저장 (단어당 최대 3개)
- `word_id` FK → words.id, `source`: `'tatoeba'`(5,831) 또는 `'llm'`(9,199)
- 생성 Phase 1: `scripts/build_examples.py` (Tatoeba, LLM 0토큰)
- 생성 Phase 2: `scripts/build_examples_ollama.py` (로컬 Ollama gemma4, 미커버 단어만.
  `--apply` 재적용 / `--gate` 샘플 검사 / `--selftest` 지원. Claude Batch API 버전은 build_examples_llm.py)

**`edu_bookmarks` 테이블** (v12 추가) — 사용자 데이터
- 교육부 단어 전용 북마크. `edu_word_id` FK → edu_words.id (unique)
- 기본 단어장의 `bookmarks` 테이블과 완전히 독립 (겹치는 단어라도 서로 영향 없음)
- DAO 직접 주입 방식 사용(리포지토리 계층 없음) — `known_items`와 동일한 컨벤션

**`edu_excluded_words` 테이블** (v12 추가) — 사용자 데이터
- 교육부 단어 전용 완전 제외. `edu_word_id` FK → edu_words.id (unique)
- 기본 단어장의 `learning_progress.is_excluded`와 독립적으로 동작
- 제외된 단어는 `EduWordList`뿐 아니라 `EduFlashCard`/`EduQuiz`/`EduSpellingQuiz`의 출제 풀에서도 제외됨 (각 ViewModel이 로드 시점에 필터링)

### 데이터 소스 라이선스

| 소스 | 라이선스 | 용도 |
|------|---------|------|
| kengdic | MPL 2.0 | 주요 단어 사전 |
| Free Dictionary API | Free | 단어 보충 |
| Tatoeba | CC BY 2.0 FR | 문법 예문 (저작자 표시 필요) |
| 교육부 공공데이터 | 정부 공공저작물 | 교육부 단어 |
| Semigradsky/phrasal-verbs | MIT | 숙어/구동사 |
| wordfreq | MIT | 빈도 점수 기반 Stage 분류 |

## 핵심 알고리즘

### SM-2 간격반복: `CalculateSpacedRepetitionUseCase`
- quality 0-5, ease factor 최소 1.3
- 간소화 4단계: Again(1), Hard(3), Good(4), Easy(5)
- interval >= 21일이면 "학습 완료"
- 퀴즈 응답의 기록(진도+오답+스트릭)은 `RecordQuizAnswerUseCase`로 일원화 — 일반/듣기/스펠링/교육부 퀴즈 공용
  (quizType: `quiz`/`listening_quiz`/`spelling`/`edu_quiz`/`edu_listening_quiz`/`edu_spelling`)
- 교육부 퀴즈·스펠링도 SM-2 대상: words 테이블의 동일 단어(id 매핑, `getWordIdByText`)에 진도 기록.
  words에 없는 교육부 단어(~8%)는 복습 추적 제외

### 일일 챌린지: `GetDailyChallengeUseCase`
- 날짜(epoch day)를 시드로 사용해 결정적 의사난수로 10개 단어 선택
- 서버 없이 모든 기기에서 같은 날 같은 단어가 나옴 → 가족 간 점수 경쟁 가능
- `WordDao.getDailyChallengeWords()`: `ORDER BY (id * :seed) % 99991` SQL로 구현

### 배치 테스트: `PlacementTestScreen`
- 초기 레벨 평가로 학습 시작 Stage를 자동 추천, `recommendedStage`로 저장
- 테스트 완료 시 학습 화면(`StudyScreen`)의 단계 선택 기본값으로 추천 Stage가 반영됨 (`StudyViewModel.initialSelectedStage`)

## 주요 UI/UX 기능

### 콤보 시스템
- Quiz, SpellingQuiz, EduQuiz, DailyChallenge 모든 퀴즈 화면에 적용
- `comboCount` / `maxCombo` StateFlow로 추적
- `ComboEffect` 컴포넌트: 3/5/10 콤보에서 단계별 애니메이션 표시
- `LevelUpEffect` 컴포넌트: Stage 완료 시 confetti 파티클 축하 오버레이

### 게임화 (Gamification)
- 12종 배지 + 연속 학습 스트릭(streak) 추적
- 레벨업 축하 이펙트

### 홈 위젯
- 오늘의 단어를 홈 화면 위젯으로 표시

### 학습 리포트 공유
- `StatisticsViewModel.generateReport()`: 학습 통계를 이모지 포함 텍스트로 포맷
- `Intent.ACTION_SEND`로 카카오톡/메시지 등 모든 앱과 공유 가능

### 학습 데이터 백업/복원
- 설정 → "학습 데이터" — SAF로 JSON 파일 내보내기/가져오기 (`BackupManager`, `BackupDao`)
- 대상: learning_progress, bookmarks, wrong_answers, known_items, user_word_meanings,
  사용자 추가 단어(words의 id >= 1M 구간) + DataStore 설정
- 콘텐츠 테이블은 백업하지 않음 (앱 내장). 복원 시 현재 DB에 없는 word_id 행은 FK 보호를 위해 건너뜀
  (사용자 단어를 참조하는 행은 백업 파일 안의 사용자 단어 id 기준으로 검증)
- 백업 포맷 버전(`BackupManager.FORMAT_VERSION`) — 필드 추가 시 하위 호환 유지 (`ignoreUnknownKeys`)

### 단어 완전 제외 & 복원
- `WordListScreen`/`EduWordListScreen`에서 다중 선택 후 "완전 제외" 가능
- 기본 단어: `learning_progress.is_excluded = true`로 마킹 → 학습/퀴즈/복습 전 영역에서 제외
- 교육부 단어: `edu_excluded_words` 테이블에 별도 기록 → `EduWordList`/`EduFlashCard`/`EduQuiz`/`EduSpellingQuiz` 전 영역에서 제외
- `ExcludedWordsScreen`(프로필 → 제외된 단어)에서 탭(기본 단어 / 교육부 단어)으로 구분해 확인 및 개별 복원

### 사용자 단어 추가
- `AddWordScreen`(단어 목록 상단 "+" / 검색 결과 없음 → "직접 추가") — 단어·뜻 필수,
  발음·예문 선택, 학습 단계 선택. 입력 중 네이버/Cambridge 사전 링크로 뜻 확인 가능
- 저장은 `UserWordDao`(리포지토리 없이 직접 주입 — known_items 컨벤션)가 words 테이블의
  id 예약 구간(>= 1,000,000)에 트랜잭션으로 id를 발급해 삽입. 중복(대소문자 무시)은 거부하고
  기존 단어 상세로 안내
- 단어 상세에서 사용자 단어(id로 판별)는 "내가 추가한 단어" 표시 + 삭제 가능
  (learning_progress·bookmarks·wrong_answers·user_word_meanings는 FK CASCADE로 함께 삭제)

### 교육부 단어 부가 기능 (기본 단어장과 기능 동등)
- **북마크**: `edu_bookmarks` 테이블(독립), `EduWordCard`(`ui/components/EduWordCard.kt`)의 북마크 아이콘으로 토글. 프로필 → 즐겨찾기 → "교육부" 탭에서 확인
- **검색**: `SearchScreen`에 "단어"/"교육부" 탭 추가, `EduWordRepository.searchWords()`(기존엔 미사용 dead code였음)를 연결
- **스펠링 퀴즈**: `EduSpellingQuizScreen`/`ViewModel` — `SpellingQuizViewModel`과 동일 패턴(뜻 보고 철자 입력)
- **듣기 퀴즈**: `EduQuizScreen`에서 `Screen.EduQuiz.listening = true`로 진입 시 뜻을 숨기고 TTS 발음만으로 정답(영단어)을 고르는 모드

### 브랜드 컬러 (Dynamic Color 미사용)
- Light: Indigo blue + Deep orange + Teal
- Dark: Navy black + Lavender + Salmon accents
- Stage별 전용 색상: 초록→파랑→보라→주황→핑크→금색

## TODO

- [x] **단어 뜻 LLM 재생성**: 9,240개 단어의 대표 뜻을 로컬 LLM(Ollama gemma4)으로 재생성 완료 (`scripts/build_meanings_llm.py`)
- [x] **단어 예문 LLM 보완**: `scripts/build_examples_ollama.py`(로컬 gemma4)로 9,199개 생성 완료
  — 커버리지 23.5% → 99.8% (12,041/12,068). 잔여 27개는 생성 실패(무효 응답) 단어
- [x] DB v9까지 정식 마이그레이션(4→9) 작성 및 `fallbackToDestructiveMigration` 제거 완료 (업그레이드 시 사용자 데이터 보존)
- [x] Tatoeba 저작자 표시 (CC BY 2.0 FR) — 설정 → "데이터 출처 및 라이선스" 화면(`LicensesScreen`)

## 주의사항

- 오프라인 전용 앱 — 네트워크 통신 없음
- Room DB version 12 — 업그레이드는 명시적 Migration(4→12)으로 처리하며 사용자 데이터를 보존한다. v13+ 추가 시 반드시 대응 Migration을 `DatabaseModule.addMigrations`에 등록할 것 (누락 시 fail-loud). 스키마 변경 시 asset DB의 user_version·room_master_table 해시와 build_word_db.py 상수도 함께 갱신 (단, v12처럼 순수 사용자 데이터 테이블만 추가하는 경우 asset DB 콘텐츠 자체는 재생성 불필요 — `RefreshWordContentMigration`과 무관)
- 콘텐츠(단어 뜻 등)만 갱신하는 릴리즈: `RefreshWordContentMigration`(9→10) 패턴 참조 — asset DB를 임시 복사해 콘텐츠 테이블만 교체. words는 FK CASCADE(wrong_answers 등) 때문에 DELETE 금지, id 기준 UPDATE만 사용. asset DB의 user_version과 room_master_table identity hash를 새 버전에 맞게 스탬프해야 신규 설치가 크래시하지 않음
- Tatoeba 예문 사용 시 저작자 표시(CC BY 2.0 FR) 필요 — 설정 → 라이선스 화면(`LicensesScreen`)에 표기됨. 데이터 소스 추가 시 이 화면의 `dataSources` 목록도 갱신할 것
- `@Serializable` 사용을 위해 kotlin-serialization 플러그인 필요
- `scripts/build_word_db.py` 실행 전 `wordfreq`, `kengdic` Python 의존성 설치 필요
