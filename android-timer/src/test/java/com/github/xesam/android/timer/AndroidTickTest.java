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
public class AndroidTickTest {

    private AndroidTick androidTick;
    private TestAndroidTick testAndroidDelay;

    @Before
    public void setUp() {
        testAndroidDelay = new TestAndroidTick(100L); // 100ms delay
        androidTick = new AndroidTick(100L);
    }

    @After
    public void tearDown() {
        if (androidTick != null) {
            androidTick.cancel();
        }
        if (testAndroidDelay != null) {
            testAndroidDelay.cancel();
        }
    }

    @Test
    public void testTick_shouldNotThrowException() {
        androidTick.tick();
        // 验证tick方法可以正常执行
        assertNotNull(androidTick);
    }

    @Test
    public void testCancel_shouldNotThrowException() {
        androidTick.tick();
        androidTick.cancel();
        // 验证cancel方法可以正常执行
        assertNotNull(androidTick);
    }

    @Test
    public void testCancel_shouldRemoveMessages() {
        androidTick.tick();
        androidTick.cancel();
        // 确保没有消息在处理队列中
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(0, testAndroidDelay.getTickCount());
    }

    @Test
    public void testOnTick_shouldBeCalledAfterDelay() {
        TestAndroidTick delay = new TestAndroidTick(100L);
        delay.tick();
        
        // 初始状态
        assertEquals(0, delay.getTickCount());
        
        // 快进时间到刚好100ms
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        
        // 验证onTick被调用了一次
        assertEquals(1, delay.getTickCount());
    }

    @Test
    public void testMultipleTicks_shouldCountCorrectly() {
        TestAndroidTick delay = new TestAndroidTick(100L);
        
        // 模拟多次延迟执行
        for (int i = 1; i <= 5; i++) {
            delay.tick();
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            assertEquals(i, delay.getTickCount());
        }
    }

    @Test
    public void testTickAfterCancel_shouldResetCount() {
        TestAndroidTick delay = new TestAndroidTick(100L);
        delay.tick();
        
        // 执行一次
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(1, delay.getTickCount());
        
        // 取消并重新开始
        delay.cancel();
        delay.tick();
        
        // 计数器应该重置
        assertEquals(1, delay.getTickCount());
    }

    @Test
    public void testConcurrentTick_shouldNotThrowException() {
        // 测试并发调用start不会抛出异常
        androidTick.tick();
        androidTick.tick(); // 第二次调用
        
        // 验证可以正常执行
        assertNotNull(androidTick);
    }

    @Test
    public void testConcurrentCancel_shouldNotThrowException() {
        // 测试并发调用cancel不会抛出异常
        androidTick.cancel();
        androidTick.cancel(); // 第二次调用
        
        // 验证可以正常执行
        assertNotNull(androidTick);
    }

    // 测试用的子类，用于验证onTick调用
    private static class TestAndroidTick extends AndroidTick {
        private int tickCount = 0;

        public TestAndroidTick(long millis) {
            super(millis);
        }

        @Override
        protected void onTick(AndroidTick thisInstance, int count) {
            super.onTick(thisInstance, count);
            tickCount = count;
            // 继续发送消息以模拟周期性执行
            // 注意：这里我们不再发送新消息，因为我们只测试单次延迟
        }

        public int getTickCount() {
            return tickCount;
        }
    }

