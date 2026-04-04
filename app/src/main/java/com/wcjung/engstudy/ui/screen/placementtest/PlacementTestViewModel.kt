package com.wcjung.engstudy.ui.screen.placementtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.data.datastore.UserPreferences
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 초기 레벨 테스트를 관리하는 ViewModel.
 * Stage 1-4에서 각 10개씩 총 ~40문제를 출제하고,
 * 각 스테이지별 정답률 80% 이상이면 "이미 아는 단계"로 판정한다.
 * 추천 시작 스테이지 = 정답률 80% 미만인 첫 번째 스테이지.
 */
@HiltViewModel
class PlacementTestViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    data class Question(
        val word: Word,
        val options: List<String>,
        val correctIndex: Int,
        val stage: Int
    )

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    private val _recommendedStage = MutableStateFlow(1)
    val recommendedStage: StateFlow<Int> = _recommendedStage

    /** 스테이지별 (정답 수, 총 문제 수) */
    private val _stageResults = MutableStateFlow<Map<Int, Pair<Int, Int>>>(emptyMap())
    val stageResults: StateFlow<Map<Int, Pair<Int, Int>>> = _stageResults

    private val correctByStage = mutableMapOf<Int, Int>()
    private val totalByStage = mutableMapOf<Int, Int>()

    val currentQuestion: Question?
        get() = _questions.value.getOrNull(_currentIndex.value)

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            val allQuestions = mutableListOf<Question>()

            // Stage 1-4에서 각 10-13개씩 단어를 가져와 문제를 생성
            for (stage in 1..4) {
                val words = wordRepository.getRandomWordsByStage(stage, 13)
                for (word in words.take(10)) {
                    // 오답 3개를 같은 스테이지에서 가져옴
                    val distractors = wordRepository.getRandomWordsInStage(
                        stage = stage,
                        excludeId = word.id,
                        count = 3
                    )
                    if (distractors.size < 3) continue

                    val options = (distractors.map { it.word } + word.word).shuffled()
                    val correctIndex = options.indexOf(word.word)

                    allQuestions.add(
                        Question(
                            word = word,
                            options = options,
                            correctIndex = correctIndex,
                            stage = stage
                        )
                    )
                }
            }

            _questions.value = allQuestions
            _isLoading.value = false
        }
    }

    fun answer(selectedIndex: Int) {
        val question = currentQuestion ?: return
        val stage = question.stage

        totalByStage[stage] = (totalByStage[stage] ?: 0) + 1
        if (selectedIndex == question.correctIndex) {
            correctByStage[stage] = (correctByStage[stage] ?: 0) + 1
        }

        val nextIndex = _currentIndex.value + 1
        if (nextIndex >= _questions.value.size) {
            finishTest()
        } else {
            _currentIndex.value = nextIndex
        }
    }

    private fun finishTest() {
        // 스테이지별 결과 계산
        val results = (1..4).associateWith { stage ->
            val correct = correctByStage[stage] ?: 0
            val total = totalByStage[stage] ?: 0
            correct to total
        }
        _stageResults.value = results

        // 추천 스테이지: 80% 미만인 첫 번째 스테이지
        var recommended = 1
        for (stage in 1..4) {
            val (correct, total) = results[stage] ?: (0 to 0)
            if (total > 0 && correct.toFloat() / total >= 0.8f) {
                recommended = stage + 1
            } else {
                break
            }
        }
        // 최대 stage 4까지 (5, 6은 테스트에 포함하지 않으므로)
        _recommendedStage.value = recommended.coerceAtMost(4)
        _isFinished.value = true

        viewModelScope.launch {
            userPreferences.setRecommendedStage(_recommendedStage.value)
            userPreferences.setPlacementTestCompleted()
        }
    }
}
