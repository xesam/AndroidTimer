package com.github.xesam.android.timer;

import android.os.Handler;

public class AndroidTick {
    private volatile boolean mIsRunning = false;
    private int mCount = 0;
    private final long mMillisDelay;

    public AndroidTick(long millisDelay) {
        if (millisDelay <= 0) {
            throw new IllegalArgumentException("延迟时间必须大于0");
        }
        this.mMillisDelay = millisDelay;
    }

    private static final int MSG = 1;

    private final Handler mHandler = new Handler(msg -> {
        int snapshot;
        boolean fire;
        synchronized (AndroidTick.this) {
            if (!mIsRunning) {
                return true;
            }
            mIsRunning = false;
            mCount++;
            snapshot = mCount;
            fire = true;
        }
        // 回调在锁外派发，避免用户回调重入时引发状态惊奇
        if (fire) {
            onTick(AndroidTick.this, snapshot);
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

    public final boolean isRunning() {
        return mIsRunning;
    }

    protected void onTick(AndroidTick thisInstance, int count) {
    }
}
