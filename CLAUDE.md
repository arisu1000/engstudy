# EngStudy - AI 에이전트 컨텍스트

## 프로젝트 개요
한국어 사용자를 위한 오프라인 영어 단어장 Android 앱. 14,060개 단어를 앱 내 SQLite DB에 내장하고, Stage 기반(1-6단계, 빈도순) 학습, 교육부 필수 영단어 3,000개, 플래시카드, 퀴즈, 스펠링, SM-2 간격반복, 통계, 알림 등을 지원한다.

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
| DB | Room (pre-populated from assets), version 5 |
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
│   ├── local/          # Room DB, DAOs, Entities (WordEntity, EduWordEntity, IdiomEntity 등)
│   ├── repository/     # Repository 구현체 (EduWordRepository, IdiomRepository 포함)
│   └── datastore/      # DataStore UserPreferences
├── domain/
│   ├── model/          # Domain 모델 (Word, EduWord, EduLevel, Idiom, IdiomType 등)
│   ├── repository/     # Repository 인터페이스
│   └── usecase/        # SM-2 알고리즘 등 UseCase
├── ui/
│   ├── navigation/     # NavGraph, Screen routes
│   ├── theme/          # Material 3 테마
│   ├── components/     # 공유 컴포넌트 (WordCard, DomainChip, ComboEffect, LevelUpEffect)
│   └── screen/         # 22개 화면 (home, study, flashcard, quiz, spelling, review, wordlist, worddetail, bookmarks, search, statistics, settings, profile, eduhome, eduwordlist, eduflashcard, eduquiz, wronganswer, challenge, idiomhome, idiomlist, idiomquiz)
├── util/               # TtsManager, NotificationHelper, Receivers
└── di/                 # Hilt 모듈 (App, Database, Repository)
```

## DB 전략

- `assets/databases/engstudy.db`에 사전 생성된 SQLite DB 탑재
- `Room.databaseBuilder().createFromAsset()` 사용
- DB version 6 / `fallbackToDestructiveMigration()` 적용 (개발 중 파괴적 마이그레이션 허용)
- 단어 데이터 생성: `scripts/build_word_db.py` — wordfreq, MUSE en-ko 사전, kengdic 외부 소스를 조합하여 14,060개 단어 생성

### 스키마 요약

**`words` 테이블**
- `stage` INT (1-6): 빈도 기반 학습 단계 (1=최고빈도 ~ 6=저빈도)
- `meaning` TEXT: 단어 의미 (이전 `meaning_ko` 컬럼에서 변경됨)
- `meaning_type` TEXT: 의미 언어 구분 (`'ko'` 또는 `'en'`)
- `age_group` 컬럼 제거됨

**`edu_words` 테이블**
- 교육부 공식 초중고 교육과정 필수 영단어 3,000개
- `edu_level`: `EduLevel` enum (`초등` 800개, `중고` 1,800개, `전문` 400개)

**`wrong_answers` 테이블** (v5 추가)
- 퀴즈/스펠링에서 오답 기록 저장
- `word_id`, `wrong_answer`, `correct_answer`, `quiz_type`, `created_at`

**`idioms` 테이블** (v6 추가)
- 영어 숙어(idiom)와 구동사(phrasal_verb) 1,099개
- `type`: `'idiom'` 또는 `'phrasal_verb'`
- `meaning_type`: 의미 언어 (`'en'` 또는 `'ko'`)
- `category`: 분류 (`'daily'` 등)

### 외부 데이터 소스
- **wordfreq** (Python 라이브러리): 빈도 점수 기반 Stage 분류
- **MUSE en-ko**: 영-한 다국어 임베딩 사전
- **kengdic**: 한국어-영어 사전

## 핵심 알고리즘

SM-2 간격반복: `CalculateSpacedRepetitionUseCase`
- quality 0-5, ease factor 최소 1.3
- 간소화 4단계: Again(1), Hard(3), Good(4), Easy(5)
- interval >= 21일이면 "학습 완료"

## 주요 기능

### 일일 챌린지 (Daily Challenge)
- `GetDailyChallengeUseCase`: 날짜(epoch day)를 시드로 사용해 결정적 의사난수로 10개 단어 선택
- 서버 없이 모든 기기에서 같은 날 같은 단어가 나옴 → 가족 간 점수 경쟁 가능
- `WordDao.getDailyChallengeWords()`: `ORDER BY (id * :seed) % 99991` SQL로 구현
- 결과 화면에서 Android `Intent.ACTION_SEND`로 공유

### 콤보 시스템
- Quiz, SpellingQuiz, EduQuiz, DailyChallenge 모든 퀴즈 화면에 적용
- `comboCount` / `maxCombo` StateFlow로 추적
- `ComboEffect` 컴포넌트: 3/5/10 콤보에서 단계별 애니메이션 표시
- `LevelUpEffect` 컴포넌트: Stage 완료 시 confetti 파티클 축하 오버레이

### 학습 리포트 공유
- `StatisticsViewModel.generateReport()`: 학습 통계를 이모지 포함 텍스트로 포맷
- 통계 화면 TopAppBar에 공유 버튼 추가
- `Intent.ACTION_SEND`로 카카오톡/메시지 등 모든 앱과 공유 가능

## 주의사항

- 오프라인 전용 앱 — 네트워크 통신 없음
- Room DB version 4, `fallbackToDestructiveMigration()` 사용 중 — 정식 마이그레이션 경로 미작성
- `words` 스키마에서 `age_group` 제거, `meaning_ko` → `meaning`으로 컬럼명 변경됨
- `@Serializable` 사용을 위해 kotlin-serialization 플러그인 필요
- `scripts/build_word_db.py` 실행 전 `wordfreq`, `muse`, `kengdic` 등 Python 의존성 설치 필요
