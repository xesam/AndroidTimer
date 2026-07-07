package com.github.xesam.android.timer.kt

import com.github.xesam.android.timer.CountDownTimer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class KtCountDownTimer internal constructor(
    duration: Long,
    interval: Long,
    option: CountDownTimer.Option,
    private val onStartCallback: (Long) -> Unit,
    private val onTickCallback: (Long) -> Unit,
    private val onPauseCallback: (Long) -> Unit,
    private val onResumeCallback: (Long) -> Unit,
    private val onCancelCallback: (Long) -> Unit,
    private val onFinishCallback: () -> Unit,
) : CountDownTimer(duration, interval, option) {

    private companion object {
        // 高频 tick 场景下缓冲 64，避免消费者短暂阻塞时丢失 tick 事件
        private const val TICK_BUFFER_CAPACITY = 64
        // finish 为单次事件，缓冲 1 足够
        private const val FINISH_BUFFER_CAPACITY = 1
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

    private val _finishFlow = MutableSharedFlow<Unit>(extraBufferCapacity = FINISH_BUFFER_CAPACITY)
    val finishFlow: Flow<Unit> = _finishFlow.asSharedFlow()

    override fun onStart(millisUntilFinished: Long) {
        _stateFlow.value = TimerState.RUNNING
        onStartCallback(millisUntilFinished)
    }

    override fun onTick(millisUntilFinished: Long) {
        _tickFlow.tryEmit(millisUntilFinished)
        onTickCallback(millisUntilFinished)
    }

    override fun onPause(millisUntilFinished: Long) {
        _stateFlow.value = TimerState.PAUSED
        onPauseCallback(millisUntilFinished)
    }

    override fun onResume(millisUntilFinished: Long) {
        _stateFlow.value = TimerState.RUNNING
        onResumeCallback(millisUntilFinished)
    }

    override fun onCancel(millisUntilFinished: Long) {
        _stateFlow.value = TimerState.IDLE
        onCancelCallback(millisUntilFinished)
    }

    override fun onFinish(millisDuration: Long) {
        _stateFlow.value = TimerState.IDLE
        _finishFlow.tryEmit(Unit)
        onFinishCallback()
    }
}
