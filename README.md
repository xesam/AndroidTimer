# AndroidTimer

一个轻量级的Android计时器库，提供AndroidTimer和CountDownTimer两个核心组件，支持灵活的状态管理。

## 🚀 快速开始

### 1. 添加依赖

以 `gradle` 为例，在根目录的 `build.gradle` 文件中添加：

```gradle
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```

再模块目录的 `build.gradle` 文件中添加：

```gradle
	dependencies {
        implementation 'com.github.xesam:AndroidTimer:0.3.0'
	}
```

更多配置方式可以参考 `jitpack`
文档：[https://jitpack.io/#xesam/AndroidTimer](https://jitpack.io/#xesam/AndroidTimer)

### 2. 基本使用

#### AndroidTimer（周期性计时器）

```java
// 创建计时器：每1000ms触发一次onTick
AndroidTimer timer = new AndroidTimer(1000L) {
            @Override
            protected void onTick(long tickCount) {
                // 周期性回调
                Log.d("Timer", "Tick count: " + tickCount);
            }

            @Override
            protected void onStart(long tickCount) {
                // 计时器启动
                Log.d("Timer", "Started with tick: " + tickCount);
            }

            @Override
            protected void onPause(long tickCount) {
                // 计时器暂停
                Log.d("Timer", "Paused at tick: " + tickCount);
            }

            @Override
            protected void onResume(long tickCount) {
                // 计时器恢复
                Log.d("Timer", "Resumed from tick: " + tickCount);
            }

            @Override
            protected void onCancel(long tickCount) {
                // 计时器取消
                Log.d("Timer", "Cancelled at tick: " + tickCount);
            }
        };

// 启动
timer.start();

// 暂停
timer.pause();

// 恢复
timer.resume();

// 取消
timer.cancel();
```

#### CountDownTimer（倒计时器）

```java
// 创建倒计时器：10秒倒计时，每100ms更新一次
CountDownTimer countDownTimer = new CountDownTimer(10000L, 100L) {
            @Override
            protected void onStart(long millisUntilFinished) {
                // 倒计时开始
                Log.d("CountDown", "Started: " + millisUntilFinished + "ms remaining");
            }

            @Override
            protected void onTick(long millisUntilFinished) {
                // 倒计时进行中
                Log.d("CountDown", "Remaining: " + millisUntilFinished + "ms");
            }

            @Override
            protected void onPause(long millisUntilFinished) {
                // 倒计时暂停
                Log.d("CountDown", "Paused with " + millisUntilFinished + "ms remaining");
            }

            @Override
            protected void onResume(long millisUntilFinished) {
                // 倒计时恢复
                Log.d("CountDown", "Resumed with " + millisUntilFinished + "ms remaining");
            }

            @Override
            protected void onCancel(long millisUntilFinished) {
                // 倒计时取消
                Log.d("CountDown", "Cancelled with " + millisUntilFinished + "ms remaining");
            }

            @Override
            protected void onFinish(long millisDuration) {
                // 倒计时完成
                Log.d("CountDown", "Finished!");
            }
        };
// 启动
countDownTimer.start();

// 暂停
countDownTimer.pause();

// 恢复
countDownTimer.resume();

// 取消
countDownTimer.cancel();
```

## 📋 API 文档

### AndroidTimer

#### 构造函数

```java
AndroidTimer(long interval)

AndroidTimer(long interval, AndroidTimer.Option option)
```

#### 方法

- `start()`: 启动计时器
- `pause()`: 暂停计时器
- `resume()`: 恢复计时器
- `cancel()`: 取消计时器
- `getState()`: 获取当前状态

#### 回调方法

- `onStart(long millisFly)`: 计时器启动时调用
- `onTick(long millisFly)`: 每个间隔触发
- `onPause(long millisFly)`: 计时器暂停时调用
- `onResume(long millisFly)`: 计时器恢复时调用
- `onCancel(long millisFly)`: 计时器取消时调用

### CountDownTimer

#### 构造函数

```java
CountDownTimer(long millisDuration, long countDownInterval)

CountDownTimer(long millisDuration, long countDownInterval, CountDownTimer.Option option)
```

#### 方法

- `start()`: 启动倒计时
- `pause()`: 暂停倒计时
- `resume()`: 恢复倒计时
- `cancel()`: 取消倒计时
- `getState()`: 获取当前状态

#### 回调方法

- `onStart(long millisUntilFinished)`: 倒计时开始时调用
- `onTick(long millisUntilFinished)`: 每个间隔触发
- `onPause(long millisUntilFinished)`: 倒计时暂停时调用
- `onResume(long millisUntilFinished)`: 倒计时恢复时调用
- `onCancel(long millisUntilFinished)`: 倒计时取消时调用
- `onFinish(long millisUntilFinished)`: 倒计时完成时调用

### 配置选项

#### AndroidTimer.Option

```java
AndroidTimer.Option option = new AndroidTimer.Option(
boolean tickWhenStart  // 启动时是否立即触发一次onTick
);
```

#### CountDownTimer.Option

```java
CountDownTimer.Option option = new CountDownTimer.Option(
boolean tickWhenStart,   // 启动时是否立即触发一次onTick
boolean tickWhenFinish   // 完成时是否触发一次onTick
);
```

## 🔄 状态管理

两个计时器都支持以下状态：

- **IDLE**: 初始状态，计时器未启动
- **RUNNING**: 运行中状态
- **PAUSED**: 暂停状态

状态转换图：

```
IDLE → RUNNING → PAUSED → RUNNING → IDLE
   ↑         ↓      ↓              ↓
   └─────────┴──────┴──────────────┘
```

## 🛠️ 开发

### 环境要求

- Android SDK 21+
- Java 8+
- Gradle 7.0+

### 构建项目

```bash
./gradlew build
```

### 运行测试

```bash
./gradlew test
```

## 📄 许可证

```
Copyright 2025 xesam

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- GitHub Issues: [创建Issue](https://github.com/xesam/AndroidTimer/issues)