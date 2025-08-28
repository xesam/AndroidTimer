package com.github.xesam.android.timer;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import androidx.annotation.IntDef;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class TimerStatus {
    @Retention(RetentionPolicy.SOURCE)
    @Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
    @IntDef({IDLE, RUNNING, PAUSED})
    public @interface Enum {
    }

    public static final int IDLE = 0;
    public static final int RUNNING = 1;
    public static final int PAUSED = 2;
}
