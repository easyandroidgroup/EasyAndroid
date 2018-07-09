# EasyToast

一个简单易用的Toast封装类。用于提供易用的、多样式的Toast组件进行使用

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyToastActivity.kt)

## 特性

1. 一行代码搞定自定义样式Toast
2. 可直接在任意线程中进行使用
3. 当有新消息需要展示时。进行即时刷新。无需等待

## 用法

### 使用默认样式

我们来与原生使用方式进行下对比：

#### 1. 原生方式

一般来说。使用默认样式时，我们通常都是使用下方的代码进行toast展示：

```
Toast.makeText(context, message, duration).show()
```

#### 2. EasyToast方式

而对于EasyToast而言。我们只需要像下面这样调用即可：

```
EastToast.newBuilder().build().show(message)
```

或者使用资源id:

```
EasyToast.newBuilder().build().show(R.string.message)
```

当然，这个时候有人可能会说了。你这样也没比原生方式方便多少嘛！hold on! 别急。

由于这种使用`默认Toast`的场景很多。所以这里我在EasyToast类中默认提供了`DEFAULT`实例进行直接使用：

```
EasyToast.DEFAULT.show(message)
```

这样是不是就很简洁很一目了然了？

### 使用自定义样式

#### 1. 原生方式

我们先来看，使用原生的方式进行toast定制应该是怎么样的：

```
// 1. 创建Toast实例
val toast = Toast(context)
// 2. 获取自定义布局与用于展示的文本控件
val container = LayoutInflater.from(context).inflate(R.layout.custom_toast, null)
val tv = container.findViewById(R.id.toast_tv)
// 3. 将控件与toast实例绑定
toast.view = contaienr
toast.duration = Toast.LENGTH_SHORT

// 最后设置值进行展示：
tv.setText(message)
toast.show()
```

#### 2. EasyToast方式

```
// 创建实例时直接指定资源文件即可
EasyToast.newBuilder(R.layout.custom_toast, R.id.toast_tv)
	.build().show(message)
```

可以看到。与默认样式相比。在进行自定义样式使用时，EasyToast能节省更多的代码。

当然，上面的写法，将创建与展示放在一起的做法，是不推荐的。推荐的方式是进行一次`二次封装`. 将项目中所需要使用到的所有toast样式进行统一管理:

```
object ToastTools {
    val default:EasyToast = EasyToast.DEFAULT// 默认样式
    val customForNetwork:EasyToast by lazy {// 网络层独有样式
        return EasyToast.newBuilder(R.layout.network_toast, R.id.toast_tv).build()
    }
    // 更多的样式
    ...
}
```

### 添加gravity与duration配置

既然是对Toast的封装, 那么Toast本身自带的`gravity`与`duration`配置当然也是支持的。

为了方式使用时产生混乱。`EasyToast`只支持在进行实例创建时进行属性配置：

```
val toast = EasyToast.newBuilder()
	// 设置gravity. 参考Toast#setGravity(gravity, offsetX, offsetY)方法
	.setGravity(gravity:Int, offsetX:Int, offsetY:Int)
	// 设置duration.参考Toast#setDuration(duration)方法
	.setDuration(Toast.LENGTH_SHORT | Toast.LENGTH_LONG)
	.build()
```

### 在任意位置进行toast展示：

正如你上面看到的：EasyToast的展示。不再像原生toast一样。需要依赖外部传入context实例进行UI展示。而是直接指定具体的数据即可，这在一些工具库中需要进行展示时。是个很棒的特性：

```
EasyToast.DEFAULT.show(message)
```

### 在任意线程进行toast展示：

这点是原生所不能比的。使用原生的Toast。你必须在`UI线程`中进行`toast的创建与展示`

而EasyToast则没有此顾虑：因为对于EasyToast来说。toast的创建与展示。都是会被派发到主线程进行操作的：

```
fun show(message:String) {
	...
	if (Looper.myLooper() == Looper.getMainLooper()) {
	    showInternal(result)
	} else {
	    mainHandler.post { showInternal(result) }
	}
}
```

### 即时更新toast文案

我们都知道：在使用原生的`Toast.makeTest().show()`方式直接进行toast展示时。在连续发起多次toast展示请求时，系统会`依次的`对所有文案进行`一个个`的展示。

这是因为在`Toast.makeText().show()`中。每次都是使用的`新建Toast实例`进行展示. 而系统在同一时刻，只能使用`一个Toast实例`进行展示，其余的Toast实例的展示，需要`等待当前文案展示完毕后`才会被触发进行展示。

所以，要解决`多次连续展示`的问题。我们只需要做到：使用同一个Toast实例进行展示即可：

如果你需要的是进行默认样式展示，那么直接使用提供的默认Toast进行展示即可：

```
EasyToast.DEFAULT.show(content)
```

当然，有些时候，UI会要求对展示样式进行美化，所以你会需要使用自定义的Toast样式进行展示。所以此时你可以：**使用一个变量接收创建的EasyToast实例。然后在展示的地方直接使用此实例进行文案展示**：

```
// 创建自定义的EasyToast实例
val custom = EasyToast.EasyToast.newBuilder(layoutId, tvId).build()

// 在使用处。直接使用创建的实例进行展示：
custom.show(content)
```

让我们来展示一个**在子线程中连续展示toast文案**的案例：

```
executor.execute {
    for (index in 0..10) {
        EasyToast.DEFAULT.show("自动更新无延迟提醒：$index")
        Thread.sleep(300)
    }
    EasyToast.DEFAULT.show("循环完毕")
}
```

**效果展示**

![](https://user-gold-cdn.xitu.io/2018/6/12/163f32808cddb6c5?w=400&h=711&f=gif&s=292801)

可以明显看到。界面展示的toast文案是被`即时更新`的