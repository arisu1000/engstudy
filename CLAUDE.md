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
| DB | Room (pre-populated from assets), version 7 |
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
│   ├── components/     # 공유 컴포넌트 (WordCard, DomainChip, ComboEffect, LevelUpEffect)
│   └── screen/         # 25개 화면 (아래 목록 참조)
├── util/               # TtsManager, NotificationHelper, Receivers
└── di/                 # Hilt 모듈 (App, Database, Repository)
```

### 화면 목록 (25개)

| 화면 | 설명 |
|------|------|
| home | 홈 (오늘의 단어, Stage 요약, 일일 챌린지 진입) |
| study | 단어 학습 (Stage 선택) |
| flashcard | 플래시카드 |
| quiz | 4지선다 퀴즈 |
| spelling | 스펠링 퀴즈 |
| review | SM-2 복습 |
| wordlist | 단어 목록 |
| worddetail | 단어 상세 |
| bookmarks | 북마크 |
| search | 검색 |
| statistics | 학습 통계 + 리포트 공유 |
| settings | 설정 |
| profile | 프로필 |
| eduhome | 교육부 단어 홈 |
| eduwordlist | 교육부 단어 목록 |
| eduflashcard | 교육부 플래시카드 |
| eduquiz | 교육부 퀴즈 |
| wronganswers | 오답 노트 |
| idiomhome | 숙어/구동사 홈 |
| idiomlist | 숙어/구동사 목록 |
| idiomquiz | 숙어/구동사 퀴즈 |
| grammarhome | 문법 예문 홈 |
| grammarlist | 문법 예문 목록 |
| dailychallenge | 일일 챌린지 |
| placementtest | 배치 테스트 |

## DB 전략

- `assets/databases/engstudy.db`에 사전 생성된 SQLite DB 탑재
- `Room.databaseBuilder().createFromAsset()` 사용
- DB version 7 / `fallbackToDestructiveMigration()` 적용 (개발 중, 릴리즈 전 정식 마이그레이션 필요)
- Room identity hash: `90d07bfa248b01c3a5cbc93c5655b8b4`
- DB 생성 스크립트: `scripts/build_word_db.py` — kengdic + Free Dictionary API

### 스키마 요약 (8개 테이블)

**`words` 테이블** — 12,068개 (kengdic MPL 2.0 + Free Dictionary API)
- `stage` INT (1-6): 빈도 기반 학습 단계 (1=최고빈도 ~ 6=저빈도)
- `meaning` TEXT: 단어 의미
- `meaning_type` TEXT: 의미 언어 구분 (`'ko'` 또는 `'en'`)

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

**`bookmarks` 테이블**
- 사용자 북마크 단어

**`wrong_answers` 테이블** (v5 추가)
- 퀴즈/스펠링에서 오답 기록 저장
- `word_id`, `wrong_answer`, `correct_answer`, `quiz_type`, `created_at`

**`known_items` 테이블** (v7 추가)
- edu_words, idioms 등에 대한 "이미 알아요" 범용 추적
- `item_id` + `item_type`(unique): `'edu_word'`, `'idiom'` 등
- `marked_at`: 마킹 시점 timestamp

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

### 일일 챌린지: `GetDailyChallengeUseCase`
- 날짜(epoch day)를 시드로 사용해 결정적 의사난수로 10개 단어 선택
- 서버 없이 모든 기기에서 같은 날 같은 단어가 나옴 → 가족 간 점수 경쟁 가능
- `WordDao.getDailyChallengeWords()`: `ORDER BY (id * :seed) % 99991` SQL로 구현

### 배치 테스트: `PlacementTestScreen`
- 초기 레벨 평가로 학습 시작 Stage를 자동 추천

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

### 브랜드 컬러 (Dynamic Color 미사용)
- Light: Indigo blue + Deep orange + Teal
- Dark: Navy black + Lavender + Salmon accents
- Stage별 전용 색상: 초록→파랑→보라→주황→핑크→금색

## 주의사항

- 오프라인 전용 앱 — 네트워크 통신 없음
- Room DB version 7, `fallbackToDestructiveMigration()` 사용 중 — 릴리즈 전 정식 마이그레이션 경로 작성 필요 (TODO)
- Tatoeba 예문 사용 시 저작자 표시(CC BY 2.0 FR) 필요 — 앱 설정/정보 화면에 표기할 것
- `@Serializable` 사용을 위해 kotlin-serialization 플러그인 필요
- `scripts/build_word_db.py` 실행 전 `wordfreq`, `kengdic` Python 의존성 설치 필요
