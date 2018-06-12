# EasyToast

一个简单易用的Toast封装类。用于提供易用的、多样式的Toast组件进行使用

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyToastActivity.kt)

## 特性

1. 非常方便的进行Toast样式定制
2. 可直接在任意线程中进行使用
3. 当有新消息需要展示时。进行即时刷新。无需等待

## 用法

### 1. 创建EasyToast的两种方式：

- 创建使用默认样式的EasyToast实例：

```
使用无参方法创建默认样式Toast。
val toast = EasyToast.newBuilder().build()
```

- 指定布局文件与文本id。创建自定义样式

```
// 指定toast的布局文件layoutId与文本控件tvId. 创建自定义样式Toast
val toast = EasyToast.newBuilder(layoutId:Int, tvId:Int).build()
```

### 2. 在任意位置进行toast展示

EasyToast实例生成好之后。即可在`任意线程`中进行展示：

```
// 1. 展示指定资源ID内容
toast.show(R.string.message)
// 2. 展示指定展示文本
toast.show("展示文本")
```

### 3. 配置gravity

为了避免使用混乱。EasyToast只支持在进行`EasyToast实例创建过程中`进行位置调整

```
val toast = EasyToast.newBuilder()
		.setGravate(gravity:Int, offsetX:Int, offsetY:Int)// 指定gravity位置与偏移量
		.build()
```

### 4. 配置duration

与`gravity`配置一样, 对`duration`的配置也`只允许`在创建过程中进行配置：

```
val toast = EasyToast.newBuilder()
		.setDuration(Toast.LENGTH_SHORT|Toast.LENGTH_LONG)
		.build()
```

### 5. 使用EasyToast.Default

因为很多时候。其实我们需要使用的就是默认的Toast样式。所以在这里，EasyToast内部直接提供了`EasyToast.Default`实例。让你可以直接使用它进行Toast展示：

```
EasyToast.Default.show("Hello EasyToast!")
```