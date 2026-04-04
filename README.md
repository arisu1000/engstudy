# EngStudy - 영어 단어장

한국어 사용자를 위한 오프라인 영어 단어장 Android 앱입니다.

## 주요 기능

- **14,060개 영어 단어** 내장 (오프라인 사용 가능)
- **Stage 1-6 단계별 학습**: 빈도 기반 분류 (Stage 1 = 최고빈도 ~ Stage 6 = 저빈도)
- **교육부 필수 영단어 3,000개**: 초등 800개, 중고등 1,800개, 전문 400개
- **플래시카드** 학습 (카드 뒤집기)
- **4지선다 퀴즈** (영→한, 한→영)
- **스펠링 퀴즈** (한국어 뜻 보고 영어 입력)
- **SM-2 간격반복** 복습 시스템
- **TTS 발음** 재생
- **학습 통계** (Stage별 진행률, 전체 현황)
- **북마크** (즐겨찾기)
- **검색** (영어/한국어 양방향)
- **다크모드** 지원
- **학습 리마인더** 알림

## 기술 스택

- Kotlin 2.1, Jetpack Compose, Material 3
- MVVM + Clean Architecture
- Room v4 (pre-populated DB), Hilt, Coroutines + Flow
- DataStore Preferences, Compose Navigation

## 빌드

```bash
# 필요: Android Studio, JDK 17+
./gradlew assembleDebug
```

## 테스트

```bash
./gradlew testDebugUnitTest
```

## 단어 데이터 생성

외부 소스(wordfreq, MUSE en-ko 사전, kengdic)를 조합하여 DB를 생성합니다.

```bash
cd scripts
pip install wordfreq  # Python 의존성 설치
python build_word_db.py   # 14,060개 단어 DB 생성
```

생성된 `engstudy.db`를 `app/src/main/assets/databases/`에 배치합니다.

## 프로젝트 구조

```
app/src/main/java/com/wcjung/engstudy/
├── data/           # Room DB, Repository 구현, DataStore
├── domain/         # 도메인 모델, Repository 인터페이스, UseCase
├── ui/             # Compose UI (15개 화면, 네비게이션, 테마)
├── util/           # TTS, 알림
└── di/             # Hilt DI 모듈
```

## 라이선스

Private project.
