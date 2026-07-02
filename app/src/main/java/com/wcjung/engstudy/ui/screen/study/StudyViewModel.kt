package com.wcjung.engstudy.ui.screen.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.data.datastore.UserPreferences
import com.wcjung.engstudy.domain.model.Stage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StudyViewModel @Inject constructor(
    userPreferences: UserPreferences
) : ViewModel() {

    /**
     * 학습 화면 진입 시 기본으로 선택할 단계.
     *
     * 배치 테스트를 완료한 사용자는 추천 단계가 기본 선택되어, 온보딩 결과가
     * 실제 학습 시작점에 반영된다. 테스트를 건너뛴 사용자는 null(전체 단계)로 두어
     * 기존처럼 사용자가 직접 고르게 한다.
     */
    val initialSelectedStage: StateFlow<Stage?> = combine(
        userPreferences.hasCompletedPlacementTest,
        userPreferences.recommendedStage
    ) { completed, recommendedLevel ->
        if (completed) Stage.fromLevel(recommendedLevel) else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
