package com.github.xesam.android.timer.kt

import com.github.xesam.android.timer.AndroidTick

class KtAndroidTick internal constructor(
    delay: Long,
    private val onTickCallback: (Int) -> Unit,
) : AndroidTick(delay) {

    override fun onTick(thisInstance: AndroidTick, count: Int) {
        onTickCallback(count)
    }
}
