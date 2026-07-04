# archive — 초기 DB 구축용 일회성 스크립트

현행 DB 빌드 파이프라인(`build_word_db.py → build_meanings.py →
apply_edu_meanings.py → build_meanings_llm.py`)에 포함되지 않는
과거 스크립트와 원본 데이터를 보관한다. 실행할 일은 없으며,
데이터 출처 추적(provenance)을 위해 남겨둔다.

| 파일 | 용도 (당시) |
|------|------------|
| `generate_word_db.py` | 1세대 DB 생성기 (word_data 기반 5,000단어) — `build_word_db.py`로 대체됨 |
| `batch1~10_*.py`, `batch_utils.py` | 분야별 단어 데이터 수집 배치 |
| `bulk_expand.py`, `gen_bulk.py`, `expand_words.py`, `gen_data_1.py` | 단어 목록 확장 실험 |
| `idioms_raw.csv`, `idioms_phrasefinder.csv`, `phrasal_verbs_raw.json` | idioms 테이블 원본 데이터 (Semigradsky MIT 등) |
| `kor-eng.zip`, `en-ko-muse.txt`, `cc-kedict/`, `word_data/`, `remaining_words.json` | 라이선스 문제(MUSE CC BY-NC) 등으로 채택하지 않았거나 사용이 끝난 사전 데이터 |
