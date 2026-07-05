# Changelog

All notable changes to this project will be documented in this file.
Format based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased] - 2026-07-05

### Changed
- **홈 화면 컴팩트 정리**: 풀폭 카드 14~15장 나열로 번잡했던 홈을 재구성
  - 유색 카드를 "오늘의 단어" 히어로 1장으로 축소 (기존 7장+) — 시각적 위계 확립
  - 학습 목표 + 누적 통계(학습 완료/복습 예정) + 챌린지 진입을 요약 카드 1장으로 통합
    (`TodaySummaryCard`, 기존 `DailyProgressCard`·`StatCard` 2장 대체)
  - 교육부/숙어·구동사/문법 예문 바로가기 카드 3장 → 3열 타일 그리드(`ContentTile`)로 압축
  - 단계별 단어 카드 6장 → 구분선 리스트 카드 1장(`StageProgressRow`),
    테마에 정의만 되어 있던 Stage 전용 색상(초록~골드)을 진행 바·단계 점에 적용.
    단어가 0개인 단계는 표시하지 않음 (탭해도 빈 목록만 나오던 행 제거)
  - 레벨 테스트 배너를 가로형으로 컴팩트화. 화면 진입점·표시 정보는 모두 유지

### Added
- **사용자 단어 추가**: 단어 목록 상단 "+" 버튼 또는 검색 결과 없음 화면에서
  나만의 단어를 단어장에 직접 추가 (`AddWordScreen`)
  - 단어·뜻(필수), 발음·예문(선택), 학습 단계 선택. 입력 중 네이버/Cambridge 사전 링크 제공
  - words 테이블의 id 예약 구간(>= 1,000,000)에 저장 — 스키마 변경 없이(DB v12 유지)
    목록·검색·플래시카드·퀴즈·SM-2 복습·북마크·오답 노트에 자동 통합되고,
    콘텐츠 갱신 마이그레이션(id 기준 UPDATE)에도 유실되지 않음
  - `frequency_rank = 0`으로 저장되어 단어 목록·학습 조회에서 가장 먼저 노출
  - 중복 추가 방지 (기존 단어면 "기존 단어 보기"로 안내),
    단어 상세에서 "내가 추가한 단어" 표시 및 삭제(학습 기록 CASCADE 정리)
  - 일일 챌린지는 기기 간 동일 세트 보장을 위해 사용자 단어를 의도적으로 제외
  - 백업/복원 대상에 포함 (`userWords`, 하위 호환 유지)
  - DAO 회귀 테스트 `UserWordDaoTest` 6건 (id 발급·중복·삭제 가드·CASCADE·챌린지 제외·백업 왕복)
- **교육부 단어 기능 확장** (DB v12, `edu_bookmarks`/`edu_excluded_words` 테이블):
  기본 단어장과 동등한 부가 기능을 교육부 필수 영단어에도 추가
  - 북마크 — 프로필 → 즐겨찾기 "교육부" 탭에서 확인 (기본 단어장과 독립된 테이블)
  - 검색 — 검색 화면에 "단어"/"교육부" 탭 추가 (`EduWordRepository.searchWords` 연결)
  - 완전 제외 & 복원 — 교육부 단어 목록에서 다중 선택 제외, 프로필 → 제외된 단어
    "교육부 단어" 탭에서 복원. 제외된 단어는 플래시카드·퀴즈·스펠링 출제 풀에서도 제외
  - 스펠링 퀴즈 (`EduSpellingQuizScreen`) — 뜻을 보고 영어 철자 입력
  - 듣기 퀴즈 — 교육부 퀴즈에 `listening = true` 모드 추가, TTS 발음만으로 정답을 고름
  - 신규 퀴즈 응답도 `RecordQuizAnswerUseCase`로 기록되어 겹치는 단어는 SM-2 복습에 반영
    (quizType: `edu_spelling`, `edu_listening_quiz`)
  - DAO 회귀 테스트 `EduBookmarkDaoTest`, `EduExcludedWordDaoTest` (총 5건)
- **단어 의미 추가/변경** (DB v11, `user_word_meanings` 테이블): 단어 상세에서
  - 대표 뜻 수정(연필 아이콘) — 목록·검색·퀴즈·복습 전역에 반영, "기본값으로 되돌리기" 지원
  - "내 의미 추가" — 나만의 의미를 의미 목록에 추가/삭제
  - 콘텐츠 테이블과 분리된 사용자 테이블이라 앱 업데이트(콘텐츠 갱신)에도 유실되지 않음.
    백업/복원에 포함. Migration(10→11) + asset DB v11 스탬프,
    회귀 테스트: `Migration10To11Test`, `UserWordMeaningDaoTest`(override 합성), 백업 왕복 확장
