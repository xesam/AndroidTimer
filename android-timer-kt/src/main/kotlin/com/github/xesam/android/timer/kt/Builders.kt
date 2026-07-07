package com.github.xesam.android.timer.kt

import com.github.xesam.android.timer.AndroidTimer
import com.github.xesam.android.timer.CountDownTimer

fun androidTimer(
    interval: Long,
    option: AndroidTimer.Option = AndroidTimer.Option(),
    onStart: (Long) -> Unit = {},
    onTick: (Long) -> Unit = {},
    onPause: (Long) -> Unit = {},
    onResume: (Long) -> Unit = {},
    onCancel: (Long) -> Unit = {},
): KtAndroidTimer = KtAndroidTimer(
    interval = interval,
    option = option,
    onStartCallback = onStart,
    onTickCallback = onTick,
    onPauseCallback = onPause,
    onResumeCallback = onResume,
    onCancelCallback = onCancel,
)

// 构建器 DSL 有意使用多个命名参数，对 LongParameterList 显式豁免
@Suppress("LongParameterList")
fun countDownTimer(
    duration: Long,
    interval: Long,
    option: CountDownTimer.Option = CountDownTimer.Option(),
    onStart: (Long) -> Unit = {},
    onTick: (Long) -> Unit = {},
    onPause: (Long) -> Unit = {},
    onResume: (Long) -> Unit = {},
    onCancel: (Long) -> Unit = {},
    onFinish: () -> Unit = {},
): KtCountDownTimer = KtCountDownTimer(
    duration = duration,
    interval = interval,
    option = option,
    onStartCallback = onStart,
    onTickCallback = onTick,
    onPauseCallback = onPause,
    onResumeCallback = onResume,
    onCancelCallback = onCancel,
    onFinishCallback = onFinish,
)

fun androidTick(
    delay: Long,
    onTick: (count: Int) -> Unit,
): KtAndroidTick = KtAndroidTick(
    delay = delay,
    onTickCallback = onTick,
)
