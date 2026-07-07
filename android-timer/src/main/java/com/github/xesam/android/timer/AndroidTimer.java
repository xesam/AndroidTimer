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
            long tickSnapshot;
            synchronized (AndroidTimer.this) {
                if (mStatus != TimerStatus.RUNNING) {
                    return true;
                }
                long now = SystemClock.elapsedRealtime();
                tickSnapshot = now - mMillisStarted - mPausedTotal;
            }
            // 回调在锁外派发，避免用户回调重入时引发状态惊奇
            onTick(tickSnapshot);
            // onTick 后重新获取锁决定下一次调度（用户回调可能已 pause/cancel）
            synchronized (AndroidTimer.this) {
                if (mStatus == TimerStatus.RUNNING) {
                    mNextTickTime += mMillisInterval;
                    scheduleNextTick();
                }
            }
            return true;
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

    public final void start() {
        boolean fireTickStart;
        synchronized (this) {
            if (mStatus == TimerStatus.RUNNING) {
                return;
            }
            mPausedTotal = 0;
            mMillisStarted = SystemClock.elapsedRealtime();
            mNextTickTime = mMillisStarted + mMillisInterval; // 设置下一次tick的时间
            mStatus = TimerStatus.RUNNING;
            fireTickStart = mOption.tickWhenStart;
            scheduleNextTick();
        }
        onStart(0);
        if (fireTickStart) {
            onTick(0);
        }
    }

    public final void pause() {
        long pauseArg;
        synchronized (this) {
            if (mStatus != TimerStatus.RUNNING) {
                return;
            }
            mHandler.removeMessages(MSG);
            mStatus = TimerStatus.PAUSED;

            mMillisPaused = SystemClock.elapsedRealtime();
            pauseArg = mMillisPaused - mMillisStarted - mPausedTotal;
        }
        onPause(pauseArg);
    }

    public final void resume() {
        long resumeArg;
        synchronized (this) {
            if (mStatus != TimerStatus.PAUSED) {
                return;
            }
            mStatus = TimerStatus.RUNNING;
            resumeArg = mMillisPaused - mMillisStarted - mPausedTotal;

            // 计算暂停期间的时间，并调整下一次tick时间
            long pauseDuration = SystemClock.elapsedRealtime() - mMillisPaused;
            mPausedTotal += pauseDuration;
            mNextTickTime += pauseDuration;

            scheduleNextTick();
        }
        onResume(resumeArg);
    }

    public final void cancel() {
        long cancelArg;
        synchronized (this) {
            if (mStatus == TimerStatus.IDLE) {
                return;
            }
            final int preState = mStatus;
            mStatus = TimerStatus.IDLE;
            mHandler.removeMessages(MSG);

            if (preState == TimerStatus.RUNNING) { //running -> cancel
                cancelArg = SystemClock.elapsedRealtime() - mMillisStarted - mPausedTotal;
            } else { //pause -> cancel
                cancelArg = mMillisPaused - mMillisStarted - mPausedTotal;
            }
        }
        onCancel(cancelArg);
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
