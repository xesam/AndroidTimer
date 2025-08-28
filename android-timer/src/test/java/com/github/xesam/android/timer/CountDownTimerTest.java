package com.github.xesam.android.timer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLooper;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class CountDownTimerTest {

    private CountDownTimer countDownTimer;
    private TestCountDownTimer testCountDownTimer;

    @Before
    public void setUp() {
        testCountDownTimer = new TestCountDownTimer(100L, 1L); // 100ms倒计时，1ms间隔
        countDownTimer = new CountDownTimer(100L, 1L);
    }

    @After
    public void tearDown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (testCountDownTimer != null) {
            testCountDownTimer.cancel();
        }
    }

    @Test
    public void testInitialState_shouldBeIdle() {
        assertEquals(TimerStatus.IDLE, countDownTimer.getState());
    }

    @Test
    public void testStart_shouldChangeStateToRunning() {
        countDownTimer.start();
        assertEquals(TimerStatus.RUNNING, countDownTimer.getState());
    }

    @Test
    public void testStart_shouldTriggerOnStart() {
        TestCountDownTimer timer = new TestCountDownTimer(1000L, 100L);
        timer.start();
        
        assertEquals(1, timer.getOnStartCount());
        assertEquals(1000L, timer.getLastMillisUntilFinished());
    }
    @Test
    public void testDoubleStart_shouldNotTriggerMultipleOnStart() {
        TestCountDownTimer timer = new TestCountDownTimer(1000L, 100L);
        timer.start();
        timer.start(); // 第二次调用
        
        assertEquals(1, timer.getOnStartCount());
    }

    @Test
    public void testPause_shouldChangeStateToPaused() {
        countDownTimer.start();
        countDownTimer.pause();
        
        assertEquals(TimerStatus.PAUSED, countDownTimer.getState());
    }

    @Test
    public void testPause_shouldTriggerOnPause() {
        TestCountDownTimer timer = new TestCountDownTimer(1000L, 100L);
        timer.start();
        
        // 运行一个间隔
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        
        timer.pause();
        
        assertEquals(1, timer.getOnPauseCount());
        assertTrue(timer.getLastMillisUntilFinished() < 1000L);
    }

    @Test
    public void testPauseWhenNotRunning_shouldNotTriggerOnPause() {
        TestCountDownTimer timer = new TestCountDownTimer(1000L, 100L);
        timer.pause(); // 未启动时暂停
        
        assertEquals(0, timer.getOnPauseCount());
    }

    @Test
    public void testResume_shouldChangeStateToRunning() {
        countDownTimer.start();
        countDownTimer.pause();
        countDownTimer.resume();
        
        assertEquals(TimerStatus.RUNNING, countDownTimer.getState());
    }

    @Test
    public void testResume_shouldTriggerOnResume() {
        TestCountDownTimer timer = new TestCountDownTimer(1000L, 100L);
        timer.start();
        timer.pause();
        timer.resume();
        
        assertEquals(1, timer.getOnResumeCount());
    }

    @Test
    public void testResumeWhenNotPaused_shouldNotTriggerOnResume() {
        TestCountDownTimer timer = new TestCountDownTimer(1000L, 100L);
        timer.resume(); // 未暂停时恢复
        
        assertEquals(0, timer.getOnResumeCount());
    }

    @Test
    public void testCancel_shouldChangeStateToIdle() {
        countDownTimer.start();
        countDownTimer.cancel();
        
        assertEquals(TimerStatus.IDLE, countDownTimer.getState());
    }

    @Test
    public void testCancelFromRunning_shouldTriggerOnCancel() {
        TestCountDownTimer timer = new TestCountDownTimer(1000L, 100L);
        timer.start();
        
        // 运行一个间隔
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        
        timer.cancel();
        
        assertEquals(TimerStatus.IDLE, timer.getState());
        assertEquals(1, timer.getOnCancelCount());
    }

    @Test
    public void testCancelFromPaused_shouldTriggerOnCancel() {
        TestCountDownTimer timer = new TestCountDownTimer(1000L, 100L);
        timer.start();
        timer.pause();
        timer.cancel();
        
        assertEquals(TimerStatus.IDLE, timer.getState());
        assertEquals(1, timer.getOnCancelCount());
    }

    @Test
    public void testCancelWhenIdle_shouldNotTriggerOnCancel() {
        TestCountDownTimer timer = new TestCountDownTimer(1000L, 100L);
        timer.cancel(); // 空闲时取消
        
        assertEquals(0, timer.getOnCancelCount());
    }

    @Test
    public void testOnTick_shouldBeCalledAtRegularIntervals() {
        TestCountDownTimer timer = new TestCountDownTimer(10L, 1L);
        timer.start();
        
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertTrue(timer.getOnTickCount() >= 1);
    }

    @Test
    public void testOnFinish_shouldBeCalledWhenCountdownCompletes() {
        TestCountDownTimer timer = new TestCountDownTimer(10L, 1L);
        timer.start();
        
        // 确保倒计时完成
        for (int i = 0; i < 15; i++) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            if (timer.getOnFinishCount() > 0) {
                break;
            }
        }
        
        assertTrue(timer.getOnFinishCount() >= 1);
        assertEquals(TimerStatus.IDLE, timer.getState());
    }

    @Test
    public void testPauseAndResume_shouldMaintainCorrectCountdown() {
        TestCountDownTimer timer = new TestCountDownTimer(10L, 1L);
        timer.start();
        
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        int tickCount1 = timer.getOnTickCount();
        
        timer.pause();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(tickCount1, timer.getOnTickCount());
        
        timer.resume();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertTrue(timer.getOnTickCount() > tickCount1);
    }



    @Test
    public void testLifecycleSequence_shouldWorkCorrectly() {
        TestCountDownTimer timer = new TestCountDownTimer(10L, 1L);
        
        timer.start();
        assertEquals(TimerStatus.RUNNING, timer.getState());
        assertEquals(1, timer.getOnStartCount());
        
        timer.pause();
        assertEquals(TimerStatus.PAUSED, timer.getState());
        assertEquals(1, timer.getOnPauseCount());
        
        timer.resume();
        assertEquals(TimerStatus.RUNNING, timer.getState());
        assertEquals(1, timer.getOnResumeCount());
        
        timer.cancel();
        assertEquals(TimerStatus.IDLE, timer.getState());
        assertEquals(1, timer.getOnCancelCount());
    }

    // 测试用的子类，用于验证各个回调方法的调用
    private static class TestCountDownTimer extends CountDownTimer {
        private int onStartCount = 0;
        private int onTickCount = 0;
        private int onPauseCount = 0;
        private int onResumeCount = 0;
        private int onCancelCount = 0;
        private int onFinishCount = 0;
        private long lastMillisUntilFinished = 0;
        private long lastMillisDuration = 0;

        public TestCountDownTimer(long millisDuration, long countDownInterval) {
            super(millisDuration, countDownInterval);
        }

        @Override
        protected void onStart(long millisUntilFinished) {
            super.onStart(millisUntilFinished);
            onStartCount++;
            lastMillisUntilFinished = millisUntilFinished;
        }

        @Override
        protected void onTick(long millisUntilFinished) {
            super.onTick(millisUntilFinished);
            onTickCount++;
            lastMillisUntilFinished = millisUntilFinished;
        }

        @Override
        protected void onPause(long millisUntilFinished) {
            super.onPause(millisUntilFinished);
            onPauseCount++;
            lastMillisUntilFinished = millisUntilFinished;
        }

        @Override
        protected void onResume(long millisUntilFinished) {
            super.onResume(millisUntilFinished);
            onResumeCount++;
            lastMillisUntilFinished = millisUntilFinished;
        }

        @Override
        protected void onCancel(long millisUntilFinished) {
            super.onCancel(millisUntilFinished);
            onCancelCount++;
            lastMillisUntilFinished = millisUntilFinished;
        }

        @Override
        protected void onFinish(long millisDuration) {
            super.onFinish(millisDuration);
            onFinishCount++;
            lastMillisDuration = millisDuration;
        }

        public int getOnStartCount() { return onStartCount; }
        public int getOnTickCount() { return onTickCount; }
        public int getOnPauseCount() { return onPauseCount; }
        public int getOnResumeCount() { return onResumeCount; }
        public int getOnCancelCount() { return onCancelCount; }
        public int getOnFinishCount() { return onFinishCount; }
        public long getLastMillisUntilFinished() { return lastMillisUntilFinished; }
    }
}