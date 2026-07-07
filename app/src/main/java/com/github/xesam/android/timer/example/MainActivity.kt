package com.github.xesam.android.timer.example

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.xesam.android.timer.AndroidTick
import com.github.xesam.android.timer.AndroidTimer
import com.github.xesam.android.timer.CountDownTimer
import com.github.xesam.android.timer.TimerStatus
import com.github.xesam.android.timer.demo.R
import com.github.xesam.android.timer.kt.KtAndroidTick
import com.github.xesam.android.timer.kt.KtAndroidTimer
import com.github.xesam.android.timer.kt.KtCountDownTimer
import com.github.xesam.android.timer.kt.TimerState
import com.github.xesam.android.timer.kt.androidTick
import com.github.xesam.android.timer.kt.androidTimer
import com.github.xesam.android.timer.kt.countDownTimer
import com.github.xesam.android.timer.demo.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var javaTimer: AndroidTimer
    private lateinit var javaCountdown: CountDownTimer
    private lateinit var javaTick: AndroidTick

    private lateinit var ktTimer: KtAndroidTimer
    private lateinit var ktCountdown: KtCountDownTimer
    private lateinit var ktTick: KtAndroidTick

    private var ktFinishCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTimers()
        setupJavaButtons()
        setupKtFlows()
        setupKtButtons()
    }

    private fun initTimers() {
        javaTimer = object : AndroidTimer(1000L) {
            override fun onTick(millisFly: Long) {
                updateJavaTimerState()
                binding.textTimerJava.text = "${millisFly} ms"
            }
            override fun onStart(millisFly: Long) {
                updateJavaTimerState()
                binding.textTimerJava.text = "0 ms"
            }
            override fun onPause(millisFly: Long) {
                updateJavaTimerState()
                binding.textTimerJava.text = "${millisFly} ms"
            }
            override fun onResume(millisFly: Long) {
                updateJavaTimerState()
            }
            override fun onCancel(millisFly: Long) {
                updateJavaTimerState()
                binding.textTimerJava.text = "0 ms"
            }
        }

        javaCountdown = object : CountDownTimer(5_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                updateJavaCountdownState()
                binding.textCountdownJava.text = "${millisUntilFinished} ms"
            }
            override fun onStart(millisUntilFinished: Long) {
                updateJavaCountdownState()
                binding.textCountdownJava.text = "${millisUntilFinished} ms"
            }
            override fun onPause(millisUntilFinished: Long) {
                updateJavaCountdownState()
                binding.textCountdownJava.text = "${millisUntilFinished} ms"
            }
            override fun onResume(millisUntilFinished: Long) {
                updateJavaCountdownState()
            }
            override fun onCancel(millisUntilFinished: Long) {
                updateJavaCountdownState()
                binding.textCountdownJava.text = "${millisUntilFinished} ms"
            }
            override fun onFinish(millisDuration: Long) {
                updateJavaCountdownState()
                binding.textCountdownJava.text = "完成"
            }
        }

        javaTick = object : AndroidTick(1_000L) {
            override fun onTick(thisInstance: AndroidTick, count: Int) {
                binding.textTickJava.text = "count: $count"
            }
        }

        ktTimer = androidTimer(1000L)
        ktCountdown = countDownTimer(5_000L, 1_000L)
        ktTick = androidTick(1_000L, onTick = { count ->
            binding.textTickKt.text = "count: $count"
        })
    }

    private fun setupJavaButtons() {
        binding.btnTimerStart.setOnClickListener { javaTimer.start() }
        binding.btnTimerPause.setOnClickListener { javaTimer.pause() }
        binding.btnTimerResume.setOnClickListener { javaTimer.resume() }
        binding.btnTimerCancel.setOnClickListener { javaTimer.cancel() }

        binding.btnCountdownStart.setOnClickListener { javaCountdown.start() }
        binding.btnCountdownPause.setOnClickListener { javaCountdown.pause() }
        binding.btnCountdownResume.setOnClickListener { javaCountdown.resume() }
        binding.btnCountdownCancel.setOnClickListener { javaCountdown.cancel() }

        binding.btnTickStart.setOnClickListener { javaTick.tick() }
        binding.btnTickForce.setOnClickListener { javaTick.tick(true) }
        binding.btnTickCancel.setOnClickListener { javaTick.cancel() }
    }

    private fun setupKtFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    ktTimer.stateFlow.collect {
                        binding.textTimerKtState.text = it.label()
                        binding.dotTimerKt.backgroundTintList = ColorStateList.valueOf(it.color())
                    }
                }
                launch { ktTimer.tickFlow.collect { binding.textTimerKtTick.text = "${it} ms" } }
                launch {
                    ktCountdown.stateFlow.collect {
                        binding.textCountdownKtState.text = it.label()
                        binding.dotCountdownKt.backgroundTintList = ColorStateList.valueOf(it.color())
                    }
                }
                launch { ktCountdown.tickFlow.collect { binding.textCountdownKtTick.text = "${it} ms" } }
                launch {
                    ktCountdown.finishFlow.collect {
                        binding.textCountdownKtFinish.text = "finished ×${++ktFinishCount}"
                    }
                }
            }
        }
    }

    private fun setupKtButtons() {
        binding.btnKtTimerStart.setOnClickListener { ktTimer.start() }
        binding.btnKtTimerPause.setOnClickListener { ktTimer.pause() }
        binding.btnKtTimerResume.setOnClickListener { ktTimer.resume() }
        binding.btnKtTimerCancel.setOnClickListener { ktTimer.cancel() }

        binding.btnKtCountdownStart.setOnClickListener { ktCountdown.start() }
        binding.btnKtCountdownPause.setOnClickListener { ktCountdown.pause() }
        binding.btnKtCountdownResume.setOnClickListener { ktCountdown.resume() }
        binding.btnKtCountdownCancel.setOnClickListener { ktCountdown.cancel() }

        binding.btnKtTickStart.setOnClickListener { ktTick.tick() }
        binding.btnKtTickForce.setOnClickListener { ktTick.tick(true) }
        binding.btnKtTickCancel.setOnClickListener { ktTick.cancel() }
    }

    private fun updateJavaTimerState() {
        val state = javaTimer.getState()
        binding.textTimerJavaState.text = stateName(state)
        binding.dotTimerJava.backgroundTintList = ColorStateList.valueOf(stateColor(state))
    }

    private fun updateJavaCountdownState() {
        val state = javaCountdown.getState()
        binding.textCountdownJavaState.text = stateName(state)
        binding.dotCountdownJava.backgroundTintList = ColorStateList.valueOf(stateColor(state))
    }

    private fun stateName(@TimerStatus.Enum state: Int): String = when (state) {
        TimerStatus.RUNNING -> getString(R.string.state_running)
        TimerStatus.PAUSED -> getString(R.string.state_paused)
        else -> getString(R.string.state_idle)
    }

    private fun stateColor(@TimerStatus.Enum state: Int): Int = when (state) {
        TimerStatus.RUNNING -> ContextCompat.getColor(this, R.color.state_running)
        TimerStatus.PAUSED -> ContextCompat.getColor(this, R.color.state_paused)
        else -> ContextCompat.getColor(this, R.color.state_idle)
    }

    private fun TimerState.label(): String = when (this) {
        TimerState.RUNNING -> getString(R.string.state_running)
        TimerState.PAUSED -> getString(R.string.state_paused)
        TimerState.IDLE -> getString(R.string.state_idle)
    }

    private fun TimerState.color(): Int = when (this) {
        TimerState.RUNNING -> ContextCompat.getColor(this@MainActivity, R.color.state_running)
        TimerState.PAUSED -> ContextCompat.getColor(this@MainActivity, R.color.state_paused)
        TimerState.IDLE -> ContextCompat.getColor(this@MainActivity, R.color.state_idle)
    }

    override fun onDestroy() {
        super.onDestroy()
        javaTimer.cancel()
        javaCountdown.cancel()
        javaTick.cancel()
        ktTimer.cancel()
        ktCountdown.cancel()
        ktTick.cancel()
    }
}
