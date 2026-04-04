# EngStudy - 영어 단어장

한국어 사용자를 위한 오프라인 영어 단어장 Android 앱입니다.

## 주요 기능

- **5,000+ 영어 단어** 내장 (오프라인 사용 가능)
- **12개 분야** 분류: 일상생활, 비즈니스, 과학, 기술, 의학, 법률, 교육, 예술, 스포츠, 여행, 음식, 일반
- **5단계 수준** 분류: 초등학교 ~ 전문가/성인
- **플래시카드** 학습 (카드 뒤집기)
- **4지선다 퀴즈** (영→한, 한→영)
- **스펠링 퀴즈** (한국어 뜻 보고 영어 입력)
- **SM-2 간격반복** 복습 시스템
- **TTS 발음** 재생
- **학습 통계** (진행률, 분야별 현황)
- **북마크** (즐겨찾기)
- **검색** (영어/한국어 양방향)
- **다크모드** 지원
- **학습 리마인더** 알림

## 기술 스택

- Kotlin 2.1, Jetpack Compose, Material 3
- MVVM + Clean Architecture
- Room (pre-populated DB), Hilt, Coroutines + Flow
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

```bash
cd scripts
python generate_word_db.py   # 기본 5,000+ 단어 생성
python expand_words.py        # 추가 단어 보충
```

생성된 `engstudy.db`를 `app/src/main/assets/databases/`에 배치합니다.

## 프로젝트 구조

```
app/src/main/java/com/wcjung/engstudy/
├── data/           # Room DB, Repository 구현, DataStore
├── domain/         # 도메인 모델, Repository 인터페이스, UseCase
├── ui/             # Compose UI (13개 화면, 네비게이션, 테마)
├── util/           # TTS, 알림
└── di/             # Hilt DI 모듈
```

## 라이선스

Private project.
