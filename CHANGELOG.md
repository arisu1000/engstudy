# Changelog

All notable changes to this project will be documented in this file.
Format based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

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
- **EduFlashcardScreen**, **EduQuizScreen**: 교육부 단어 전용 플래시카드/퀴즈 화면 추가
- 커스텀 브랜드 컬러 테마 (Dynamic Color 미사용)
  - Light: Indigo blue + Deep orange + Teal
  - Dark: Navy black + Lavender + Salmon accents
  - Stage별 전용 색상: 초록(1)→파랑(2)→보라(3)→주황(4)→핑크(5)→금색(6)

### Changed
- **총 콘텐츠**: 21,268개 (단어 12,068 + 교육부 3,000 + 숙어 1,092 + 예문 5,108)
- **Room DB version**: 1 → **7** (릴리즈 전 정식 마이그레이션 필요)
- Room identity hash: `90d07bfa248b01c3a5cbc93c5655b8b4`
- 화면 수: 25개
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
