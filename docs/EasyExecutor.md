# EasyExecutor

用于进行`安全`、`高效`、`便利`的线程池操作功能组件

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyExecutorActivity.kt)

## 特性

- **安全**: 直接catch住任务执行期间出现的异常。并通知给用户，避免出现crash
- **回调通知**: 执行任务期间，有分别的生命周期作为通知。
- **配置灵活**: 可方便、灵活的对每次所启动的任务，配置线程名、回调等。
- **任务延迟**: 支持在每次启动任务前。指定延迟时间
- **异步任务**: 支持直接启动异步任务并回调传递数据
- **线程切换**: 支持指定回调方法所在的线程。默认为运行于UI线程中
- **进度通知**: 支持进行任务处理进度通知

## 用法

### 创建配置EasyExecutor实例

```
val executor =
    // size为所需创建的线程池的大小。当size <= 0时。
    // 表示需要使用newCachedThreadPool。
    EasyExecutor.newBuilder(size)
            .setName(name)// 默认的线程名
            .setPriority(priority)// 线程池中创建线程的优先级
            .onStart {threadName -> } // 默认任务启动时的回调
            .onSuccess {threadName -> }// 默认任务执行完毕后的回调
            .onError {threadName, throwable -> }// 默认任务执行出现异常时的回调
            .setDeliver(deliver)// 默认的回调任务派发器。用于将信息派发到指定线程去。
            .build()// 最后。执行创建
```

### 启动普通任务

```
executor.execute(runnable:Runnable)
```

### 启动异步任务

```
executor.async(task:Callable<T>, result:(T)->Unit)
```

### 延时启动任务

```
executor.setDelay(time)// 单位为毫秒，在启动任务前调用即可
    .execute(runnable:Runnable)
```

### 进行进度通知

```
executor.onProgressChanged { current, total ->
            // 接收进度变化通知。current为当前进度。total为总量
        }
        .execute { notifier ->
            // 通过notifier.progressChanged()自行指定进度状态
            notifier.progressChanged(current, total)
        }
```

### 设置当前执行任务名

```
executor.setName(name)// 只针对当前此的任务进行任务名重置
```

### 设置当前执行回调任务

```
// 只被当前次的任务所触发。且不拦截创建时指定的默认回调
executor.onStart {threadName -> }
        .onSuccess {threadName -> }
        .onError {threadName, throwable -> }
```


