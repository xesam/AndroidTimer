package com.github.xesam.android.timer.kt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class KtCountDownTimerTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `initial stateFlow is IDLE`() {
        val timer = countDownTimer(100L, 1L)
        assertEquals(TimerState.IDLE, timer.stateFlow.value)
    }

    @Test
    fun `start transitions stateFlow to RUNNING`() {
        val timer = countDownTimer(100L, 1L)
        timer.start()
        assertEquals(TimerState.RUNNING, timer.stateFlow.value)
        timer.cancel()
    }

    @Test
    fun `pause transitions stateFlow to PAUSED`() {
        val timer = countDownTimer(100L, 1L)
        timer.start()
        timer.pause()
        assertEquals(TimerState.PAUSED, timer.stateFlow.value)
        timer.cancel()
    }

    @Test
    fun `cancel transitions stateFlow to IDLE`() {
        val timer = countDownTimer(100L, 1L)
        timer.start()
        timer.cancel()
        assertEquals(TimerState.IDLE, timer.stateFlow.value)
    }

    @Test
    fun `onFinish lambda is called when countdown ends`() {
        var finished = false
        val timer = countDownTimer(10L, 1L, onFinish = { finished = true })
        timer.start()
        repeat(15) { ShadowLooper.runUiThreadTasksIncludingDelayedTasks() }
        assertTrue(finished)
    }

    @Test
    fun `stateFlow returns to IDLE after countdown finishes`() {
        val timer = countDownTimer(10L, 1L)
        timer.start()
        repeat(15) { ShadowLooper.runUiThreadTasksIncludingDelayedTasks() }
        assertEquals(TimerState.IDLE, timer.stateFlow.value)
    }

    @Test
    fun `tickFlow emits during countdown`() {
        val emitted = mutableListOf<Long>()
        val timer = countDownTimer(100L, 1L)
        scope.launch { timer.tickFlow.collect { emitted.add(it) } }

        timer.start()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertTrue(emitted.isNotEmpty())
        timer.cancel()
    }

    @Test
    fun `finishFlow emits once when countdown ends`() {
        var emitCount = 0
        val timer = countDownTimer(10L, 1L)
        scope.launch { timer.finishFlow.collect { emitCount++ } }

        timer.start()
        repeat(15) { ShadowLooper.runUiThreadTasksIncludingDelayedTasks() }

        assertEquals(1, emitCount)
    }

    @Test
    fun `onTick lambda receives remaining milliseconds`() {
        var lastMs = Long.MAX_VALUE
        val timer = countDownTimer(100L, 1L, onTick = { ms -> lastMs = ms })
        timer.start()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertTrue(lastMs < 100L)
        timer.cancel()
    }

    @Test
    fun `stateFlow reflects full lifecycle`() {
        val timer = countDownTimer(100L, 1L)
        assertEquals(TimerState.IDLE, timer.stateFlow.value)

        timer.start()
        assertEquals(TimerState.RUNNING, timer.stateFlow.value)

        timer.pause()
        assertEquals(TimerState.PAUSED, timer.stateFlow.value)

        timer.resume()
        assertEquals(TimerState.RUNNING, timer.stateFlow.value)

        timer.cancel()
        assertEquals(TimerState.IDLE, timer.stateFlow.value)
    }
}
