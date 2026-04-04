# Changelog

## [Unreleased] - 2026-04-04

### Added
- **Stage 기반 학습 체계** (Stage 1-6): 분야/연령대 분류를 폐기하고 wordfreq 빈도 점수 기반 6단계 Stage 시스템으로 전환
- **교육부 필수 영단어 3,000개** (`edu_words` 테이블): 초등 800개, 중고등 1,800개, 전문 400개 — `EduLevel` enum으로 분류
- **EduHomeScreen**: 교육부 단어 홈 화면 (Stage별 진행 카드 + "교육부 필수 영단어 3,000" 진입점)
- **EduWordListScreen**: 교육부 단어 목록 화면 (EduLevel 필터링)
- `EduWordEntity`, `EduWordDao`, `EduWord` 도메인 모델, `EduWordRepository`
- 외부 데이터 파이프라인: `scripts/build_word_db.py` — wordfreq, MUSE en-ko 사전, kengdic 소스 조합
- `meaning_type` 컬럼 (`'ko'`/`'en'`): 한국어/영어 의미 구분

### Changed
- **단어 수 확대**: 수동 작성 ~3,000개 → 외부 소스 기반 14,060개
- **`words` 스키마 변경**:
  - `meaning_ko` 컬럼 → `meaning`으로 이름 변경
  - `age_group` 컬럼 제거
  - `stage` INT 컬럼 추가 (기존 `domain`/`age_group` 분류 대체)
- **StudyScreen**: AgeGroup 선택 → Stage 선택 방식으로 변경
- **HomeScreen**: Stage별 진행 요약 카드 표시
- Room DB version 1 → **version 4** (`fallbackToDestructiveMigration()` 사용)
- 데이터 생성 스크립트: `generate_word_db.py` + `expand_words.py` → `build_word_db.py` 단일 스크립트로 통합

### Removed
- `domain`/`age_group` 기반 단어 분류 체계
- `AgeGroup` 도메인 모델 및 관련 UI 컴포넌트 (DomainChip 등 영향 범위 확인 필요)
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
