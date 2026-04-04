package com.wcjung.engstudy.ui.screen.idiom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.domain.model.IdiomType
import com.wcjung.engstudy.domain.repository.IdiomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class IdiomHomeViewModel @Inject constructor(
    private val idiomRepository: IdiomRepository
) : ViewModel() {

    val totalCount: StateFlow<Int> = idiomRepository.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** 타입별 숙어 수를 사전에 StateFlow로 변환하여 불필요한 Flow 재생성을 방지한다 */
    val typeCounts: Map<IdiomType, StateFlow<Int>> = IdiomType.entries.associateWith { type ->
        idiomRepository.getCountByType(type.key)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }
}
