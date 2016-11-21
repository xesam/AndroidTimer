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

    private final long mCountInterval;
    private long mMillisStart = -1;
    private long mMillisPause;
    private long mMillisLastTickStart;
    private long mTotalPausedFly;

    /**
     * representing the timer state
     */
    private int mState = State.TIMER_NOT_START;

    public CountTimer(long countInterval) {
        mCountInterval = countInterval;
    }

    /**
     * Start the timer.
     */
    public synchronized void start() {
        mTotalPausedFly = 0;
        mMillisStart = SystemClock.elapsedRealtime();
        mHandler.sendEmptyMessage(MSG);
        mState = State.TIMER_RUNNING;
        onStart(0);
    }

    /**
     * Pause the timer.
     * if the timer has been canceled or is running --> skip
     */
    public synchronized void pause() {
        if (mState != State.TIMER_RUNNING) {
            return;
        }
        mState = State.TIMER_PAUSED;

        mHandler.removeMessages(MSG);
        mMillisPause = SystemClock.elapsedRealtime();
        onPause(mMillisPause - mMillisStart - mTotalPausedFly);
    }

    /**
     * Resume the timer.
     */
    public synchronized void resume() {
        if (mState != State.TIMER_PAUSED) {
            return;
        }
        mState = State.TIMER_RUNNING;

        onResume(mMillisPause - mMillisStart - mTotalPausedFly);

        long delay = mCountInterval - (mMillisPause - mMillisLastTickStart);
        mTotalPausedFly += SystemClock.elapsedRealtime() - mMillisPause;
        mHandler.sendEmptyMessageDelayed(MSG, delay);
    }

    /**
     * Cancel the timer.
     */
    public synchronized void cancel() {
        if (mState == State.TIMER_NOT_START) {
            return;
        }
        mHandler.removeMessages(MSG);

        if (mState == State.TIMER_RUNNING) { //running -> cancel
            onCancel(SystemClock.elapsedRealtime() - mMillisStart - mTotalPausedFly);
        } else if (mState == State.TIMER_PAUSED) { //pause -> cancel
            onCancel(mMillisPause - mMillisStart - mTotalPausedFly);
        }
        mState = State.TIMER_NOT_START;
    }

    public int getState() {
        return mState;
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    public void onStart(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    public void onCancel(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    public void onPause(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    public void onResume(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    public void onTick(long millisFly) {
    }

    // handles counting
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (CountTimer.this) {
                if (mState != State.TIMER_RUNNING) {
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
