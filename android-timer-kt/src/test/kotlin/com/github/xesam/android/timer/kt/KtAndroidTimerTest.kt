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
class KtAndroidTimerTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `initial stateFlow is IDLE`() {
        val timer = androidTimer(1L)
        assertEquals(TimerState.IDLE, timer.stateFlow.value)
    }

    @Test
    fun `start transitions stateFlow to RUNNING`() {
        val timer = androidTimer(1L)
        timer.start()
        assertEquals(TimerState.RUNNING, timer.stateFlow.value)
        timer.cancel()
    }

    @Test
    fun `pause transitions stateFlow to PAUSED`() {
        val timer = androidTimer(1L)
        timer.start()
        timer.pause()
        assertEquals(TimerState.PAUSED, timer.stateFlow.value)
        timer.cancel()
    }

    @Test
    fun `resume transitions stateFlow back to RUNNING`() {
        val timer = androidTimer(1L)
        timer.start()
        timer.pause()
        timer.resume()
        assertEquals(TimerState.RUNNING, timer.stateFlow.value)
        timer.cancel()
    }

    @Test
    fun `cancel transitions stateFlow to IDLE`() {
        val timer = androidTimer(1L)
        timer.start()
        timer.cancel()
        assertEquals(TimerState.IDLE, timer.stateFlow.value)
    }

    @Test
    fun `onStart lambda is called when started`() {
        var called = false
        val timer = androidTimer(1L, onStart = { called = true })
        timer.start()
        assertTrue(called)
        timer.cancel()
    }

    @Test
    fun `onTick lambda is called on each tick`() {
        var count = 0
        val timer = androidTimer(1L, onTick = { count++ })
        timer.start()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertTrue(count > 0)
        timer.cancel()
    }

    @Test
    fun `onPause lambda is called when paused`() {
        var called = false
        val timer = androidTimer(1L, onPause = { called = true })
        timer.start()
        timer.pause()
        assertTrue(called)
        timer.cancel()
    }

    @Test
    fun `onCancel lambda is called when cancelled`() {
        var called = false
        val timer = androidTimer(1L, onCancel = { called = true })
        timer.start()
        timer.cancel()
        assertTrue(called)
    }

    @Test
    fun `tickFlow emits on each tick`() {
        val emitted = mutableListOf<Long>()
        val timer = androidTimer(1L)
        scope.launch { timer.tickFlow.collect { emitted.add(it) } }

        timer.start()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertTrue(emitted.isNotEmpty())
        timer.cancel()
    }

    @Test
    fun `stateFlow reflects full lifecycle`() {
        val timer = androidTimer(1L)
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
