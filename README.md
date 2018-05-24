# EasyAndroid

EasyAndroid用于为Andorid开发者提供一系列**小而精**的基础组件进行使用。

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

目前版本提供以下部分工具类库:

- [EasyDimension](./docs/EasyDimension.md)
> 用于灵活的进行设备尺寸单位转换

用法示例
```
// 转换10dp到px
EasyDimension.withDIP(10).toPX()
// 转换30sp到MM
EasyDimension.withSP(30).toMM()
```
- [EasyFormatter](./docs/EasyFormatter.md)
> 用于对任意类型数据，进行格式化输出。便于展示查看

用法示例：
```
val any:Any = create()// 创建待格式化数据
val result:String = EasyFormatter.DEFAULT.format(any)// 使用formatter实例进行格式化
val result2:String = any.easyFormat()// 或者使用扩展函数。直接格式化
```

- [EasyLog](./docs/EasyLog.md)
> 用于简单的进行日志打印输出，支持格式化输出、自定义打印格式。

用法示例：
```
val any:Any = create()// 创建待打印数据
EasyLog.DEFAULT.d(any)// 使用默认log实例进行数据打印. 以Log.d()的方式进行输出
any.easyLogE()// 使用扩展函数直接进行数据打印，以Log.e()的方式进行输出
```
- [EasyToast](./docs/EasyToast.md)
> 用于进行Toast提示，可很简单的指定输出样式。

用法示例：
```
val message:String = create()// 创建待提示数据
EasyToast.DEFAULT.show(message)// 使用系统样式进行输出
EasyToast.create(layoutID:Int, tvID:Int, duration:Int).show(message)// 使用自定义样式进行输出
```
- [EasyReflect](./docs/EasyReflect.md)
> 对常规的反射操作进行封装。达到更便于使用反射的效果

用法示例：
```
// 以类名Test为例
class Test(val name:String) {
    fun wrap(name:String):String = "包裹后的数据$name"
}

// 创建Reflect实例：
var reflect = EasyReflect.create(Test())

// 为name字段赋值：
reflect.setField("name", "EasyReflect")
// 读取name字段的值："EasyReflect"
val value = reflect.getValue("name")

// 调用方法wrap方法，并传入参数value
reflect.call("wrap", value)
// 调用wrap方法，并获取返回值: "包裹后的数据EasyReflect"
val result = reflect.callWithReturn("wrap", value).get<String>()
```

- [APIs](./docs/APIs.md)
> 提供的一些其他零散的类库APIs

## 联系作者

<a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=99e758d20823a18049a06131b6d1b2722878720a437b4690e238bce43aceb5e1"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="安卓交流会所" title="安卓交流会所"></a>

或者手动加入QQ群: 108895031

## License

[apache license 2.0](http://choosealicense.com/licenses/apache/)
