# EngStudy - AI 에이전트 컨텍스트

## 프로젝트 개요
한국어 사용자를 위한 오프라인 영어 단어장 Android 앱. 5,000+ 단어를 앱 내 SQLite DB에 내장하고, 분야별/연령대별 분류, 빈도순 학습, 플래시카드, 퀴즈, 스펠링, SM-2 간격반복, 통계, 알림 등을 지원한다.

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
| DB | Room (pre-populated from assets) |
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
│   ├── local/          # Room DB, DAOs, Entities
│   ├── repository/     # Repository 구현체
│   └── datastore/      # DataStore UserPreferences
├── domain/
│   ├── model/          # Domain 모델 (Word, Domain, AgeGroup 등)
│   ├── repository/     # Repository 인터페이스
│   └── usecase/        # SM-2 알고리즘 등 UseCase
├── ui/
│   ├── navigation/     # NavGraph, Screen routes
│   ├── theme/          # Material 3 테마
│   ├── components/     # 공유 컴포넌트 (WordCard, DomainChip)
│   └── screen/         # 13개 화면 (home, study, flashcard, quiz, spelling, review, wordlist, worddetail, bookmarks, search, statistics, settings, profile)
├── util/               # TtsManager, NotificationHelper, Receivers
└── di/                 # Hilt 모듈 (App, Database, Repository)
```

## DB 전략

- `assets/databases/engstudy.db`에 사전 생성된 SQLite DB 탑재
- `Room.databaseBuilder().createFromAsset()` 사용
- 단어 데이터 생성: `scripts/generate_word_db.py` → SQLite
- 보충 데이터: `scripts/expand_words.py`

## 핵심 알고리즘

SM-2 간격반복: `CalculateSpacedRepetitionUseCase`
- quality 0-5, ease factor 최소 1.3
- 간소화 4단계: Again(1), Hard(3), Good(4), Easy(5)
- interval >= 21일이면 "학습 완료"

## 주의사항

- 오프라인 전용 앱 — 네트워크 통신 없음
- Room 스키마 변경 시 migration 필요 (현재 version 1)
- `@Serializable` 사용을 위해 kotlin-serialization 플러그인 필요
