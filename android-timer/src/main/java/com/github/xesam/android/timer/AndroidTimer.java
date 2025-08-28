package com.github.xesam.android.timer;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.NonNull;

public class AndroidTimer {
    public static final class Option {
        static final Option DEFAULT = new Option();
        private final boolean tickWhenStart;

        public Option() {
            this(false);
        }

        public Option(boolean tickWhenStart) {
            this.tickWhenStart = tickWhenStart;
        }
    }

    private final long mMillisInterval;
    private final Option mOption;
    private long mMillisStarted = -1;
    private long mMillisPaused = -1;
    private long mPausedTotal = 0;
    private long mNextTickTime; // 绝对时间基准

    @TimerStatus.Enum
    private volatile int mStatus = TimerStatus.IDLE;

    private static final int MSG = 1;
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            synchronized (AndroidTimer.this) {
                if (mStatus != TimerStatus.RUNNING) {
                    return true;
                }

                long now = SystemClock.elapsedRealtime();
                onTick(now - mMillisStarted - mPausedTotal);

                if (mStatus == TimerStatus.RUNNING) {
                    mNextTickTime += mMillisInterval;
                    scheduleNextTick();
                }
                return true;
            }
        }
    });

    public AndroidTimer(long interval) {
        this(interval, Option.DEFAULT);
    }

    public AndroidTimer(long interval, Option option) {
        if (interval <= 0) {
            throw new IllegalArgumentException("间隔时间必须大于0");
        }
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

        mHandler.sendEmptyMessageDelayed(MSG, delay);
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
        mPausedTotal = 0;
        mMillisStarted = SystemClock.elapsedRealtime();
        mNextTickTime = mMillisStarted + mMillisInterval; // 设置下一次tick的时间
        mStatus = TimerStatus.RUNNING;
        onStart(0);
        if (mOption.tickWhenStart) {
            onTick(0);
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
        onPause(mMillisPaused - mMillisStarted - mPausedTotal);
    }

    public final synchronized void resume() {
        if (mStatus != TimerStatus.PAUSED) {
            return;
        }
        mStatus = TimerStatus.RUNNING;

        onResume(mMillisPaused - mMillisStarted - mPausedTotal);

        // 计算暂停期间的时间，并调整下一次tick时间
        long pauseDuration = SystemClock.elapsedRealtime() - mMillisPaused;
        mPausedTotal += pauseDuration;
        mNextTickTime += pauseDuration;

        scheduleNextTick();
    }

    public final synchronized void cancel() {
        if (mStatus == TimerStatus.IDLE) {
            return;
        }
        final int preState = mStatus;
        mStatus = TimerStatus.IDLE;
        mHandler.removeMessages(MSG);

        if (preState == TimerStatus.RUNNING) { //running -> cancel
            onCancel(SystemClock.elapsedRealtime() - mMillisStarted - mPausedTotal);
        } else if (preState == TimerStatus.PAUSED) { //pause -> cancel
            onCancel(mMillisPaused - mMillisStarted - mPausedTotal);
        }
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    protected void onStart(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    protected void onCancel(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    protected void onPause(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    protected void onResume(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    protected void onTick(long millisFly) {
    }

}
