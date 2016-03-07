package dev.xesam.android.toolbox.timer;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * 倒计时器
 * <p/>
 * xesamguo@gmail.com
 */
public class CountDownTimer {

    private static final int NOT_START = -1;
    /**
     * Millis since epoch when alarm should stop.
     */
    private final long mMillisInFuture;

    private final long mCountdownInterval;

    private long mStopTimeInFuture;

    private long mMillisStart = NOT_START;
    private long mMillisPause;
    private long mMillisLastTickStart;

    private long mTotalPausedFly;

    /**
     * boolean representing if the timer was cancelled
     */
    private boolean mCancelled = true;
    private boolean mRunning = false;

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public CountDownTimer(long millisInFuture, long countDownInterval) {
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;
    }

    /**
     * Start the countdown.
     */
    public synchronized CountDownTimer start() {
        if (mMillisInFuture <= 0) {
            onFinish();
            return this;
        }
        mCancelled = false;
        mRunning = true;
        mTotalPausedFly = 0;
        mMillisStart = SystemClock.elapsedRealtime();
        mStopTimeInFuture = mMillisStart + mMillisInFuture;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    public synchronized void pause() {
        if (mCancelled || !mRunning) {
            return;
        }
        mRunning = false;

        mMillisPause = SystemClock.elapsedRealtime();
        mHandler.removeMessages(MSG);
        onPause(mStopTimeInFuture - mMillisPause);
    }

    public synchronized void resume() {
        if (mCancelled || mRunning) {
            return;
        }
        mRunning = true;

        long delay = mCountdownInterval - (mMillisPause - mMillisLastTickStart);
        while (delay < 0) {
            delay += mCountdownInterval;
        }
        final long tmpStopTimeInFuture = mStopTimeInFuture;
        mTotalPausedFly += SystemClock.elapsedRealtime() - mMillisPause;
        mStopTimeInFuture = mMillisStart + mMillisInFuture + mTotalPausedFly;
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG), delay);
        onResume(tmpStopTimeInFuture - mMillisPause);
    }

    /**
     * Cancel the countdown.
     */
    public synchronized void cancel() {
        if (mMillisStart == NOT_START) {
            return;
        }
        mCancelled = true;
        mHandler.removeMessages(MSG);

        if (mRunning) { //running -> cancel
            onCancel(mStopTimeInFuture - SystemClock.elapsedRealtime());
        } else { //pause -> cancel
            onCancel(mStopTimeInFuture - mMillisPause);
        }
        mRunning = false;
        mMillisStart = NOT_START;
    }

    public void onPause(long millisUntilFinished) {
    }

    public void onResume(long millisUntilFinished) {
    }

    /**
     * Callback fired when the cancel.
     *
     * @param millisUntilFinished The amount of time until finished.
     */
    public void onCancel(long millisUntilFinished) {
    }

    /**
     * Callback fired on regular interval.
     *
     * @param millisUntilFinished The amount of time until finished.
     */
    public void onTick(long millisUntilFinished) {
    }


    /**
     * Callback fired when the time is up.
     */
    public void onFinish() {
    }


    private static final int MSG = 1;


    // handles counting down
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (CountDownTimer.this) {
                if (mCancelled || !mRunning) {
                    return;
                }

                final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();

                if (millisLeft <= 0) {
                    mRunning = false;
                    mCancelled = true;
                    onTick(0);
                    onFinish();
                } else if (millisLeft < mCountdownInterval) {
                    // no tick, just delay until done
                    sendMessageDelayed(obtainMessage(MSG), millisLeft);
                } else {
                    mMillisLastTickStart = SystemClock.elapsedRealtime();
                    onTick(millisLeft);

                    // take into account user's onTick taking time to execute
                    long delay = mMillisLastTickStart + mCountdownInterval - SystemClock.elapsedRealtime();

                    // special case: user's onTick took more than interval to
                    // complete, skip to next interval
                    while (delay < 0) {
                        delay += mCountdownInterval;
                    }

                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };

}
