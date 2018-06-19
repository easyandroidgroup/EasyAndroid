# EasyBundle

EasyBundle顾名思义，主要是对Bundle的封装。作用在于使Bundle的存取操作变得`灵活、方便、简洁`

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyBundleActivity.kt)

## 特性

1. 简化Bundle数据存取api：
> 不需要再根据待存取的数据类型。进行`putXXX/getXXX`的方法选择了。统一为`put/get`
2. 打破Bundle数据格式限制。支持对非可序列化对象进行存取。
> 非可序列化对象将会转换为json后进行存储
3. 支持注入操作。在进行页面跳转传值时。将会非常好用。

## 用法

### 创建EasyBundle实例

```
// 传入具体的Bundle实例进行处理。当传入为null时，将默认创建一个空的Bundle实例提供使用
val easyBundle = EasyBundle.create(bundle)

... // 具体操作区

// 操作完成后，获取操作后的bundle实例进行使用
val bundle = easyBundle.bundle
```

### 统一存取api

EasyBundle简化了存取api。 **不用像原生Bundle一样，需要根据指定数据类型选择使用不同的api进行使用:**

以存取`String、Parcelable、Serializable`实例为例：

```
val string = "Hello world"
val parcelable = ParcelableSubclass()
val serializable = SerializableSubclass()
```

#### 统一存储

EasyBundle提供了三种重载方法进行数据存储

1. 统一使用put方法进行存储, 且支持链式调用

```
easybundle.put(key1, string)
    .put(key2, parcelable)
    .put(key3, serializable)
```

2. 或者，你也可以使用提供的带可变参数的方法进行多数据存储

```
easyBundle.put(key1 to String,
    key2 to parcelable,
    key3 to serializable)
```

3. 当然，你也可以传入一个存在的map实例

```
easyBundle.put(mapOf<String, Any>(
            key1 to string,
            key2 to parcelable,
            key3 to serializable))
```

#### 统一读取

1. 通过内联函数指定`数据泛型`进行读取

```
val string = easyBundle.get<String>(key1)
val parcelable = easyBundle.get<ParcelableSubclass>(key2)
val serializable = easyBundle.get<SerializableSubclass>(key3)
```

2. 或者。直接通过`指定class`进行读取

```
val String = easyBundle.get(key1, String::class.java)
val parcelable = easyBundle.get(key2, ParcelableSubclass::class.java)
val serializable = easyBundle.get(key3, SerializableSubclass::class.java)
```

### 打破bundle数据存储限制

EasyBundle破除了存储的入口限制，所以也理所应当的，**破除了Bundle的数据存储限制**

意思即是：**EasyBundle允许你向Bundle内部存储任意的数据类型实例**

以下方的`非可序列化类`为例：

```
class Info {
    val name:String? = "Info's name"
}
```

- 进行存储：

```
val info = Info()
easyBundle.put("info", info)
```

- 进行读取

```
val info = easyBundle.get<Info>("info")
```

可以看到:Info本身并未实现序列化接口。但是也是可以通过EasyBundle直接进行存取操作的。

这是因为`EasyBundle`采用的是`JSON`作为数据中转格式：
- 在进行存储时：将不能被Bundle直接存储的`(非可序列化对象)`转为`JSON`数据，再进行存储
- 在进行读取时：取出的数据与实际要求的类型不匹配。通过'JSON'数据作为中转，并解析出要求的数据对象返回

我们来通过部分`核心代码`来进行说明：

- 存储时：

```
fun put(name:String, value:Any?):EasyBundle {
    when (value) {
        // 对于bundle支持的数据格式，直接使用对应的api进行存储
        is Int -> bundle.putInt(name, value)
        ...
        // 对于不满足条件的，进行json转码后再进行存放
        else -> bundle.putString(name, toJSON(value))
    }

    return this
}
```

- 读取时：

```
fun <T> get(key:String, type:Class<T>):T? {
    var value = bundle.get(key) ?: return returnsValue(null, type) as T?
    // 当取出数据类型与指定类型匹配时。直接返回
    if (type.isInstance(value)) {
        return value as T
    }

    if (value !is String) {
        // 不匹配类型，使用json作为数据中转站
        value = toJSON(value)
    }
    value = value as String

    // 处理两种情况下的数据自动转换：
    val result = when(type.canonicalName) {
        // 兼容基本数据类型
        "byte", "java.lang.Byte" -> value.toByte()
        ...
        // 对不匹配类型数据。使用json进行反序列化解析。
        else -> parseJSON(value, type)
    }
    return result as T
}
```

