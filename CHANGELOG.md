# Changelog

All notable changes to this project will be documented in this file.
Format based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased] - 2026-07-04

### Added
- **DB v10 콘텐츠 갱신 마이그레이션** (`RefreshWordContentMigration`): 기존 설치 사용자에게
  개선된 단어 뜻을 전달하면서 학습 데이터는 보존
  - APK 내장 asset DB를 임시 파일로 복사해 `words`(id 기준 UPDATE)와 `word_meanings`(전체 교체)만 갱신
  - `learning_progress`/`bookmarks`/`wrong_answers`/`known_items` 보존 —
    words는 FK CASCADE 연쇄 삭제 방지를 위해 DELETE 없이 UPDATE만 수행
  - asset DB의 `user_version`(10)과 `room_master_table` identity hash를 현행 스키마로 스탬프
    (신규 설치는 마이그레이션 없이 asset을 그대로 사용)
  - 회귀 테스트: `RefreshWordContentMigrationTest`(androidTest) — 뜻 갱신 + 사용자 데이터 보존 검증,
    에뮬레이터에서 업그레이드/신규 설치 두 경로 모두 확인

## [Unreleased] - 2026-07-03

### Fixed
- **단어 대표 뜻 품질 개선**: kengdic.tsv가 가나다순 정렬이라 희귀 의미가 먼저 노출되던 문제 보정
  (예: white → "공백의, 눈이 많은" → "흰")
  - `scripts/apply_edu_meanings.py` 신규: 교육부 `edu_words`와 겹치는 2,736개 단어의
    `words.meaning`을 교육부 검수 뜻으로 교체, `word_meanings`의 sense_order 0에 삽입 (source='edu')
  - `scripts/build_meanings_llm.py` 신규: 나머지 9,240개 단어를 로컬 LLM(Ollama gemma4)으로
    재생성 — "가장 흔한 뜻 1~3개 + 품사"를 `words.meaning`과 `word_meanings`(source='llm')에 적용,
    기존 kengdic 의미는 뒤 순서로 보존 (최대 5개)
  - 품질 게이트: 교육부 검수 뜻 100개 샘플과 대조 후 전체 실행 (실질 오류 ~1%)
  - 숫자·약어 15개(tv, ai, forty 등)는 스크립트 내 `MANUAL_SENSES` 수동 뜻으로 처리
  - 생성 결과는 `scripts/meanings_llm_results.jsonl`로 보존 — `--apply`로 재생성 없이 재적용 가능

## [Unreleased] - 2026-04-06

### Added
- **다중 의미 테이블** (`word_meanings`, DB v9): WordNet 빈도 기반 의미 순서 정렬
  - `WordMeaningEntity`, `WordMeaningDao`, `WordMeaning` 도메인 모델
  - `WordRepository.getMeaningsForWord()` / `WordRepositoryImpl` 구현
  - `WordDetailScreen`: 의미별 품사(POS) 태그 표시, 빈도 순서로 나열
  - 폴백: `word_meanings`가 없으면 기존 `meaning` 필드 표시
- **단어별 예문 테이블** (`word_examples`, DB v9): Tatoeba + Claude Haiku 보완
  - `WordExampleEntity`, `WordExampleDao`, `WordExample` 도메인 모델
  - `WordRepository.getExamplesForWord()` / `WordRepositoryImpl` 구현
  - `WordDetailScreen`: 예문 최대 3개 표시 (출처별: tatoeba/llm)
  - 폴백: `word_examples`가 없으면 기존 `example_en`/`example_ko` 표시
- **DB 생성 스크립트 2종**
  - `scripts/build_meanings.py`: kengdic + WordNet(NLTK)으로 word_meanings 채우기
  - `scripts/build_examples.py`: Tatoeba 매칭으로 word_examples 채우기 (LLM 0토큰)
  - `scripts/build_examples_llm.py`: Anthropic Batch API로 미커버 단어 보완

## [Unreleased] - 2026-04-04

### Added
- **숙어 & 구동사** (`idioms` 테이블, v6): MIT 라이선스 데이터셋 1,092개 내장
  - `IdiomHomeScreen`, `IdiomListScreen`, `IdiomQuizScreen` 3개 화면
  - `IdiomEntity`, `IdiomDao`, `Idiom` 도메인 모델, `IdiomRepository`
  - `type`: `'idiom'` / `'phrasal_verb'` 구분
  - "이미 알아요" 마킹 지원 (`known_items` 테이블 연동)
- **문법 예문** (`example_sentences` 테이블, v7): Tatoeba CC BY 2.0 FR 5,108개
  - `GrammarHomeScreen`, `GrammarListScreen` 2개 화면
  - `grammar_topic` / `grammar_topic_ko` / `level` (초급/중급/고급) 컬럼
  - `ExampleSentenceEntity`, `SentenceRepository`
- **일일 챌린지** (`DailyChallengeScreen`): 날짜(epoch day) 시드 기반 10개 단어 결정적 출제
  - `GetDailyChallengeUseCase`: 서버 없이 모든 기기에서 동일 단어 → 가족 간 경쟁 가능
  - `WordDao.getDailyChallengeWords()`: `ORDER BY (id * :seed) % 99991` SQL
  - 결과 화면에서 `Intent.ACTION_SEND`로 점수 공유
