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

EasyExecutor是对线程池进行的封装，所以在创建时，我们需要指定需要创建的`线程池的大小`

```
val builder = EasyExecutor.newBuilder(size)
... // 其他配置
val executor = builder.build()// 配置完成后再创建EasyExecutor提供使用
```

参数size为`Int`类型，即为指定的`线程池大小`，size与创建的线程池的关系为：

```
val executor = when {
	// size小于1, 创建可缓存大小的线程池提供使用
	size <= 0 -> Executors.newCachedThreadPool(createFactory())
	// size大于0, 创建指定大小的线程池。
	else -> Executors.newFixedThreadPool(size, createFactory())
}
```

### 线程优先级配置

可以通过以下方式，指定线程池`创建出来的线程优先级`：

```
builder.setPriority(priority)
```

### 线程任务名配置

所谓`任务名`即是创建出来的`线程的线程名`, 而指定任务名的方式有以下两种：

> 1.指定默认任务名:在创建时进行指定

```
builder.setName("default name")
```

> 2.指定临时任务名:在使用前进行指定

```
executor.setName("temp name")
```

`临时`与`默认`任务名的关系是：在启动一次后台任务时：

- 当`有配置临时任务名`时：使用`临时任务名`作为此次的线程任务名，并将此`临时任务名进行重置`
- 当`没配置临时任务名`时：使用`默认任务名`作为此次的线程任务名。

### 启动异步任务

普通异步任务，直接创建任务进行启动即可：

```
executor.execute { // TODO }
```

很多时候，在java原生中的使用习惯是：普通任务都是通过`Runnable`接口进行定义，所以`EasyExecutor`也提供了直接使用`Runnable`任务的重载方法：

```
val runnable:Runnable = createTask()
executor.execute(runnable)
```

### 启动异步回调任务

异步回调任务：用于在需要接收异步任务返回值时使用:

```
executor.async(task:Callable<T>, result:(T)->Unit)
```

比如说。在子线程进行Bitmap创建:

```
executor.async(
	{ // 异步任务
		// 子线程中，进行Bitmap创建，
		return@Callable bitmap
	},
	{ bitmap -> // 异步回调，默认运行于UI线程
		// TODO 使用创建好的bitmap进行操作
		// 关于回调线程的派发，后面 派发器 小节会进行详细说明
	}
)
```

### 启动延迟任务

如果需要指定`此次任务`需要被`延迟执行`时。使用`setDelay`直接指定`延迟时间`即可：

```
executor.setDelay(delayTime)
```

请注意: `delayTime`类型为`Long`, 单位为`毫秒`。且此`延迟时间`的有效作用域是`此次被启动的任务`。一旦有任务被启动之后。`延迟时间`将被重置。也就是：

```
executor.setDelay(3000)
executor.execute(task1)// task1任务将被延迟3秒执行
executor.execute(task2)// task2任务将被直接执行
```

### 安全的进行回调派发

`EasyExecutor`提供三个回调方法：

| 类型 | lambda | 说明 |
|:-----|----|:-----|
| onStart| (String) -> Unit |当线程任务被执行时被触发，参数为`任务名` |
| onError| (String, Throwable) -> Unit | 当线程任务执行出现异常时被触发, 参数为`任务名`和`出现的异常`|
| onSuccess| (String) -> Unit| 当线程任务执行完成时被触发，参数为`任务名`|

此三种回调，也有`默认回调配置`和`临时回调配置`的区别

> 指定默认回调：作用域为所有的线程任务

```
builder.onStart {threadName -> } // 所有任务启动时的回调
    .onSuccess {threadName -> }// 所有任务执行完毕后的回调
    .onError {threadName, throwable -> }// 所有任务执行出现异常时的回调
```

> 指定临时回调：作用域为此次启动的线程任务

```
executor.onStart {threadName -> } // 此次任务启动时的回调
    .onSuccess {threadName -> }// 此次任务执行完毕后的回调
    .onError {threadName, throwable -> }// 此次任务执行出现异常时的回调
```

所以，与`任务名配置`不同。临时回调不会覆盖掉默认回调，而他们被触发的先后顺序是：**先触发默认回调。再触发临时回调**

而所谓`安全`,即是在整个任务的执行过程中。会将执行过程中`出现的异常`进行捕获。防止`任务执行出错导致crash`。被捕获的异常将会通过`onError`回调。通知到指定线程进行处理：

```
Thread.currentThread().setUncaughtExceptionHandler {
    name, e ->
    // deliver：派发器。将消息通知到指定线程。
    deliver.execute {
        builder.error?.invoke(name, e)// 默认回调异常通知
        error?.invoke(name, e)// 临时回调异常通知
    }
}
```

### 配置派发器

就以上面的`三个回调方法`为例：我们在说的时候。只说了它们被触发的时机，但是没说它们具体运行在哪个线程中。而这，就是派发器干的事：

派发器的作用: 就是`将消息派发到指定线程中去之后再进行用户通知！`

派发器的本质，是一个`Executor`接口的实现类：

```
private var deliver:Executor = UIDeliver
```

而默认使用的`UIDeliver`，就是专门针对`Android运行时环境`创建的：**将消息派发到UI线程进行通知**

```
private val UIDeliver:Executor = Executor { runnable ->
    if (Looper.myLooper() == Looper.getMainLooper()) {
        runnable.run()
    } else {
        mainHandler.post { runnable.run() }
    }
}
```

而对于派发器来说。也存在`默认配置`与`临时配置`

> 默认配置：当不存在临时派发器配置时，使用此默认派发器

```
builder.setDeliver(Executor {})
```

> 临时配置：只对此次启动任务生效

```
executor.setDeliver(Executor {})
```

需要注意的是：`派发器也对异步回调任务生效`。所以在默认配置下，异步回调也是运行于UI线程中的：

```
executor.async(
	Callable<T> {return T},
	{ T ->
		// 此回调所处线程也受派发器控制。
	}
)
```

### 进行异步任务进度通知

有些时候，我们会需要在任务的处理过程中，对外进行进度状态通知，以便进行上传的进度UI更新。使用`EasyExecutor`。也可以很方便的做到进度通知的效果：

需要进行状态通知，首先需要定制进度回调通知：

```
executor.onProgressChanged { current:Long, total:Long ->
	// 传参current为当前的进度， total为数据总量。
}
```

而`EasyExecutor`本身提供的`任务模型`就提供了有进行外部状态通知的实例：

```
// 普通任务：
executor.execute { notifier -> }
// 异步任务
executor.async( { notifier -> }, { result -> })
```

任务实例中的传参notifier即是用于进行外部通知的类。对于需要监听状态通知的任务。可以通过此实例方便的指定进度信息。

所以一个完整的`进度回调任务`模型应该是如以下代码一样：

```
executor.onProgressChanged {
	current, total ->
		TODO("进行进度变化通知展示")
	}
	.execute { notifier ->
		// 在合适的处理进度中。使用以下api进行进度状态派发即可
		notifier.progressChanged(current, total)
	}
```

