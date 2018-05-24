# EasyLog

用于简单、安全、方便地进行日志格式化输出的工具类.

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyLogActivity.kt)

## 特性

- 使用EasyFormatter对任意数据进行格式化排版
- 支持添加自定义规则
- 灵活、直观的进行输出样式定制
- 自动适配TAG. 也可手动指定。
- 使用开关。关闭线上包的日志输出。
- 使用'上边界'逻辑进行栈帧匹配，支持二次封装使用

## 用法

### 使用默认实例进行日志输出

```
easyLog:EasyLog = EasyLog.Default
easyLog.d(any:Any?)     // 使用Log.d()进行打印
easyLog.i(any:Any?)     // 使用Log.i()进行打印
easyLog.v(any:Any?)     // 使用Log.v()进行打印
easyLog.w(any:Any?)     // 使用Log.w()进行打印
easyLog.e(any:Any?)     // 使用Log.e()进行打印
easyLog.wtf(any:Any?)   // 使用Log.wtf()进行打印
```

可以看到，打印器输入的数据为任意类型数据。此处的any数据将会使用**与此EasyLog实例相绑定的EasyFormatter实例**进行格式化排版。再进行日志输出。
### 使用开关

```
easyLog.enable = true|false // enable为true时才进行日志输出。
```

### 手动指定TAG

```
// 在调用对应的输出打印方法前。调用tag方法指定即可
easyLog.tag(tag).d(any)
```

请注意：此tag方法指定的'临时TAG'，只会对'下一次日志打印'起作用, 即：

```
easyLog.tag("custom").d(message1)// 自定义tag只作用于打印message1时
easyLog.e(message2)// message2仍然使用自动适配的TAG进行输出
```

默认的TAG值为**调用处所在的文件名**。

### 创建自定义日志打印器

我们可以根据自身的需要来定制对应的日志打印器进行使用

```
val newLog:EasyLog = EasyLog.newBuilder().build()
```

### 指定具体的格式化器进行排版：

```
// 1. 创建出对应的格式化器
val formatter:EasyFormatter = createFormatter()

// 2. 创建时进行绑定
val builder = EasyLog.newBuilder()
builder.formatter = formatter
...
```

### 指定输出样式

EasyLog采用字符串样式进行样式定制。达到更直观的进行样式定制的效果：

```
val builder = EasyLog.newBuilder()
builder.formatStyle = createStyle()
```

比如这是默认样式：

```
var formatStyle = """
>[EasyLog]#F
>┌──────#T───────
>│#M
>└───────────────
""".trimMargin(">")
```

EasyLog将对样式中的带#号的数据进行匹配，填充入对应的数据再进行打印。

**#F|#T|#M**为默认格式。分别匹配不同的数据：

- #F => filename+number. 此条数据用于在logcat中提供文件的索引链接
- #T => 调用处所处的线程名
- #M => 具体打印message数据的地方.
> 请注意：在一个定制样式中。必须只能有一个#M格式。

下面展示使用此样式进行打印后的数据格式：

```
EasyLog.DEFAULT.d("""
        >"第一行数据",
        >"第二行数据",
        >"第三行数据".
""".trimMargin(">"))
```

![](https://user-gold-cdn.xitu.io/2018/5/23/1638d70caa59cc25?w=169&h=19&f=png)

处上方的formatStyle以外。EasyLog还提供singleStyle样式定制：

```
val builder = EasyLog.newBuilder()
builder.singleStyle = createStyle()
```

因为formatStyle主要面向的场景是多行数据排版输出。但是实际上大部分时间我们都是打印的单行数据。
所以额外提供的singleStyle样式。主要便是用于对单行数据进行输出。避免造成性能浪费

singleStyle配置规则与formatStyle一致。默认的样式为：[EasyLog]#F ==> #M

### 添加自定义规则

```
// 创建出对应的格式化器
val formatter:EasyFormatter = createFormatter()

val builder = EasyLog.newBuilder()
builder.addRule(name:String, rule:(StackTraceElement, Thread) -> String)
```

通过[builder.addRule(name, rule)]的方式进行自定义规则的添加。name必须不含#号等特殊符号：

通过举例来说明：加入我想对单行数据日志输出末尾加上个特殊标签：

```
val builder = EasyLog.newBuilder()
builder.singleStyle = "#N - #F - #M"
builder.addRule("N", {_,_ -> "[Haoge]"})
val log = builder.build()

// 输出单行数据
log.e("这里是单行数据")
```

![](https://user-gold-cdn.xitu.io/2018/5/24/1638fc486355b8be?w=532&h=36&f=png&s=12041)

### 匹配调用处栈帧

调用处栈帧是指的进行日志打印时。方法栈中正确的调用帧，比如上方的EasyLogActivity.kt:42.即是此栈帧中的部分数据。
这样打印出来的数据才能在logcat中打印出正确的调用链接。方便跳转查看。

一般情况下直接使用EasyLog进行日志输出的。是不需要自己指定**上边界**进行栈帧匹配的。此匹配逻辑是针对需要对此进行二次封装使用时提供的。

比如进行如下二次封装：

```
object MyLog {
    private val log by lazy {
        val builder = EasyLog.newBuilder(MyLog::class.java.canonicalName)
        builder.debug = BuildConfig.DEBUG
        return@lazy builder.build()
    }

    fun d(message:Any?) {
        log.d(message)
    }

    fun e(message: Any?) {
        log.e(message)
    }
}
```

一般来说。引入的第三方库，如果有条件的话，都比较建议使用上方这种模式。进行一次二次封装后再提供使用

特别是对于这种很简单的功能库，进行简单封装后，具体功能都通过此次二次封装进行提供。
此种做法在后期，如果需要进行框架替换、升级时。省心很多

所以，对于此二次封装来说。我们在创建使用实例时。提供了自身的类名作为**上边界**, 达到自动匹配栈帧的目的。
