package com.wcjung.engstudy.ui.screen.addword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.wcjung.engstudy.data.local.dao.UserWordDao
import com.wcjung.engstudy.data.local.entity.WordEntity
import com.wcjung.engstudy.ui.navigation.Screen
import com.wcjung.engstudy.util.launchSafely
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AddWordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userWordDao: UserWordDao
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.AddWord>()

    private val _word = MutableStateFlow(route.prefillWord.orEmpty())
    val word: StateFlow<String> = _word

    private val _meaning = MutableStateFlow("")
    val meaning: StateFlow<String> = _meaning

    private val _pronunciation = MutableStateFlow("")
    val pronunciation: StateFlow<String> = _pronunciation

    private val _exampleEn = MutableStateFlow("")
    val exampleEn: StateFlow<String> = _exampleEn

    private val _exampleKo = MutableStateFlow("")
    val exampleKo: StateFlow<String> = _exampleKo

    private val _stageLevel = MutableStateFlow(1)
    val stageLevel: StateFlow<Int> = _stageLevel

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /** 이미 있는 단어일 때 해당 단어 상세로 이동할 수 있게 id를 노출한다. */
    private val _duplicateWordId = MutableStateFlow<Int?>(null)
    val duplicateWordId: StateFlow<Int?> = _duplicateWordId

    /** 저장 완료 — 화면이 관찰하다가 새 단어 상세로 이동한다. */
    private val _savedWordId = MutableStateFlow<Int?>(null)
    val savedWordId: StateFlow<Int?> = _savedWordId

    fun updateWord(value: String) {
        _word.value = value
        clearError()
    }

    fun updateMeaning(value: String) { _meaning.value = value }
    fun updatePronunciation(value: String) { _pronunciation.value = value }
    fun updateExampleEn(value: String) { _exampleEn.value = value }
    fun updateExampleKo(value: String) { _exampleKo.value = value }
    fun updateStageLevel(level: Int) { _stageLevel.value = level }

    fun save() {
        val wordText = _word.value.trim()
        val meaningText = _meaning.value.trim()
        if (wordText.isBlank() || meaningText.isBlank()) return

        launchSafely(onError = { _errorMessage.value = "저장 중 오류가 발생했습니다" }) {
            val existing = userWordDao.findByText(wordText)
            if (existing != null) {
                _duplicateWordId.value = existing.id
                _errorMessage.value = "'${existing.word}'은(는) 이미 단어장에 있습니다"
                return@launchSafely
            }
            val newId = userWordDao.addUserWord(
                WordEntity(
                    id = 0, // addUserWord가 트랜잭션 안에서 실제 id를 발급해 교체한다
                    word = wordText,
                    pronunciation = _pronunciation.value.trim(),
                    meaning = meaningText,
                    meaningType = "ko",
                    partOfSpeech = "",
                    exampleEn = _exampleEn.value.trim(),
                    exampleKo = _exampleKo.value.trim(),
                    stage = _stageLevel.value,
                    domain = "GENERAL",
                    // 0이면 목록·학습 조회(frequency_rank ASC)에서 가장 먼저 노출된다 —
                    // 직접 추가한 단어는 지금 배우고 싶은 단어라는 의도를 반영
                    frequencyRank = 0
                )
            )
            _savedWordId.value = newId
        }
    }

    private fun clearError() {
        _errorMessage.value = null
        _duplicateWordId.value = null
    }
}