- **단어 예문 커버리지 23.5% → 99.8%**: 로컬 LLM(Ollama gemma4)으로 미커버 9,226개 중
  9,199개 예문+한국어 번역 생성 (`scripts/build_examples_ollama.py`, API 비용 0원).
  `RefreshWordContentMigration`이 word_examples도 교체하도록 확장해 기존 사용자에게도 전달
  (마이그레이션 회귀 테스트 검증 항목 추가)
- **교육부 단어 SM-2 복습 통합**: 교육부 퀴즈 응답이 words 테이블의 동일 단어에
  학습 진도로 기록되어 복습(SM-2) 루프와 오답 노트에 포함됨.
  퀴즈 기록 로직을 `RecordQuizAnswerUseCase`로 추출해 일반/듣기/스펠링/교육부 퀴즈가 공유
  (단위 테스트 4건). words에 없는 교육부 단어(~8%)는 기존처럼 "알아요" 표시만 지원
- **듣기 퀴즈 모드**: 학습 → "듣기 퀴즈" — TTS 발음을 듣고 뜻을 고르는 4지선다.
  문제 진입 시 자동 재생 + "다시 듣기" 버튼, 정답 선택 후 단어·발음기호 공개.
  기존 Quiz 구조 재사용(`Screen.Quiz(listening = true)`)으로 오답 기록·SM-2·콤보 동일 적용
- **학습 데이터 백업/복원**: 설정에서 학습 진도·북마크·오답 노트·"알아요" 표시·설정을
  JSON 파일로 내보내고(SAF) 복원 가능. 오프라인 전용 앱의 기기 변경/분실 대비.
  복원 시 현재 DB에 없는 단어를 참조하는 행은 건너뛰어 FK 위반 방지
  (`BackupManagerTest` 왕복 테스트 3건)
- **홈 위젯 상호작용**: 단어 탭 → 해당 단어 상세 화면으로 딥링크,
  "다음 단어 →" 버튼 → 캐시된 10개 단어를 오프라인 순환.
  위젯 데이터가 앱에서 갱신되지 않던 버그 수정 — `updateWidgetData`가
  호출되지 않아 항상 플레이스홀더만 표시됐음. 이제 홈 진입 시
  오늘의 단어 + 랜덤 9개를 SharedPreferences에 캐시 (`WidgetUpdateHelperTest` 5건)
- **오답노트 재도전**: 오답 노트에서 기록된 단어들로 바로 4지선다 퀴즈를 풀 수 있는
  "오답 재도전" 버튼 추가. `Screen.Quiz`가 `wordIds` 목록을 받아 지정 단어로 출제
  (DAO 회귀 테스트 `WordDaoByIdsTest`)
- **데이터 출처 및 라이선스 화면** (`LicensesScreen`): 설정에서 진입.
  Tatoeba(CC BY 2.0 FR) 저작자 표시 의무 이행 + kengdic(MPL 2.0),
  교육부 공공데이터, Semigradsky(MIT), wordfreq(MIT), Free Dictionary API 출처 표기.
  UI 회귀 테스트 `LicensesScreenTest` 추가 (espresso-core 3.7.0 도입 —
  API 35+ 에뮬레이터의 `InputManager.getInstance` 크래시 수정 버전)

### Fixed
- **검색 관련도 정렬**: 단어/교육부 단어/숙어 검색이 "정확 일치 > 접두 일치 > 부분 일치 >
  뜻 일치" 순으로 정렬되도록 개선 (기존엔 빈도·id 순만 적용되어 정확 일치가 뒤로 밀림).
  DAO 회귀 테스트 `WordSearchRelevanceTest` 추가
- **교육부 뜻 교차 검증 병합** (`build_meanings_llm.py --edu-merge`): 교육부 검수 뜻이
  다의어의 덜 흔한 의미인 경우 보정 (예: just → "올바른", burn → "시내", teen → "슬픔")
  - 교육부 겹침 2,772개 단어에 gemma4로 흔한 뜻 1~3개 생성 후 교차 검증:
    겹치면 교육부 뜻 유지(1,965개), 안 겹치면 LLM 뜻을 대표로 승격하고
    교육부 뜻은 `word_meanings` 뒤 순서에 보존(807개)
  - 1글자 뜻("흰", "물", "쓴")도 겹침 판정하도록 `_korean_tokens` 수정
    (기존엔 2글자 미만 토큰이 버려져 white/water 등이 잘못 교체됨) — `--selftest` 회귀 테스트 추가
  - 동의어 불일치로 잘못 교체되는 4개 단어(well, even, table, pat)는
    `EDU_MERGE_KEEP` 수동 유지 목록으로 보호 (교체 목록 811개 전수 검토로 선정)
- **기능어 큐레이션 뜻 보호** (`apply_edu_meanings.py`): 기능어 56개(FUNCTION_WORDS)를
  교육부 뜻 적용 대상에서 제외하고, 과거 실행으로 덮인 뜻은 실행 시 자동 복원
  (예: just "올바른"→"단지, 바로, 방금", will "의지"→"~할 것이다", the "art.저"→"관사: 그, 저")

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
