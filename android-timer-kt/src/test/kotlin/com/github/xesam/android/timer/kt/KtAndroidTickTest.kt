package com.github.xesam.android.timer.kt

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class KtAndroidTickTest {

    @Test
    fun `onTick lambda is called after delay`() {
        var receivedCount = 0
        val tick = androidTick(100L, onTick = { receivedCount = it })
        tick.tick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertEquals(1, receivedCount)
    }

    @Test
    fun `second tick without forceCancel is ignored while pending`() {
        var receivedCount = 0
        val tick = androidTick(100L, onTick = { receivedCount = it })
        tick.tick()
        tick.tick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertEquals(1, receivedCount)
    }

    @Test
    fun `tick with forceCancel restarts and fires once`() {
        var receivedCount = 0
        val tick = androidTick(100L, onTick = { receivedCount = it })
        tick.tick()
        tick.tick(true)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertEquals(1, receivedCount)
    }

    @Test
    fun `cancel prevents pending callback from firing`() {
        var receivedCount = 0
        val tick = androidTick(100L, onTick = { receivedCount = it })
        tick.tick()
        tick.cancel()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertEquals(0, receivedCount)
    }

    @Test
    fun `count increments across multiple sequential ticks`() {
        var lastCount = 0
        val tick = androidTick(100L, onTick = { lastCount = it })
        repeat(3) {
            tick.tick()
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        }
        assertEquals(3, lastCount)
    }
}
