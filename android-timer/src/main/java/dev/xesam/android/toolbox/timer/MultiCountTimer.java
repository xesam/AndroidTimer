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

    private SparseArray<CounterTimerTask> ticks = new SparseArray<>();
    private long mDefaultInterval = DEFAULT_INTERVAL;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            final int msgType = msg.what;

            synchronized (MultiCountTimer.this) {

                CounterTimerTask task = ticks.get(msgType);
                if (task == null) {
                    return;
                }

                if (task.isCancelled() || !task.isRunning()) {
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

    public synchronized MultiCountTimer registerTask(CounterTimerTask task) {
        task.attachHandler(mHandler);
        if (task.mCountInterval == CounterTimerTask.INVALID_INTERVAL) {
            task.mCountInterval = mDefaultInterval;
        }
        ticks.append(task.getType(), task);
        return this;
    }

    public synchronized void startAll() {
        for (int i = 0, size = ticks.size(); i < size; i++) {
            int key = ticks.keyAt(i);
            ticks.get(key).start();
        }
    }

    public synchronized void cancelAll() {
        for (int i = 0, size = ticks.size(); i < size; i++) {
            int key = ticks.keyAt(i);
            ticks.get(key).cancel();
        }
        ticks.clear();
    }

    public synchronized void start(int type) {
        CounterTimerTask task = ticks.get(type);
        if (task == null) {
            return;
        }
        task.start();
    }

    public synchronized void pause(int type) {
        CounterTimerTask task = ticks.get(type);
        if (task == null) {
            return;
        }
        task.pause();
    }

    public synchronized void resume(int type) {
        CounterTimerTask task = ticks.get(type);
        if (task == null) {
            return;
        }
        task.resume();
    }

    public synchronized void cancel(int type) {
        CounterTimerTask task = ticks.get(type);
        if (task == null) {
            return;
        }
        task.cancel();
        ticks.remove(type);
    }

}
