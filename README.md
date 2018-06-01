# EasyAndroid

在平时的开发过程中，我们经常会需要使用到一些基础功能组件，比如Toast,比如Log等。

而这些功能组件，在开发时需要使用到的功能点其实相当有限，所以这也意味着，我们对此类组件的要求是：**简单、轻量、易用**！相对应的，此类组件的封装库，也应该尽量实现得**轻巧精练**

**EasyAndroid**即是专门针对此种需求所设计的一款**基础组件集成库：**

### 宗旨

#### 1. 设计独立
> 组件间独立存在，不相互依赖，若只需要集成库中的部分功能，可直接非常方便的copy源码进行使用。

#### 2. 设计轻巧
> 因为是组件集成库，所以要求每个组件的设计尽量精练、轻巧。避免因为一个小功能而引入大量无用代码

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

目前版本已集成以下部分组件库:

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

用法示例：

```
val message:String = create()// 创建待提示数据
EasyToast.DEFAULT.show(message)// 使用系统样式进行输出
EasyToast.create(layoutID:Int, tvID:Int, duration:Int).show(message)// 使用自定义样式进行输出
```

### [EasyReflect](./docs/EasyReflect.md)
> 对常规的反射操作进行封装。达到更便于使用反射的效果

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

### [MVP](./docs/MVP.md)

> 提供的一种简单的MVP分层架构实现。

- 支持单页面绑定多个Presenter进行使用
- 支持P层进行生命周期派发

### [APIs](./docs/APIs.md)
> 提供的一些其他零散的类库APIs

## 联系作者

<a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=99e758d20823a18049a06131b6d1b2722878720a437b4690e238bce43aceb5e1"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="安卓交流会所" title="安卓交流会所"></a>

或者手动加入QQ群: 108895031

## License

[apache license 2.0](http://choosealicense.com/licenses/apache/)
