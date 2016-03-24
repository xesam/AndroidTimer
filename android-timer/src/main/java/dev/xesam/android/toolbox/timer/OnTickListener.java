package dev.xesam.android.toolbox.timer;

/**
 * Created by xesamguo@gmail.com on 16-3-25.
 */
public interface OnTickListener {

    void onStart(long millisFly);

    void onCancel(long millisFly);

    void onPause(long millisFly);

    void onResume(long millisFly);

    void onTick(long millisFly);

}
