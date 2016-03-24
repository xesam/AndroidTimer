package dev.xesam.android.toolbox.timer;

import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

/**
 * 监控多个任务的定时器
 * <p>
 * Created by xesamguo@gmail.com on 16-3-23.
 */
public class MultiCountTimer {
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };

    private SparseArray<OnTickListener> ticks;

    public void registerTick(int msg, OnTickListener onTickListener) {
        ticks.append(msg, onTickListener);
    }

    public void start(int msg) {

    }

    public void pause(int msg) {

    }

    public void resume(int msg) {

    }

    public void cancel(int msg) {

    }

}
