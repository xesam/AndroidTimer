package dev.xesam.android.toolbox.timer;

import android.os.Handler;
import android.os.SystemClock;

/**
 * Created by xesamguo@gmail.com on 16-3-25.
 */
public class CounterTimerTask implements OnTickListener {

    static final long INVALID_INTERVAL = -1;
    private static final int NOT_START = -1;

    final int mId;
    long mCountInterval = INVALID_INTERVAL;

    private long mTotalPausedFly;
    private long mMillisStart = NOT_START;
    private long mMillisPause;
    private long mMillisLastTickStart;

    private Handler mHandler;
    private int mState = State.TIMER_NOT_START;

    public CounterTimerTask(int id) {
        this(id, INVALID_INTERVAL);
    }

    public CounterTimerTask(int id, long interval) {
        mId = id;
        mCountInterval = interval;
    }

    void attachHandler(Handler handler) {
        mHandler = handler;
    }

    void tickAndNext() {
        mMillisLastTickStart = SystemClock.elapsedRealtime();
        onTick(mMillisLastTickStart - mMillisStart - mTotalPausedFly);

        long delay = mMillisLastTickStart + mCountInterval - SystemClock.elapsedRealtime();

        while (delay < 0) {
            delay += mCountInterval;
        }

        mHandler.sendMessageDelayed(mHandler.obtainMessage(mId), delay);
    }

    /**
     * Start the count.
     */
    public void start() {
        mTotalPausedFly = 0;
        mMillisStart = SystemClock.elapsedRealtime();
        mState = State.TIMER_RUNNING;
        onStart(0);
        mHandler.sendEmptyMessage(mId);
    }

    /**
     * Pause the count.
     */
    public void pause() {
        if (mState != State.TIMER_RUNNING) {
            return;
        }
        mHandler.removeMessages(mId);
        mState = State.TIMER_PAUSED;

        mMillisPause = SystemClock.elapsedRealtime();
        onPause(mMillisPause - mMillisStart - mTotalPausedFly);
    }

    /**
     * Resume the count.
     */
    public void resume() {
        if (mState != State.TIMER_PAUSED) {
            return;
        }
        mState = State.TIMER_RUNNING;

        onResume(mMillisPause - mMillisStart - mTotalPausedFly);

        mTotalPausedFly += SystemClock.elapsedRealtime() - mMillisPause;
        long delay = mCountInterval - (mMillisPause - mMillisLastTickStart);
        mHandler.sendEmptyMessageDelayed(mId, delay);
    }

    /**
     * Cancel the count.
     */
    public void cancel() {

        if (mState == State.TIMER_NOT_START) {
            return;
        }
        final int preState = mState;
        mHandler.removeMessages(mId);
        mState = State.TIMER_NOT_START;

        if (preState == State.TIMER_RUNNING) { //running -> cancel
            onCancel(SystemClock.elapsedRealtime() - mMillisStart - mTotalPausedFly);
        } else if (preState == State.TIMER_PAUSED) { //pause -> cancel
            onCancel(mMillisPause - mMillisStart - mTotalPausedFly);
        }
    }

    public int getState() {
        return mState;
    }

    @Override
    public void onStart(long millisFly) {

    }

    @Override
    public void onCancel(long millisFly) {

    }

    @Override
    public void onPause(long millisFly) {

    }

    @Override
    public void onResume(long millisFly) {

    }

    @Override
    public void onTick(long millisFly) {

    }
}
