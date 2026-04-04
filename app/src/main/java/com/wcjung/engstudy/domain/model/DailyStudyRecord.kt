package com.wcjung.engstudy.domain.model

/**
 * 일별 학습 기록의 도메인 모델.
 * data 레이어의 DailyStudyCount가 domain 레이어에 노출되지 않도록 분리한다.
 */
data class DailyStudyRecord(
    val studyDate: String,
    val count: Int
)