源码很简单。相信很容易看懂。

而在读取时，也对基本数据类型做判断兼容的好处是：可以做到很好的兼容市面上的路由框架。

#### 路由传参的兼容方案

我们都知道。路由的传参，有相当一部分的数据是通过`url`自带的`params`进行的数据传递。
而这些`params`解析后放入intent的数据。基本上都是`String`类型。所以在数据接收页，
普遍的还会需要自己去进行数据解析，这样很容易导致可维护性降低。

所以EasyBundle自带的读取时解析数据。在这种场景下就能得到很好的应用：

以下方所示的链接为例：

```
val uri = Uri.parse("haoge://page/example").buildUpon()
    .appendQueryParameter("int", "12")
    .appendQueryParameter("user", JSON.toJSONString(User("Haoge")))
    .appendQueryParameter("name", "Haoge")
    .build()
```

为了便于展示说明。这里采用builder的方式进行了url的创建。传递一个`基本数据类型`一个`JSON`数据，

所以。在解析url时，这部分的参数将会被解析后存入intent中进行传递：

```
val intent = getIntent()
intent.putExtra("int", uri.getQueryParameter("int"))
intent.putExtra("user", uri.getQueryParameter("user"))
intent.putExtra("name", uri.getQueryParameter("name"))
```

然后在参数接收页。按照常规做法。我们应该是要先自己从intent中读取数据。然后自己转换成对应数据后再进行使用：

但是使用EasyBundle即可以不用那么麻烦：

```
val easyBundle = EasyBundle.create(intent.extras)
val int = easyBundle.get<Int>("int")
val user = easyBundle.get<User>("user")
val name = easyBundle.get<String>("name")
```

### 使用BundleField做自动数据注入

`EasyBundle`提供`BundleField`注解作自动数据注入

类似于ButterKnife。EasyBundle可以很方便的，从`bundle`容器中，将数据自动注入到实体类中的`对应成员变量`中去。

最经典的用法是进行页面传参时进行使用：仍以上方路由传参的几个参数作为说明：

```
class InjectorActivity:Activity() {
    // 配置可注入的参数. 添加BundleField注解即可
    @BundleField("name")
    var name:String? = null
    @BundleField("int")
    var int:Int = 0
    @BundleField("user")
    var user:User? = null

    override fun onCreate(saveInstanceState:Bundle?) {
        super.onCreate(saveInstanceState)
        // 执行注入操作
        EasyBundle.toEntity(this, intent?.extras)
    }
}
```

这样的操作可以大大的提高代码的可读性。不用再去自己单独手动进行读取了。

当然，每个类都去手动调用`toEntity`方法，也是很蛋疼的。所以`EasyBundle`也支持将注入api入口配置到基类中去。

而且，结合`EasyBundle`的另一个`反向注入`api：`toBundle`。能够达到很方便的`现场数据保存`的效果

```
abstract class BaseActivity:Activity() {

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 自动触发注入操作
        EasyBundle.toEntity(this, intent?.extras)
    }

    // ==== 自动进行现场保护. 可选配置
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        // 将当前类中的被BundleField注解的变量。注入到outState中进行保存
        EasyBundle.toBundle(this, outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // 将savedInstanceState中的数据注入到当前类中被BundleField注解的成员变量中
        EasyBundle.toEntity(this, savedInstanceState)
    }
}
```

#### BundleField参数说明

BundleField提供两个参数：

```
annotation class BundleField(val value:String = "", val throwable:Boolean = true)
```

**1. value**: 参数的key值，当为空时，代表使用成员变量的变量名作为key值使用
**2. throwable**: 在进行数据注入时，当出现异常时，是否允许抛出异常。

#### 指定参数默认值

很多时候我们会需要为某个参数指定默认值。可以通过`直接为变量配置默认值`的方式进行配置：

```
@BundleField
var name:String = "this is default name"
```