- **배치 테스트** (`PlacementTestScreen`): 초기 레벨 평가로 학습 시작 Stage 자동 추천
- **오답 노트** (`WrongAnswersScreen`, v5): 퀴즈/스펠링 오답 자동 수집 및 재학습
  - `wrong_answers` 테이블: `word_id`, `wrong_answer`, `correct_answer`, `quiz_type`, `created_at`
- **`known_items` 테이블** (v7): edu_words, idioms "이미 알아요" 범용 추적
  - `item_id` + `item_type`(unique): `'edu_word'`, `'idiom'`
- **콤보 시스템**: Quiz, SpellingQuiz, EduQuiz, DailyChallenge 전 화면 적용
  - `ComboEffect` 컴포넌트: 3/5/10 연속 정답 시 단계별 애니메이션
  - `LevelUpEffect` 컴포넌트: Stage 완료 시 confetti 파티클 축하 오버레이
- **게임화 (Gamification)**: 12종 배지 + 연속 학습 스트릭(streak) 추적
- **학습 이력 캘린더**: GitHub contribution 스타일 날짜별 학습 기록 시각화
- **홈 위젯**: 오늘의 단어를 Android 홈 화면 위젯으로 표시
- **학습 리포트 공유**: `StatisticsViewModel.generateReport()`로 통계 텍스트 생성 → 카카오톡 등 공유
- **북마크 내보내기/공유**: 북마크 목록 `Intent.ACTION_SEND`로 공유
- **다크모드 3단계**: 시스템/라이트/다크 직접 선택 (DataStore 저장)
- **일일 학습 목표** 설정 및 홈 화면 진행률 표시
- **"이미 알아요" 마킹**: words, edu_words, idioms 개별 건너뛰기 지원
  - `WordListScreen`에서 다중 선택 후 일괄 "이미 알아요" 마킹 가능
- **단어 완전 제외 & 복원** (`ExcludedWordsScreen`, v8)
  - `learning_progress.is_excluded` 컬럼으로 학습/퀴즈/복습 전 영역에서 제외
  - 프로필 → "제외된 단어"에서 제외 목록 확인 및 개별 복원
- **EduFlashcardScreen**, **EduQuizScreen**: 교육부 단어 전용 플래시카드/퀴즈 화면 추가
- 커스텀 브랜드 컬러 테마 (Dynamic Color 미사용)
  - Light: Indigo blue + Deep orange + Teal
  - Dark: Navy black + Lavender + Salmon accents
  - Stage별 전용 색상: 초록(1)→파랑(2)→보라(3)→주황(4)→핑크(5)→금색(6)

### Changed
- **총 콘텐츠**: 21,268개 (단어 12,068 + 교육부 3,000 + 숙어 1,092 + 예문 5,108)
- **Room DB version**: 1 → **8** (릴리즈 전 정식 마이그레이션 필요)
- Room identity hash: `90d07bfa248b01c3a5cbc93c5655b8b4`
- 화면 수: 26개 (`ExcludedWordsScreen` 추가)
- Stage 기반 학습 체계로 전환 (AgeGroup/Domain 분류 폐기)
- `words` 스키마: `meaning` + `meaning_type` + `stage` 컬럼 구성
- 데이터 생성 스크립트: `build_word_db.py` 단일 스크립트 (kengdic + Free Dictionary API)

### Removed
- `AgeGroup` 도메인 모델 및 관련 UI 컴포넌트
- 수동 작성 단어 생성 스크립트 (`generate_word_db.py`, `expand_words.py`)

---

## [1.0.0] - 2026-04-04

### Added
- 프로젝트 초기 구조 (Kotlin 2.1, Jetpack Compose, Material 3, Hilt, Room)
- 5,000+ 영어 단어 내장 DB (12개 분야, 5단계 수준)
- 홈 화면 (오늘의 단어, 학습 요약 통계)
- 단어 목록 화면 (분야/수준 필터링, 빈도순 정렬)
- 단어 상세 화면 (IPA 발음, 예문, TTS 재생)
- 플래시카드 학습 (카드 뒤집기, 4단계 자가 평가)
- 4지선다 퀴즈 (영→한, 한→영 교차 출제)
- 스펠링 퀴즈 (한국어 뜻 보고 영어 입력)
- SM-2 간격반복 복습 시스템
- 복습 화면 (기한 도래 단어 자동 로드)
- 북마크 (즐겨찾기)
- 검색 (영어/한국어 양방향, 300ms debounce)
- 학습 통계 (전체 단어 수, 학습 완료, 진행 중, 분야별 현황)
- 프로필 화면 (통계/북마크/검색/설정 네비게이션)
- 설정 (다크모드, TTS 속도, 일일 목표, 알림 토글)
- 학습 리마인더 알림 (AlarmManager + NotificationCompat)
- 부팅 시 알림 재등록 (BootReceiver)
- SM-2 알고리즘 단위 테스트
- Domain/AgeGroup 모델 단위 테스트
