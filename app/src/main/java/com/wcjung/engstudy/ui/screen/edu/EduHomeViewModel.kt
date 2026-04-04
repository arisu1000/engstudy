package com.wcjung.engstudy.ui.screen.edu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.domain.model.EduLevel
import com.wcjung.engstudy.domain.repository.EduWordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EduHomeViewModel @Inject constructor(
    private val eduWordRepository: EduWordRepository
) : ViewModel() {

    val totalCount: StateFlow<Int> = eduWordRepository.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** 레벨별 단어 수를 사전에 StateFlow로 변환하여 불필요한 Flow 재생성을 방지한다 */
    val levelCounts: Map<EduLevel, StateFlow<Int>> = EduLevel.entries.associateWith { level ->
        eduWordRepository.getCountByLevel(level.key)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }
}
