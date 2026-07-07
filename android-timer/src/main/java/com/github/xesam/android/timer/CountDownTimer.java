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
            long tickSnapshot = 0;
            boolean fireNormalTick = false;
            boolean fireFinish = false;
            synchronized (CountDownTimer.this) {
                if (mStatus != TimerStatus.RUNNING) {
                    return true;
                }
                final long millisLeft = mFinishTimeInFuture - SystemClock.elapsedRealtime();
                if (millisLeft <= 0) {
                    // 结束路径：仅由 tickWhenFinish 决定是否触发最终 onTick(0)
                    mStatus = TimerStatus.IDLE;
                    mHandler.removeMessages(MSG);
                    fireFinish = true;
                } else {
                    tickSnapshot = millisLeft;
                    fireNormalTick = true;
                }
            }

            // 回调在锁外派发，避免用户回调重入时引发状态惊奇
            if (fireNormalTick) {
                onTick(tickSnapshot);
                // onTick 后重新获取锁，决定下一次调度（可能直接结束）
                synchronized (CountDownTimer.this) {
                    if (mStatus == TimerStatus.RUNNING) {
                        long millisLeft = mFinishTimeInFuture - SystemClock.elapsedRealtime();
                        if (millisLeft < mMillisInterval) {
                            mHandler.sendEmptyMessageDelayed(MSG, millisLeft);
                        } else {
                            mNextTickTime += mMillisInterval;
                            if (scheduleNextTick()) {
                                fireFinish = true;
                            }
                        }
                    }
                }
            }
            if (fireFinish) {
                if (mOption.tickWhenFinish) {
                    onTick(0);
                }
                onFinish(0);
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
     * 增量调度下一次 tick；检测到倒计时结束时将状态置为 IDLE 并移除消息，
     * 返回 true 表示结束已发生（调用方负责在锁外触发 onTick(0)/onFinish）。
     */
    private boolean scheduleNextTick() {
        long now = SystemClock.elapsedRealtime();
        long delay = mNextTickTime - now;

        // 如果已经错过了预定时间，计算需要跳过的间隔数
        while (delay < 0) {
            mNextTickTime += mMillisInterval;
            delay = mNextTickTime - now;
        }
        if (mNextTickTime > mFinishTimeInFuture) {
            mStatus = TimerStatus.IDLE;
            mHandler.removeMessages(MSG);
            return true;
        }
        mHandler.sendEmptyMessageDelayed(MSG, delay);
        return false;
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

    public final void start() {
        boolean fireStart = false;
        long startArg = 0;
        boolean fireTickStart = false;
        long tickStartArg = 0;
        boolean fireFinish = false;
        synchronized (this) {
            if (mStatus == TimerStatus.RUNNING) {
                return;
            }
            if (mMillisFutureDuration <= 0) {
                mStatus = TimerStatus.IDLE;
                mHandler.removeMessages(MSG);
                fireFinish = true;
            } else {
                mPausedTotal = 0;
                mMillisStarted = SystemClock.elapsedRealtime();
                mFinishTimeInFuture = mMillisStarted + mMillisFutureDuration;
                mNextTickTime = mMillisStarted + mMillisInterval; // 设置第一次tick的绝对时间

                mStatus = TimerStatus.RUNNING;
                fireStart = true;
                startArg = mMillisFutureDuration;
                if (mOption.tickWhenStart) {
                    fireTickStart = true;
                    tickStartArg = mMillisFutureDuration;
                }
                if (scheduleNextTick()) {
                    fireFinish = true;
                }
            }
        }
        if (fireStart) {
            onStart(startArg);
        }
        if (fireTickStart) {
            onTick(tickStartArg);
        }
        if (fireFinish) {
            if (mOption.tickWhenFinish) {
                onTick(0);
            }
            onFinish(0);
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
            pauseArg = mFinishTimeInFuture - mMillisPaused;
        }
        onPause(pauseArg);
    }

    public final void resume() {
        long resumeArg;
        boolean fireFinish = false;
        synchronized (this) {
            if (mStatus != TimerStatus.PAUSED) {
                return;
            }
            mStatus = TimerStatus.RUNNING;
            resumeArg = mFinishTimeInFuture - mMillisPaused;

            long pauseDuration = SystemClock.elapsedRealtime() - mMillisPaused;
            mPausedTotal += pauseDuration;
            mFinishTimeInFuture = mMillisStarted + mMillisFutureDuration + mPausedTotal;
            mNextTickTime += pauseDuration; // 调整下一次tick时间
            if (scheduleNextTick()) {
                fireFinish = true;
            }
        }
        onResume(resumeArg);
        if (fireFinish) {
            if (mOption.tickWhenFinish) {
                onTick(0);
            }
            onFinish(0);
        }
    }

    public final void cancel() {
        long cancelArg;
        synchronized (this) {
            if (mStatus == TimerStatus.IDLE) {
                return;
            }
            final int preState = mStatus;
            mHandler.removeMessages(MSG);
            mStatus = TimerStatus.IDLE;

            if (preState == TimerStatus.RUNNING) { //running -> cancel
                cancelArg = mFinishTimeInFuture - SystemClock.elapsedRealtime();
            } else { //pause -> cancel
                cancelArg = mFinishTimeInFuture - mMillisPaused;
            }
        }
        onCancel(cancelArg);
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

    /**
     * 倒计时自然结束时调用。通过 cancel() 中止的倒计时不会触发此方法。
     * @param millisDuration 始终为 0，表示倒计时已结束
     */
    protected void onFinish(long millisDuration) {
    }
}
