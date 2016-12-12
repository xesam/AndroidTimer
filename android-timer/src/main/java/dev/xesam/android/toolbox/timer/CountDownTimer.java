package dev.xesam.android.toolbox.timer;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * 倒计时器
 * <p>
 * xesamguo@gmail.com
 */
public class CountDownTimer {

    private static final int NOT_START = -1;
    /**
     * Millis since epoch when alarm should stop.
     */
    private final long mMillisInFuture;

    private final long mInterval;

    private long mStopTimeInFuture;

    private long mMillisStart = NOT_START;
    private long mMillisPause;
    private long mMillisLastTickStart;

    private long mTotalPausedFly;
    /**
     * representing the timer state
     */
    private volatile int mState = State.TIMER_NOT_START;

    public CountDownTimer(long millisInFuture, long countDownInterval) {
        mMillisInFuture = millisInFuture;
        mInterval = countDownInterval;
    }

    /**
     * Start the countdown.
     */
    public synchronized void start() {
        if (mState == State.TIMER_RUNNING) {
            return;
        }
        if (mMillisInFuture <= 0) {
            onFinish();
            return;
        }
        mTotalPausedFly = 0;
        mMillisStart = SystemClock.elapsedRealtime();
        mState = State.TIMER_RUNNING;
        mStopTimeInFuture = mMillisStart + mMillisInFuture;

        onStart(mMillisInFuture);
        mHandler.sendEmptyMessage(MSG);
    }

    public synchronized void pause() {
        if (mState != State.TIMER_RUNNING) {
            return;
        }
        mHandler.removeMessages(MSG);
        mState = State.TIMER_PAUSED;

        mMillisPause = SystemClock.elapsedRealtime();
        onPause(mStopTimeInFuture - mMillisPause);
    }

    public synchronized void resume() {
        if (mState != State.TIMER_PAUSED) {
            return;
        }
        mState = State.TIMER_RUNNING;
        onResume(mStopTimeInFuture - mMillisPause);

        long delay = mInterval - (mMillisPause - mMillisLastTickStart);
        mTotalPausedFly += SystemClock.elapsedRealtime() - mMillisPause;
        mStopTimeInFuture = mMillisStart + mMillisInFuture + mTotalPausedFly;
        mHandler.sendEmptyMessageDelayed(MSG, delay);
    }

    /**
     * Cancel the countdown.
     */
    public synchronized void cancel() {
        if (mState == State.TIMER_NOT_START) {
            return;
        }
        final int preState = mState;
        mHandler.removeMessages(MSG);
        mState = State.TIMER_NOT_START;

        if (preState == State.TIMER_RUNNING) { //running -> cancel
            onCancel(mStopTimeInFuture - SystemClock.elapsedRealtime());
        } else if (preState == State.TIMER_PAUSED) { //pause -> cancel
            onCancel(mStopTimeInFuture - mMillisPause);
        }
    }

    public int getState() {
        return mState;
    }

    protected void onStart(long millisUntilFinished) {
    }

    protected void onPause(long millisUntilFinished) {
    }

    protected void onResume(long millisUntilFinished) {
    }

    protected void onCancel(long millisUntilFinished) {
    }

    /**
     * Callback fired on regular interval.
     *
     * @param millisUntilFinished The amount of time until finished.
     */
    protected void onTick(long millisUntilFinished) {
    }

    /**
     * Callback fired when the time is up.
     */
    public void onFinish() {
    }

    private static final int MSG = 1;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            synchronized (CountDownTimer.this) {
                if (mState != State.TIMER_RUNNING) {
                    return true;
                }

                final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();

                if (millisLeft <= 0) {
                    onTick(0);
                    mState = State.TIMER_NOT_START;
                    onFinish();
                } else if (millisLeft < mInterval) {
                    // no tick, just delay until done
                    mHandler.sendEmptyMessageDelayed(MSG, millisLeft);
                } else {
                    mMillisLastTickStart = SystemClock.elapsedRealtime();
                    onTick(millisLeft);
                    if (mState != State.TIMER_RUNNING) {
                        return true;
                    }
                    // take into account user's onTick taking time to execute
                    long delay = mMillisLastTickStart + mInterval - SystemClock.elapsedRealtime();

                    // special case: user's onTick took more than interval to
                    // complete, skip to next interval
                    while (delay < 0) {
                        delay += mInterval;
                    }

                    mHandler.sendEmptyMessageDelayed(MSG, delay);
                }
            }
            return true;
        }
    });
}
