package com.wcjung.engstudy.ui.screen.wronganswer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.domain.model.WrongAnswer
import com.wcjung.engstudy.domain.repository.WrongAnswerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WrongAnswerViewModel @Inject constructor(
    private val wrongAnswerRepository: WrongAnswerRepository
) : ViewModel() {

    val wrongAnswers: StateFlow<List<WrongAnswer>> =
        wrongAnswerRepository.getRecentWrongAnswers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
