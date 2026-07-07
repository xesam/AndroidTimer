package com.github.xesam.android.timer.kt

import com.github.xesam.android.timer.AndroidTimer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class KtAndroidTimer internal constructor(
    interval: Long,
    option: AndroidTimer.Option,
    private val onStartCallback: (Long) -> Unit,
    private val onTickCallback: (Long) -> Unit,
    private val onPauseCallback: (Long) -> Unit,
    private val onResumeCallback: (Long) -> Unit,
    private val onCancelCallback: (Long) -> Unit,
) : AndroidTimer(interval, option) {

    private companion object {
        // 高频 tick 场景下缓冲 64，避免消费者短暂阻塞时丢失 tick 事件
        private const val TICK_BUFFER_CAPACITY = 64
    }

    private val _tickFlow = MutableSharedFlow<Long>(extraBufferCapacity = TICK_BUFFER_CAPACITY)
    val tickFlow: Flow<Long> = _tickFlow.asSharedFlow()

    private val _stateFlow = MutableStateFlow(TimerState.IDLE)
    val stateFlow: StateFlow<TimerState> = _stateFlow.asStateFlow()

    /**
     * 当前状态（规范化入口）。Kotlin 消费者优先使用 [state] / [stateFlow]，
     * Java 继承的 getState()（返回 IntDef）仅供 Java 使用。
     */
    val state: TimerState get() = stateFlow.value

    override fun onStart(millisFly: Long) {
        _stateFlow.value = TimerState.RUNNING
        onStartCallback(millisFly)
    }

    override fun onTick(millisFly: Long) {
        _tickFlow.tryEmit(millisFly)
        onTickCallback(millisFly)
    }

    override fun onPause(millisFly: Long) {
        _stateFlow.value = TimerState.PAUSED
        onPauseCallback(millisFly)
    }

    override fun onResume(millisFly: Long) {
        _stateFlow.value = TimerState.RUNNING
        onResumeCallback(millisFly)
    }

    override fun onCancel(millisFly: Long) {
        _stateFlow.value = TimerState.IDLE
        onCancelCallback(millisFly)
    }
}
