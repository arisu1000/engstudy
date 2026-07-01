package com.wcjung.engstudy.util

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "EngStudy"

/**
 * `viewModelScope.launch`를 감싸 코루틴 내부의 예외를 삼키지 않고 로깅하는 안전 실행 헬퍼.
 *
 * 이 앱은 오프라인 SQLite(Room) + DataStore 기반이라 네트워크 예외는 없지만,
 * DB/DataStore 읽기·쓰기에서 드물게 예외(디스크 꽉 참, 파일 손상 등)가 발생할 수 있다.
 * 처리되지 않은 예외가 `viewModelScope`로 전파되면 앱이 그대로 크래시하므로,
 * 각 ViewModel의 일회성 코루틴(주로 쓰기 및 단발성 읽기)을 이 헬퍼로 감싼다.
 *
 * - [CancellationException]은 코루틴 취소의 정상 신호이므로 그대로 다시 던진다.
 * - 그 외 예외는 로깅하고 [onError] 콜백으로 넘겨 필요 시 UI에 알릴 수 있게 한다.
 *
 * 주의: `stateIn` 기반 StateFlow 스트림에는 적용되지 않는다.
 * 그런 스트림의 예외 처리는 `Flow.catch` 연산자를 사용한다.
 */
fun ViewModel.launchSafely(
    onError: (Throwable) -> Unit = {},
    block: suspend CoroutineScope.() -> Unit
): Job = viewModelScope.launch {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Log.e(TAG, "launchSafely에서 처리되지 않은 예외: ${e.message}", e)
        onError(e)
    }
}