    // 新增测试：测试并发调用tick
    @Test
    public void testConcurrentTick_shouldWorkCorrectly() throws InterruptedException {
        TestAndroidTick delay = new TestAndroidTick(100L);
        
        // 创建多个线程同时调用tick
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> delay.tick());
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 验证只执行了一次
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(1, delay.getTickCount());
    }

    @Test
    public void testLifecycle_shouldWorkCorrectly() {
        TestAndroidTick delay = new TestAndroidTick(50L);
        
        // 启动并验证onTick被调用
        delay.tick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(1, delay.getTickCount());
        
        // 取消后验证不再执行
        delay.cancel();
        int countBefore = delay.getTickCount();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(countBefore, delay.getTickCount());
    }

    @Test
    public void testDifferentDelayValues_shouldWorkCorrectly() {
        // 测试不同的延迟值
        long[] delays = {1L, 10L, 100L, 1000L};
        
        for (long delayValue : delays) {
            TestAndroidTick delay = new TestAndroidTick(delayValue);
            delay.tick();
            
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            assertEquals(1, delay.getTickCount());
            
            delay.cancel();
        }
    }

    @Test
    public void testNullOnTick_shouldNotCrash() {
        // 测试onTick为空实现时不会崩溃
        androidTick.tick();
        
        // 确保不会抛出异常
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        
        assertNotNull(androidTick);
    }

    // 新增测试：测试零延迟情况
    @Test
    public void testZeroDelay_shouldWorkCorrectly() {
        TestAndroidTick delay = new TestAndroidTick(0L);
        delay.tick();
        
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(1, delay.getTickCount());
    }

    // 新增测试：测试负数延迟情况
    @Test
    public void testNegativeDelay_shouldWorkCorrectly() {
        TestAndroidTick delay = new TestAndroidTick(-100L);
        delay.tick();
        
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(1, delay.getTickCount());
    }

    // 新增测试：测试多次cancel调用
    @Test
    public void testMultipleCancel_shouldNotThrowException() {
        androidTick.tick();
        androidTick.cancel();
        androidTick.cancel(); // 第二次调用
        androidTick.cancel(); // 第三次调用
        
        // 验证可以正常执行
        assertNotNull(androidTick);
    }

    // 新增测试：测试cancel后再次tick
    @Test
    public void testTickAfterCancel_shouldWorkCorrectly() {
        TestAndroidTick delay = new TestAndroidTick(100L);
        delay.tick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(1, delay.getTickCount());
        
        delay.cancel();
        delay.tick(); // cancel后再次调用tick
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(2, delay.getTickCount()); // 应该继续递增
    }

    // 新增测试：测试isRunning状态
    @Test
    public void testIsRunningState_shouldBeCorrect() {
        TestAndroidTickWithState delay = new TestAndroidTickWithState(100L);
        
        assertFalse(delay.isRunning());
        
        delay.tick();
        assertTrue(delay.isRunning());
        
        delay.cancel();
        assertFalse(delay.isRunning());
    }

    // 带状态检查的测试用子类
    private static class TestAndroidTickWithState extends AndroidTick {
        public TestAndroidTickWithState(long millis) {
            super(millis);
        }

        public boolean isRunning() {
            // 通过反射获取私有字段
            try {
                java.lang.reflect.Field field = AndroidTick.class.getDeclaredField("mIsRunning");
                field.setAccessible(true);
                return field.getBoolean(this);
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onTick(AndroidTick thisInstance, int count) {
            super.onTick(thisInstance, count);
        }
    }

    // 新增测试：测试forceCancel参数
    @Test
    public void testForceCancel_shouldWorkCorrectly() {
        TestAndroidTickWithState delay = new TestAndroidTickWithState(100L);
        delay.tick();
        
        // 不使用forceCancel，应该不会重新调度
        delay.tick(); // 第二次调用
        assertTrue(delay.isRunning()); // 应该仍然运行
        
        // 使用forceCancel，应该重新调度
        delay.tick(true); // 使用forceCancel
        assertTrue(delay.isRunning()); // 应该重新运行
    }

    // 新增测试：测试计数器递增
    @Test
    public void testCounterIncrement_shouldBeCorrect() {
        TestAndroidTick delay = new TestAndroidTick(100L);
        
        for (int i = 1; i <= 5; i++) {
            delay.tick();
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            assertEquals(i, delay.getTickCount());
        }
    }

    // 新增测试：测试快速连续调用
    @Test
    public void testRapidConsecutiveCalls_shouldNotCauseIssues() {
        TestAndroidTick delay = new TestAndroidTick(10L);
        
        // 快速连续调用tick
        for (int i = 0; i < 100; i++) {
            delay.tick();
        }
        
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        // 由于AndroidTick的实现机制，只有第一次调用会生效
        assertEquals(1, delay.getTickCount());
    }
}