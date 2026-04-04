package com.wcjung.engstudy.ui.screen.edu

import androidx.lifecycle.ViewModel
import com.wcjung.engstudy.domain.model.EduLevel
import com.wcjung.engstudy.domain.repository.EduWordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class EduHomeViewModel @Inject constructor(
    private val eduWordRepository: EduWordRepository
) : ViewModel() {

    val totalCount: Flow<Int> = eduWordRepository.getTotalCount()

    fun getCountByLevel(level: EduLevel): Flow<Int> =
        eduWordRepository.getCountByLevel(level.key)
}
