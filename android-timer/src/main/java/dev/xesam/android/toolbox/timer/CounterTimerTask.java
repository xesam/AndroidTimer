package dev.xesam.android.toolbox.timer;

import android.os.Handler;
import android.os.SystemClock;

/**
 * Created by xesamguo@gmail.com on 16-3-25.
 */
public class CounterTimerTask implements OnTickListener {

    static final long INVALID_INTERVAL = -1;
    private static final int NOT_START = -1;

    final int mType;
    long mCountInterval = INVALID_INTERVAL;

    long mTotalPausedFly;
    long mMillisStart = NOT_START;
    long mMillisPause;
    long mMillisLastTickStart;

    private boolean mCancelled = true;
    private boolean mRunning = false;

    private Handler mHandler;

    public CounterTimerTask(int type) {
        this(type, INVALID_INTERVAL);
    }

    public CounterTimerTask(int type, long interval) {
        mType = type;
        mCountInterval = interval;
    }

    void attachHandler(Handler handler) {
        mHandler = handler;
    }

    int getType() {
        return mType;
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    public boolean isRunning() {
        return mRunning;
    }

    void tickAndNext() {
        mMillisLastTickStart = SystemClock.elapsedRealtime();
        onTick(mMillisLastTickStart - mMillisStart - mTotalPausedFly);

        long delay = mMillisLastTickStart + mCountInterval - SystemClock.elapsedRealtime();

        while (delay < 0) {
            delay += mCountInterval;
        }

        mHandler.sendMessageDelayed(mHandler.obtainMessage(mType), delay);
    }

    /**
     * Start the count.
     */
    public void start() {
        mCancelled = false;
        mRunning = true;
        mTotalPausedFly = 0;
        onStart(0);

        mMillisStart = SystemClock.elapsedRealtime();
        mHandler.sendMessage(mHandler.obtainMessage(mType));
    }

    /**
     * Pause the count.
     */
    public void pause() {
        if (mCancelled || !mRunning) {
            return;
        }
        mRunning = false;

        mHandler.removeMessages(mType);
        mMillisPause = SystemClock.elapsedRealtime();
        onPause(mMillisPause - mMillisStart - mTotalPausedFly);
    }

    /**
     * Resume the count.
     */
    public void resume() {
        if (mCancelled || mRunning) {
            return;
        }
        mRunning = true;

        onResume(mMillisPause - mMillisStart - mTotalPausedFly);

        mTotalPausedFly += SystemClock.elapsedRealtime() - mMillisPause;

        long delay = mCountInterval - (mMillisPause - mMillisLastTickStart);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(mType), delay);
    }

    /**
     * Cancel the count.
     */
    public void cancel() {

        if (mMillisStart == NOT_START) {
            return;
        }
        mCancelled = true;
        mHandler.removeMessages(mType);

        if (mRunning) { //running -> cancel
            onCancel(SystemClock.elapsedRealtime() - mMillisStart - mTotalPausedFly);
        } else { //pause -> cancel
            onCancel(mMillisPause - mMillisStart - mTotalPausedFly);
        }
        mRunning = false;
        mMillisStart = NOT_START;
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
