# 001: 사용자 추가 단어를 words 테이블의 id 예약 구간에 저장

날짜: 2026-07-05
상태: 수락됨

## 맥락

사용자가 직접 단어를 추가하는 기능이 필요했다. 추가한 단어는 단어 목록·검색·플래시카드·
퀴즈·SM-2 복습·북마크·오답 노트 등 기존 학습 기능 전체에서 내장 단어와 동일하게
동작해야 한다.

이 앱의 데이터 구조상 두 가지 제약이 있다:

1. **모든 학습 기능이 `words.id`를 기준으로 동작한다.** `learning_progress`,
   `bookmarks`, `wrong_answers`, `user_word_meanings`가 전부 `words(id)`를 FK로
   참조하고, 학습/퀴즈/복습 쿼리도 words 테이블만 조회한다.
2. **콘텐츠 갱신 마이그레이션이 콘텐츠 테이블을 교체한다.**
   `RefreshWordContentMigration`은 asset DB의 words를 id 기준 UPDATE/INSERT하고,
   `word_meanings`/`word_examples`는 전체 교체한다. 콘텐츠 테이블에 사용자 데이터를
   섞으면 앱 업데이트 때 유실될 수 있다.

검토한 대안:

- **A. 별도 `user_words` 테이블 (v11 `user_word_meanings` 방식)**: 사용자 데이터
  분리는 깔끔하지만, `words.id` FK 기반인 학습 진도·퀴즈·복습에 통합하려면
  모든 DAO 쿼리와 리포지토리에 두 테이블 병합 로직이 필요하다. 사실상 학습 파이프라인
  전체를 수정해야 하고, FK 무결성(진도가 어느 테이블의 id를 가리키는지)도 깨진다.
- **B. words 테이블에 `is_user_added` 컬럼 추가**: 스키마 변경으로 DB 버전 증가,
  마이그레이션, asset DB 재생성과 identity hash 재스탬프가 필요하다.
- **C. words 테이블의 id 예약 구간(>= 1,000,000) 사용** ← 채택

## 결정

사용자 단어를 별도 테이블 없이 **words 테이블의 id >= 1,000,000 구간**
(`UserWordDao.USER_WORD_ID_START`)에 저장한다.

- 스키마 변경이 없으므로 DB 버전(v12)과 asset DB를 그대로 유지한다
- id 발급은 `UserWordDao.addUserWord()`가 트랜잭션 안에서 `MAX(id) + 1`로 수행한다
- 사용자 단어 여부는 id로 판별한다 (`isUserWordId()`)
- `frequency_rank = 0`으로 저장해 목록·학습 조회(frequency_rank ASC)에서 먼저 노출한다
- 콘텐츠 빌드 스크립트(`build_word_db.py`)는 콘텐츠 id가 예약 구간에 도달하면
  assert로 실패한다 — 구간 침범을 빌드 시점에 차단
- 일일 챌린지 쿼리는 `id < 1,000,000` 조건으로 사용자 단어를 제외한다
  (기기마다 다른 단어가 섞이면 "모든 기기에서 같은 날 같은 단어" 설계가 깨짐)
- 백업/복원은 이 구간을 사용자 데이터로 취급한다 (`BackupDao.getAllUserWords()` 등).
  복원 시 FK 검증은 "현재 DB의 콘텐츠 id + 백업 파일의 사용자 단어 id"로 판단한다

## 결과

**장점**

- 학습/퀴즈/복습/북마크/오답/의미 편집 등 `words.id` 기반 기능 전체에 코드 수정 없이
  자동 통합된다 (이 기능의 실제 diff는 추가 화면 + DAO + 백업 확장 정도)
- 콘텐츠 갱신 마이그레이션(asset id < 1M 기준 UPDATE)에 사용자 단어가 안전하다
- 스키마 변경·마이그레이션·asset 재생성이 전혀 필요 없다

**단점 / 주의사항**

- "words = 순수 콘텐츠 테이블"이라는 기존 가정이 깨진다. words를 전체 DELETE하거나
  통계(전체 단어 수 등)를 콘텐츠 기준으로 계산하는 코드는 이 구간을 의식해야 한다
- 기기 간 결정성이 필요한 기능(일일 챌린지처럼)은 쿼리에서 명시적으로 이 구간을
  제외해야 한다 — 새 기능 추가 시 체크 포인트
- 콘텐츠가 1,000,000개에 도달하면 구간 재설계가 필요하다 (현재 12,068개로 여유 충분,
  빌드 스크립트 assert가 감지)

관련: `CLAUDE.md`의 "스키마 요약 > words 테이블 > 사용자 추가 단어" 항목,
`app/src/main/java/com/wcjung/engstudy/data/local/dao/UserWordDao.kt`,
회귀 테스트 `UserWordDaoTest`.
