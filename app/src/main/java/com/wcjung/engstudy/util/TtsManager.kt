package com.wcjung.engstudy.util

import android.content.Context
import android.speech.tts.TextToSpeech
import com.wcjung.engstudy.data.datastore.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext context: Context,
    userPreferences: UserPreferences
) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    // 발음 속도는 설정(DataStore)에 저장된 값을 따른다. 기본값은 UserPreferences와 동일한 0.85.
    private var speechRate = 0.85f

    // 싱글턴 수명 동안 설정 변경을 구독하기 위한 스코프. shutdown 시 취소한다.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                isReady = result != TextToSpeech.LANG_MISSING_DATA
                    && result != TextToSpeech.LANG_NOT_SUPPORTED
                // 엔진 초기화 시점에 이미 로드된 속도를 즉시 적용한다.
                tts?.setSpeechRate(speechRate)
            }
        }

        // 저장된 발음 속도를 관찰해 콜드 스타트 복원과 실시간 변경을 모두 반영한다.
        // 기존에는 설정 슬라이더 값이 DataStore에만 저장되고 TTS 엔진에는 전달되지 않아
        // 발음 속도 설정이 실제로 동작하지 않았다.
        scope.launch {
            userPreferences.ttsSpeed.collect { rate ->
                speechRate = rate
                tts?.setSpeechRate(rate)
            }
        }
    }

    fun speak(text: String) {
        if (isReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "engstudy_tts")
        }
    }

    fun isAvailable(): Boolean = isReady

    fun shutdown() {
        scope.cancel()
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
