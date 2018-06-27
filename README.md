# EasyAndroid

在平时的开发过程中，我们经常会需要使用到一些基础功能组件，比如Toast,比如Log等。

而这些功能组件，在开发时需要使用到的功能点其实相当有限，所以这也意味着，我们对此类组件的要求是：**简单、轻量、易用**！相对应的，此类组件的封装库，也应该尽量实现得**轻巧精练**

**EasyAndroid**即是专门针对此种需求所设计的一款`基础组件集成库`

### 宗旨

#### 1. 设计独立
> 组件间独立存在，不相互依赖，且若只需要集成库中的部分组件。也可以很方便的`只copy对应的组件文件`进行使用

#### 2. 设计轻巧

> 因为是组件集成库，所以要求每个组件的设计尽量精练、轻巧。避免因为一个小功能而引入大量无用代码.
>
> 每个组件的方法数均`不超过100`. 大部分组件方法数甚至`不超过50`。

### 添加依赖

1. 添加jitpack仓库依赖

```
maven { url 'https://jitpack.io' }
```

2. 添加依赖

lastest_version = [![](https://jitpack.io/v/yjfnypeu/EasyAndroid.svg)](https://jitpack.io/#yjfnypeu/EasyAndroid)

```
implementation "com.github.yjfnypeu:EasyAndroid:$lastest_version"
```

3. 初始化

在Application中调用初识化方法：

```
EasyAndroid.init(application)
```

然后即可直接使用

### 目录索引

- [EasyDimension](#easydimension): 尺寸转换组件
- [EasyFormatter](#easyformatter): 数据格式化排版组件
- [EasyLog](#easylog): 日志打印组件
- [EasyToast](#easytoast): Toast通知组件
- [EasyReflect](#easyreflect): 反射操作组件
- [EasyActivityResult](#easyactivityresult): onActivityResult解耦组件
- [EasyPermissions](#easypermissions): 动态权限申请组件
- [EasyExecutor](#easyexecutor): 线程池封装组件
- [EasyBundle](#easybundle): Bundle数据存取组件
- [MVP](#mvp): 简单MVP架构

### [EasyDimension](./docs/EasyDimension.md)

> 用于灵活的进行设备尺寸单位转换
> 
> 支持全尺寸数值转换。

用法示例

```
// 转换10dp到px
EasyDimension.withDIP(10).toPX()
// 转换30sp到MM
EasyDimension.withSP(30).toMM()
```

### [EasyFormatter](./docs/EasyFormatter.md)
> 用于对任意类型数据，进行格式化输出排版，结合log打印组件使用，使log输出展示更清晰

- 支持对`Set/List/Map/JSON/POJO`数据进行格式化排版
- 支持最高长度过滤：避免打印超长数据时造成版面浪费

用法示例：

```
// 创建待格式化数据
val any:Any = create()
// 使用formatter实例进行格式化
val result:String = EasyFormatter.DEFAULT.format(any)
// 或者使用扩展函数。直接格式化
val result2:String = any.easyFormat()
```

### [EasyLog](./docs/EasyLog.md)
> 用于简单的进行日志打印输出，支持格式化输出、自定义打印格式。

- 不阻塞：打印任务运行于独立线程中，避免大量打印数据时造成UI阻塞。
- 安全: 对打印任务做好了异常处理。不用担心出现crash问题
- 使用EasyFormatter对任意数据进行格式化排版
- 支持添加自定义规则
- 灵活、直观的进行输出样式定制
- 自动适配TAG. 也可手动指定。
- 使用开关。关闭线上包的日志输出。
- 使用'上边界'逻辑进行栈帧匹配，支持二次封装使用

用法示例：

```
val any:Any = create()// 创建待打印数据
EasyLog.DEFAULT.d(any)// 使用默认log实例进行数据打印. 以Log.d()的方式进行输出
any.easyLogE()// 使用扩展函数直接进行数据打印，以Log.e()的方式进行输出
```

### [EasyToast](./docs/EasyToast.md)
> 用于进行Toast提示，可很简单的指定输出样式。

1. 支持在任意线程下进行toast提示
2. 非常方便的进行任意样式的定制
3. 不管当前是否正在展示之前的数据。有新消息通知时，直接展示新消息，无需等待

博客地址：https://juejin.im/post/5b0638336fb9a07aa9261ce6

用法示例：

```
val message:String = create()// 创建待提示数据
EasyToast.DEFAULT.show(message)// 使用系统样式进行输出
EasyToast.create(layoutID:Int, tvID:Int, duration:Int).show(message)// 使用自定义样式进行输出
```

### [EasyReflect](./docs/EasyReflect.md)
> 对常规的反射操作进行封装。达到更便于使用反射的效果

博客地址：https://juejin.im/post/5b1de20c6fb9a01e701000cb

用法示例：

```
// 以类名Test为例
class Test private constructor(private val name:String) {
    private fun wrap(name:String):String = "包裹后的数据$name"
}

// 创建Reflect实例：
var reflect = EasyReflect.create(Test::class.java).instance("默认参数")

// 为name字段赋值：
reflect.setField("name", "EasyReflect")
// 读取name字段的值："EasyReflect"
val value = reflect.getValue("name")

// 调用方法wrap方法，并传入参数value
reflect.call("wrap", value)
// 调用wrap方法，并获取返回值: "包裹后的数据EasyReflect"
val result = reflect.callWithReturn("wrap", value).get<String>()
```

### [EasyActivityResult](./docs/EasyActivityResult.md)

>用于解决onActivityResult业务逻辑臃肿的问题

- **业务解耦分离**: 各自启动业务线处理各自的回调逻辑
- **去除requestCode**: 进行启动时自动生成随机的requestCode, 不用再为每个启动任务分别配置请求码了。
- **防暴击**: 防止快速点击时启动多个重复页面

博客地址：https://juejin.im/post/5b21d019e51d4506d93701ba

用法示例：

```
EasyActivityResult.startActivity(activity, Intent(activity, DemoActivity::class.java),
        { resultCode:Int, data:Intent? ->
            // 直接在此进行返回数据处理
        })
```

### [EasyPermissions](./docs/EasyPermissions.md)

> 进行6.0+的动态权限请求

- 链式调用
- 支持定制权限申请说明弹窗
- 支持同时申请多个权限
- 多权限申请时进行去重与空过滤
- 自动使用顶层Activity执行权限请求
- 支持在任意线程进行权限申请

博客地址：https://juejin.im/post/5b1a2a326fb9a01e5d32f208

用法示例：

```
EasyPermissions.create(// 指定待申请权限
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_CALENDAR,
    Manifest.permission.WRITE_CONTACTS
    )
    // 定制权限申请说明弹窗
    .rational { permission, chain ->
        AlertDialog.Builder(this)
                .setTitle("权限申请说明")
                .setMessage("应用需要此权限：\n$permission")
                .setNegativeButton("拒绝", {_, _ -> chain.cancel() })
                .setPositiveButton("同意", {_, _ -> chain.process() })
                .show()

        return@rational true
    }
    // 设置授权结果回调
    .callback { grant ->
        EasyToast.DEFAULT.show("权限申请${if (grant) "成功" else "失败"}")
    }
    // 发起请求
    .request()
```

### [EasyExecutor](./docs/EasyExecutor.md)

> 用于进行`安全`、`高效`、`便利`的线程池操作功能组件

- **安全**: 直接catch住任务执行期间出现的异常。并通知给用户，避免出现crash
- **回调通知**: 执行任务期间，有分别的生命周期作为通知。
- **配置灵活**: 可方便、灵活的对每次所启动的任务，配置线程名、回调等。
- **任务延迟**: 支持在每次启动任务前。指定延迟时间
- **异步任务**: 支持直接启动异步任务并回调传递数据
- **线程切换**: 支持指定回调方法所在的线程。默认为运行于UI线程中

用法示例

```
// 1. 第一步：创建示例
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

// 2. 第二步：启动任务
executor.execute(runnable:Runnable)// 启动普通任务
executor.async(callable:Callable<T>, result:(T) -> Unit)// 启动异步回调任务
executor.setDelay(delay).execute(runnable)// 延时启动任务
```

### [EasyBundle](./docs/EasyBundle.md)

> 用于使Bundle数据存取操作变得`简单`、`方便`、`灵活`、`强大`

1. 简化Bundle数据存取api：
2. 打破Bundle数据格式限制。支持对非可序列化对象进行存取。
3. 支持注入操作。在进行页面跳转传值时。将会非常好用。

博客地址：https://juejin.im/post/5b2c65bde51d45587d2dd86f

用法示例：

```
// 1. 存储任意数据对象到bundle中去
EasyBundle.create(bundle)// 绑定bundle容器
    .put(key, value)// 指定任意数据进行存储。包括非可序列化对象

// 2. 从bundle中读取并自动转换为具体对象数据
EasyBundle.create(bundle).get<User>("user")

// 3. 支持数据自动注入
class ExampleActivity:BaseActivity() {
    // 从intent中读取name数据并注入到name字段中去
    @BundleField
    var name:String? = null
}
```

### [MVP](./docs/MVP.md)

> 提供的一种简单的MVP分层架构实现。

- 支持单页面绑定多个Presenter进行使用
- 支持P层进行生命周期派发

用法示例

```
// 1. 创建页面通信协议接口：
interface CustomView:MVPView {
    // 定义通信协议方法：P层将使用此方法驱动V层进行界面更新
    fun updateUI()
}

// 2. 创建Presenter。并与对应的通信协议相绑定：
class CustomPresenter(view:CustomView):MVPPresenter(view) {
    // 直接创建对应的启动方法，供V层进行启动调用
    fun requestData() {
        // TODO 进行数据业务处理。并在处理完成后，通过view通知到V层
    }
}

// 3. 创建具体的V层(Activity or Fragment),并绑定Presenter进行使用：
class CustomActivity:BaseMVPActivity<CustomPresenter>, CustomView {
    override fun updateUI() {// TODO 进行界面更新}

    fun initPage() {
        // 通过绑定的Presenter发起任务
        presenter?.requestData()
    }
}
```

### [APIs](./docs/APIs.md)
> 提供的一些其他零散的类库APIs

## 联系作者

<a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=99e758d20823a18049a06131b6d1b2722878720a437b4690e238bce43aceb5e1"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="安卓交流会所" title="安卓交流会所"></a>

或者手动加入QQ群: 108895031

## License

[apache license 2.0](http://choosealicense.com/licenses/apache/)
