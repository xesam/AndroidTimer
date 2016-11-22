package dev.xesam.android.toolbox.timer;

import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

/**
 * 监控多个任务的定时器
 * <p/>
 * Created by xesamguo@gmail.com on 16-3-23.
 */
public class MultiCountTimer {

    private static final long DEFAULT_INTERVAL = 1000;

    private SparseArray<CountTimerTask> mTicks = new SparseArray<>();
    private long mDefaultInterval = DEFAULT_INTERVAL;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            final int id = msg.what;

            synchronized (MultiCountTimer.this) {

                CountTimerTask task = mTicks.get(id);
                if (task == null || task.getState() != State.TIMER_RUNNING) {
                    return;
                }

                task.tickAndNext();
            }
        }
    };

    public MultiCountTimer() {
    }

    public MultiCountTimer(long defaultInterval) {
        mDefaultInterval = defaultInterval;
    }

    public synchronized MultiCountTimer add(CountTimerTask task) {
        task.attachHandler(mHandler);
        if (task.mCountInterval == CountTimerTask.INVALID_INTERVAL) {
            task.mCountInterval = mDefaultInterval;
        }
        mTicks.append(task.mId, task);
        return this;
    }

    public synchronized void startAll() {
        for (int i = 0, size = mTicks.size(); i < size; i++) {
            int key = mTicks.keyAt(i);
            mTicks.get(key).start();
        }
    }

    public synchronized void cancelAll() {
        for (int i = 0, size = mTicks.size(); i < size; i++) {
            int key = mTicks.keyAt(i);
            mTicks.get(key).cancel();
        }
        mTicks.clear();
    }

    public synchronized void start(int id) {
        CountTimerTask task = mTicks.get(id);
        if (task == null) {
            return;
        }
        task.start();
    }

    public synchronized void pause(int id) {
        CountTimerTask task = mTicks.get(id);
        if (task == null) {
            return;
        }
        task.pause();
    }

    public synchronized void resume(int id) {
        CountTimerTask task = mTicks.get(id);
        if (task == null) {
            return;
        }
        task.resume();
    }

    public synchronized void cancel(int id) {
        CountTimerTask task = mTicks.get(id);
        if (task == null) {
            return;
        }
        task.cancel();
        mTicks.remove(id);
    }

}
