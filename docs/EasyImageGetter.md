# EasyImageGetter

EasyImageGetter用于为TextView加载html标签时，提供`img`标签的图片加载功能

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyImageGetterActivity.kt)

## 特性

- 支持设置`placeholde`图片加载时占位图
- 支持设置`error`图片加载失败时的占位图
- 支持指定uri进行加载。不仅仅局限于网络图片。还包括加载本地图片、assets图片等。
- 支持自定义加载器：满足各种加载需求。

## 用法

### 提供带img标签的文本数据

首先需要提供一个可用的带img标签的文本：img标签中的地址。为通用的uri地址，比如：

```
private val html =
"""
<h5>asset图片加载示例</h5>
<img src="file:///android_asset/imagegetter/cat.png">
<h5>http图片加载示例</h5>
<img src="http://www.w3school.com.cn/i/eg_tulip.jpg">
""".trimIndent()
```

可以看到。asset图片与网络图片均完美支持。当然如果需要。此处也可以配置本地图片进行展示

### 创建实例并进行展示：

```
EasyImageGetter.create().loadHtml(html, textView)
```

**加载效果图**

![](https://user-gold-cdn.xitu.io/2018/9/28/1661e407616d8701?w=271&h=295&f=png&s=41866)

### 配置占位图

组件支持两种占位图：

```
EasyImageGetter.create()
    .setPlaceHolder(R.drawable.placeholder)// 设置在进行图片加载时的占位图
    .setError(R.drawable.error)// 设置在进行图片加载失败时的占位图
    .loadHtml(html, textView)
```

### 配置额外加载器

在上面的基础用法示例中。`img`标签中的src支持任意的uri进行展示。而内部加载器是使用的`Glide`(compileOnly方式依赖)

但是有时候我们可能会需要定制自己的加载器去进行图片解析。比如项目不支持Glide。或者src中的数据格式不为规则的uri时。

这个时候就可以使用额外加载器去进行自主图片解析了：

```
val imageGetter = EasyImageGetter.create()
imageGetter.setLoader { src -> // src为img标签中的原始src属性值
    // 此加载器回调运行于子线程中。所以可以在此直接进行图片解析操作。

    // 通过src数据进行自主drawable解析加载。并返回加载的drawable数据
    val drawable = parseFromSrc(src)
    // 若返回的drawable为null。表示需要使用内置加载器继续进行drawable解析。
    return drawable
}
```

### 日志定位

有时候会出现加载失败的情况。这时候可以通过组件提供的关键字`EasyImageGetter`去进行日志过滤。便于进行问题定位

