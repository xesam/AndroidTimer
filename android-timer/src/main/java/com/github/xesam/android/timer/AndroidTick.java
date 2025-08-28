package com.github.xesam.android.timer;

import android.os.Handler;

public class AndroidTick {
    private volatile boolean mIsRunning = false;
    private int mCount = 0;
    private final long mMillisDelay;

    public AndroidTick(long millisDelay) {
        this.mMillisDelay = millisDelay;
    }

    private static final int MSG = 1;

    private final Handler mHandler = new Handler(msg -> {
        synchronized (AndroidTick.this) {
            if (!mIsRunning) {
                return true;
            }
            mIsRunning = false;
            mCount++;
            onTick(AndroidTick.this, mCount);
        }
        return true;
    });

    public final synchronized void tick() {
        this.tick(false);
    }

    public final synchronized void tick(boolean forceCancel) {
        if (mIsRunning) {
            if (forceCancel) {
                this.cancel();
            } else {
                return;
            }
        }
        mIsRunning = true;
        mHandler.sendEmptyMessageDelayed(MSG, mMillisDelay);
    }

    public final synchronized void cancel() {
        mIsRunning = false;
        mHandler.removeMessages(MSG);
    }

    protected void onTick(AndroidTick thisInstance, int count) {
    }
}
