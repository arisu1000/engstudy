package com.wcjung.engstudy.util

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * launchSafely 확장 함수 테스트.
 *
 * ViewModel 코루틴에서 발생한 예외가 앱을 크래시시키지 않고 로깅·콜백으로 처리되는지,
 * 그리고 코루틴 취소(CancellationException)는 정상 신호로 다시 전파되는지 검증한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LaunchSafelyTest {

    private class TestViewModel : ViewModel()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // viewModelScope는 Dispatchers.Main을 사용하므로 테스트 디스패처로 대체한다.
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `정상 블록은 그대로 실행된다`() = runTest(dispatcher) {
        val vm = TestViewModel()
        var executed = false

        vm.launchSafely { executed = true }
        advanceUntilIdle()

        assertTrue(executed)
    }

    @Test
    fun `블록에서 예외가 발생해도 크래시하지 않고 onError로 전달된다`() = runTest(dispatcher) {
        val vm = TestViewModel()
        var caught: Throwable? = null

        vm.launchSafely(onError = { caught = it }) {
            throw IllegalStateException("DB 쓰기 실패")
        }
        advanceUntilIdle()

        assertTrue(caught is IllegalStateException)
        assertEquals("DB 쓰기 실패", caught?.message)
    }

    @Test
    fun `코루틴 취소는 onError로 전달되지 않고 다시 던져진다`() = runTest(dispatcher) {
        val vm = TestViewModel()
        var onErrorCalled = false

        vm.launchSafely(onError = { onErrorCalled = true }) {
            throw CancellationException("취소")
        }
        advanceUntilIdle()

        assertFalse(onErrorCalled)
    }

    @Test
    fun `onError를 지정하지 않아도 예외를 삼켜 크래시하지 않는다`() = runTest(dispatcher) {
        val vm = TestViewModel()

        // 예외를 던지는 블록을 onError 없이 실행 — 예외가 전파되면 이 테스트가 실패한다.
        val job = vm.launchSafely {
            throw RuntimeException("처리되지 않은 예외")
        }
        advanceUntilIdle()

        // 예외가 삼켜졌다면 코루틴은 취소가 아니라 정상 완료 상태여야 한다.
        assertTrue(job.isCompleted)
        assertFalse(job.isCancelled)
    }
}
