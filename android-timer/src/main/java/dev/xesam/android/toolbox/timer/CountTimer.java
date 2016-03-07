package dev.xesam.android.toolbox.timer;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * 计时器
 * <p>
 * xesamguo@gmail.com
 */
public class CountTimer {

    private static final int NOT_START = -1;

    private final long mCountInterval;

    private long mTotalPausedFly;

    private long mMillisStart = NOT_START;
    private long mMillisPause;
    private long mMillisLastTickStart;

    /**
     * boolean representing if the timer was cancelled
     */
    private boolean mCancelled = true;
    private boolean mRunning = false;

    public CountTimer(long countInterval) {
        mCountInterval = countInterval;
    }

    /**
     * Start the count.
     */
    public synchronized CountTimer start() {
        mCancelled = false;
        mRunning = true;
        mTotalPausedFly = 0;
        onStart(0);
        
        mMillisStart = SystemClock.elapsedRealtime();
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    /**
     * Pause the count.
     */
    public synchronized void pause() {
        if (mCancelled || !mRunning) {
            return;
        }
        mRunning = false;

        mHandler.removeMessages(MSG);
        mMillisPause = SystemClock.elapsedRealtime();
        onPause(mMillisPause - mMillisStart - mTotalPausedFly);
    }

    /**
     * Resume the count.
     */
    public synchronized void resume() {
        if (mCancelled || mRunning) {
            return;
        }
        mRunning = true;

        onResume(mMillisPause - mMillisStart - mTotalPausedFly);

        long delay = mCountInterval - (mMillisPause - mMillisLastTickStart);
        mTotalPausedFly += SystemClock.elapsedRealtime() - mMillisPause;
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG), delay);
    }

    /**
     * Cancel the count.
     */
    public synchronized void cancel() {

        if (mMillisStart == NOT_START) {
            return;
        }
        mCancelled = true;
        mHandler.removeMessages(MSG);

        if (mRunning) { //running -> cancel
            onCancel(SystemClock.elapsedRealtime() - mMillisStart - mTotalPausedFly);
        } else { //pause -> cancel
            onCancel(mMillisPause - mMillisStart - mTotalPausedFly);
        }
        mRunning = false;
        mMillisStart = NOT_START;
    }

    public void onStart(long millisFly) {
    }

    public void onCancel(long millisFly) {
    }

    public void onPause(long millisFly) {
    }

    public void onResume(long millisFly) {
    }

    public void onTick(long millisFly) {
    }

    public final boolean isRunning() {
        return mRunning;
    }

    // handles counting
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (CountTimer.this) {
                if (mCancelled || !mRunning) {
                    return;
                }

                mMillisLastTickStart = SystemClock.elapsedRealtime();
                onTick(mMillisLastTickStart - mMillisStart - mTotalPausedFly);

                // take into account user's onTick taking time to execute
                long delay = mMillisLastTickStart + mCountInterval - SystemClock.elapsedRealtime();

                // special case: user's onTick took more than interval to
                // complete, skip to next interval
                while (delay < 0) {
                    delay += mCountInterval;
                }

                sendMessageDelayed(obtainMessage(MSG), delay);
            }
        }
    };

    private static final int MSG = 1;

}
