package com.github.xesam.android.timer;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.NonNull;

public class CountDownTimer {
    public static final class Option {
        static final Option DEFAULT = new Option();
        private final boolean tickWhenStart;
        private final boolean tickWhenFinish;

        public Option() {
            this(false, false);
        }

        public Option(boolean tickWhenStart, boolean tickWhenFinish) {
            this.tickWhenStart = tickWhenStart;
            this.tickWhenFinish = tickWhenFinish;
        }
    }

    private final long mMillisFutureDuration;
    private final long mMillisInterval;
    private final Option mOption;
    private long mMillisStarted = -1;
    private long mMillisPaused = -1;
    private long mPausedTotal = 0;
    private long mFinishTimeInFuture;
    private long mNextTickTime; // 绝对时间基准
    @TimerStatus.Enum
    private volatile int mStatus = TimerStatus.IDLE;
    private static final int MSG = 1;

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            synchronized (CountDownTimer.this) {
                if (mStatus != TimerStatus.RUNNING) {
                    return true;
                }
                final long millisLeft = mFinishTimeInFuture - SystemClock.elapsedRealtime();
                if (millisLeft <= 0) {
                    onTick(0);
                    triggerFinish();
                    return true;
                }
                onTick(millisLeft);
                if (mStatus != TimerStatus.RUNNING) {
                    return true;
                }
                if (millisLeft < mMillisInterval) {
                    mHandler.sendEmptyMessageDelayed(MSG, millisLeft);
                } else {
                    // 更新下一次tick的绝对时间
                    mNextTickTime += mMillisInterval;
                    scheduleNextTick();
                }
            }
            return true;
        }
    });

    public CountDownTimer(long millisDuration, long interval) {
        this(millisDuration, interval, Option.DEFAULT);
    }

    public CountDownTimer(long millisDuration, long interval, Option option) {
        if (millisDuration <= 0) {
            throw new IllegalArgumentException("倒计时时长必须大于0");
        }
        if (interval <= 0) {
            throw new IllegalArgumentException("间隔时间必须大于0");
        }
        mMillisFutureDuration = millisDuration;
        mMillisInterval = interval;
        mOption = option;
    }

    /**
     * 基于绝对时间调度下一次tick
     */
    private void scheduleNextTick() {
        long now = SystemClock.elapsedRealtime();
        long delay = mNextTickTime - now;

        // 如果已经错过了预定时间，计算需要跳过的间隔数
        while (delay < 0) {
            mNextTickTime += mMillisInterval;
            delay = mNextTickTime - now;
        }
        if (mNextTickTime > mFinishTimeInFuture) {
            this.triggerFinish();
        } else {
            mHandler.sendEmptyMessageDelayed(MSG, delay);
        }
    }

    private void triggerFinish() {
        mStatus = TimerStatus.IDLE;
        mHandler.removeMessages(MSG);
        if (mOption.tickWhenFinish) {
            onTick(0);
        }
        onFinish(0);
    }

    public final long getFutureDuration() {
        return mMillisFutureDuration;
    }

    public final long getInterval() {
        return mMillisInterval;
    }

    public final @TimerStatus.Enum int getState() {
        return mStatus;
    }

    public final synchronized void start() {
        if (mStatus == TimerStatus.RUNNING) {
            return;
        }
        if (mMillisFutureDuration <= 0) {
            this.triggerFinish();
            return;
        }
        mPausedTotal = 0;
        mMillisStarted = SystemClock.elapsedRealtime();
        mFinishTimeInFuture = mMillisStarted + mMillisFutureDuration;
        mNextTickTime = mMillisStarted + mMillisInterval; // 设置第一次tick的绝对时间

        mStatus = TimerStatus.RUNNING;
        onStart(mMillisFutureDuration);
        if (mOption.tickWhenStart) {
            onTick(mMillisFutureDuration);
        }
        scheduleNextTick();
    }

    public final synchronized void pause() {
        if (mStatus != TimerStatus.RUNNING) {
            return;
        }
        mHandler.removeMessages(MSG);
        mStatus = TimerStatus.PAUSED;

        mMillisPaused = SystemClock.elapsedRealtime();
        onPause(mFinishTimeInFuture - mMillisPaused);
    }

    public final synchronized void resume() {
        if (mStatus != TimerStatus.PAUSED) {
            return;
        }
        mStatus = TimerStatus.RUNNING;
        onResume(mFinishTimeInFuture - mMillisPaused);

        long pauseDuration = SystemClock.elapsedRealtime() - mMillisPaused;
        mPausedTotal += pauseDuration;
        mFinishTimeInFuture = mMillisStarted + mMillisFutureDuration + mPausedTotal;
        mNextTickTime += pauseDuration; // 调整下一次tick时间
        scheduleNextTick();
    }

    public final synchronized void cancel() {
        if (mStatus == TimerStatus.IDLE) {
            return;
        }
        final int preState = mStatus;
        mHandler.removeMessages(MSG);
        mStatus = TimerStatus.IDLE;

        if (preState == TimerStatus.RUNNING) { //running -> cancel
            onCancel(mFinishTimeInFuture - SystemClock.elapsedRealtime());
        } else if (preState == TimerStatus.PAUSED) { //pause -> cancel
            onCancel(mFinishTimeInFuture - mMillisPaused);
        }
    }

    protected void onStart(long millisUntilFinished) {
    }

    protected void onPause(long millisUntilFinished) {
    }

    protected void onResume(long millisUntilFinished) {
    }

    protected void onCancel(long millisUntilFinished) {
    }

    protected void onTick(long millisUntilFinished) {
    }

    protected void onFinish(long millisUntilFinished) {
    }
}
