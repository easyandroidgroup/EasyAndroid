# EasyBundle

EasyBundle顾名思义，主要是对Bundle的封装。作用在于使Bundle的存取操作变得`灵活、方便、简洁`

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyBundleActivity.kt)

## 特性

1. 统一存取api
2. 支持存储任意类型数据，打破Bundle数据限制
3. 自动类型转换。读取随心
4. Bundle与实体类之间的双向数据注入

## 用法

### 用法概览

我们先来与`原生`使用方式进行一下`对比`。以便让大家能对`EasyBundle`的用法有个大概的概念

假设我们有以下一批数据，需要进行存储

| 类型 | 值 |
| :-----| :------|
| Int | age|
| String| name|

- **原生存储**:需要根据存储类型不同选择不同的api

```
val bundle = getBundle()
bundle.putInt("age", age)
bundle.putString("name", name)
```

- **使用EasyBundle进行存储**:统一存储api。直接存储

```
val bundle:Bundle = EasyBundle.create(getBundle())
	.put("age", age)
	.put("name", name)
	.getBundle()
```

- **原生读取**:需要根据容器中的`不同类型`, 选择`不同api`进行读取

```
val bundle = getBundle()
val age:Int = bundle.getInt("age")
val name:String = bundle.getString("name")
```

- **使用EasyBundle进行读取**：统一读取api。直接读取

```
val easyBundle = EasyBundle.create(getBundle())
val age = easyBundle.get<Int>("age")
val name = easyBundle.get<String>("name")
```

- **原生方式页面取值**

```
class ExampleActivity:Activity() {
	var age:Int = 0
	var name:String = ""

	override fun onCreate(saveInstanceState:Bundle?) {
		super.onCreate(saveInstanceState)
		intent?.let{
			age = it.getIntExtra("age", 0)
			name = it.getStringExtra("name")
		}
	}
}
```

- **使用EasyBundle进行页面取值**

```
class BaseActivity() {
	override fun onCreate(saveInstanceState:Bundle?) {
		super.onCreate(saveInstanceState)
		// 在基类中直接配置注入入口，将intent中的数据注入到配置了BundleField注解的变量中去
		EasyBundle.toEntity(this, intent?.extras)
	}
}

class ExampleActivity:BaseActivity() {
	// 在对应的字段上添加BundleField即可
	@BundleField
	var age:Int = 0
	@BundleField
	var name:String = ""
	...
}
```

- **原生方式进行现场保护**

```
class ExampleActivity:Activity() {
	var age:Int = 0
	var name:String = ""

	// 原生方式。需要手动一个个的进行数据存储
	override fun onSaveInstanceState(outState: Bundle?) {
		super.onSaveInstanceState(outState)
		outState?.let{
			it.putInt("age", age)
			it.putString("name", name)
		}
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
		super.onRestoreInstanceState(savedInstanceState)
		saveInstanceState?.let {
			age = it.getIntExtra("age", 0)
			name = it.getStringExtra("name")
		}
	}
}
```

- **使用EasyBundle进行现场保护配置**

```
// 直接在基类中进行基础注入配置即可
class BaseActivity() {
	override fun onSaveInstanceState(outState: Bundle?) {
		super.onSaveInstanceState(outState)
		EasyBundle.toBundle(this, outState)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
		super.onRestoreInstanceState(savedInstanceState)
		EasyBundle.toEntity(this, savedInstanceState)
	}
}
```

以上即是EasyBundle的各种主要使用方式。希望能让大家对EasyBundle的主要功能先有个大致了解。

### EasyBundle实例创建说明

EasyBundle是对Bundle的存取操作进行封装的，那么肯定我们会需要绑定一个Bundle对应进行操作

```
val easyBundle:EasyBundle = EasyBundle.create(bundle)
```

然后，通过easyBundle操作完数据后，取出操作后的bundle数据进行使用：

```
val bundle:Bundle = easyBundle.bundle
```

若创建时传递进入的bundle为`null`。则将新建一个`空的bundle容器`进行数据存储

```
fun create(source:Bundle? = null): EasyBundle {
    return EasyBundle(source?: Bundle())
}
```

所以。我们再返回去看上面的存储示例代码，就很清晰了：

```
val bundle:Bundle = EasyBundle.create(getBundle())
	.put("age", age)
	.put("name", name)
	.getBundle()
```

### 统一存取api

从上面的示例中我们可以看得出来：相比于原生方式(需要针对`不同类型数据`使用`不同的api`进行数据存取), `EasyBundle`统一了存取的api：

#### 统一存储的三种方式

1. 直接使用`put(key:String, value:Any)`方法一个个进行存储：

```
easyBundle.put(key1, value1)
	.put(key2, value2)// 支持链式调用
```

2. 通过提供的带可变参数的方法`put(vararg params:Pair<String, Any>)`进行多数据同时存储

```
easyBundle.put(
	key1 to value1,
	key2 to value2
	...
)
```

3. 直接存储别人传过来的map数据`put(params:Map<String, Any>)`

```
val map:Map<String, Any> = getMap()
easyBundle.put(map)
```

#### 统一读取

统一了数据的存储入口。理所当然的，`EasyBundle`也统一了数据的读取入口：

需要进行读取时。可以通过内联函数`get<T>(key:String)`读取指定数据.

比如读取`实现了Parcelable接口的User`实例:

```
val user = easyBundle.get<User>("user")
```

而在java环境下。因为没有内联函数可用，所以你也可以使用`get(key:String, type:Class<*>)`方法进行读取

```
User user = easyBundle.get("user", User.class)
```

### 打破Bundle存储数据限制

都知道，Bundle的存取api那么复杂，主要是需要过滤掉`不被系统允许的非序列化数据`。

所以经常性的。有时候我们在开发中，突然会需要将一个`普通的实体类`传递到下一个页面。这个时候就会需要对这个类进行序列化修改。

虽然实际上对类进行实现序列化接口还是很简单的。但是经常需要去实现，也是让人神烦的。

解决办法其实很简单，参考经典的网络通信模型即可：**使用JSON作为中转类型进行通信**

以下方的User为例：

```
class User() {
	val name:String? = null
}
```

**进行存储**

```
easyBundle.put("user", user)
```

存储时，自动对user进行`类型检查`，发现此类型`不被bundle所支持存储`，所以会将user通过`fastjson`或者`gson`进行`json序列化转码`后，再进行存储.

**核心源码展示**

```
fun put(name:String, value:Any?) {
	...
	when (value) {
		// 首先，对于Bundle支持的数据类型。自动选择正确的api进行存储
		is Int -> bundle.putInt(name, value)
		is Long -> bundle.putLong(name, value)
		...
		// 对于Bundle不支持的数据类型。转换为临时中间JSON数据再进行存储
		else -> bundle.putString(name, toJSON(value))
	}
}
```

**进行读取**

```
val user:User = easyBundle.get<User>("user")
```

读取时，从bundle中取出的是`json字串`。与指定类型`User`不匹配。则将通过`fastjson`或者`gson`进行`json反序列化解析`后。再进行返回：

除了此处所举例的`JSON数据自动转换兼容`方案。还有一种是`基本数据类型转换兼容`:

比如当前bundle中放入了数字的字符串:

```
easyBundle.put("number", "10086")
```

虽然我们存入的时候是String类型数据。但是内容实际上是可以转为int的。那么我们也可以直接`指定接受者类型为int`来进行读取：

```
val number:Int = easyBundle.get<Int>("number")
```

`基本类型兼容`的方式。在使用路由的项目下进行使用。非常好用：

**因为路由框架中，url的参数部分，大部分都是直接以String的格式进行解析、传递的**

**核心源码展示：**

```
fun <T> get(key:String, type:Class<T>):T? {
    var value = bundle.get(key) ?: return returnsValue(null, type) as T?
    // 当取出数据类型与指定类型匹配时。直接返回
    if (type.isInstance(value)) {
        return value as T
    }

    if (value !is String) {
        // 对于数据类型不为String的，先行转换为json。
        value = toJSON(value)
    }

    // 处理两种情况下的数据自动转换：
    val result = when(type.canonicalName) {
    	// 第一种：基本数据类型数据自动转换兼容
		"byte", "java.lang.Byte" -> value.toByte()
		"short", "java.lang.Short" -> value.toShort()
		...
		// 第二种：JSON数据自动解析兼容
		else -> parseJSON(value, type)
    }
    return result as T
}
```

**关于EasyBundle中，json中转数据的说明**

在EasyBundle中。并没有直接依赖`fastjson`与`gson`解析库。而是通过在运行时进行`json库匹配`。使用当前的运行环境所支持的`json解析库`：

```
// 当前运行环境下。是否存在fastjson
private val FASTJSON by lazy { return@lazy exist("com.alibaba.fastjson.JSON") }
// 当前运行环境下，是否存在gson
private val GSON by lazy { return@lazy exist("com.google.gson.Gson") }

// 进行json库判断。优先使用gson
private fun toJSON(value:Any) = when {
    GSON -> Gson().toJson(value)
    FASTJSON -> JSON.toJSONString(value)
    else -> throw RuntimeException("Please make sure your project support [FASTJSON] or [GSON] to be used")
}

private fun parseJSON(json:String, clazz: Class<*>) = when {
    GSON -> Gson().fromJson(json, clazz)
    FASTJSON -> JSON.parseObject(json, clazz)
    else -> throw RuntimeException("Please make sure your project support [FASTJSON] or [GSON] to be used")
}
```

所以，完全不用担心会引入新的不需要的库进来。而且，相信大部分的项目中也肯定有`fastjson`与`gson`至少其中一种解析库。

### 双向数据注入

`EasyBundle`提供了`BundleField`注解。用于提供`双向数据注入`功能。

双向注入的意思即是：即可以将数据`从实体类中`注入到`bundle容器中`，也可以`从bundle容器中`注入到`实体类中`:

举个栗子,这是个普通bean类，存储着用户信息：

```
class User(var name:String, var arg:Int, var address:String)
```

然后。正常模式下。当我们需要将这些数据存储到bundle中去时：

```
val user = getUser()
bundle.putString("name", user.name)
bundle.putInt("age", user.age)
bundle.putString("address", user.address)
```

或者，需要从bundle中将对应的数据取出来并赋值给user:

```
user.name = bundle.getString("name")
user.age = bundle.getInt("age")
user.address = bundle.getString("address")
```

但是，如果你使用`EasyBundle`提供的`双向数据注入`功能，就很简单了：

**1. 为需要进行注入的字段。添加注解：**

```
class User(@BundleField var name:String,
	@BundleField var arg:Int,
	@BundleField var address:String)
```

**2. 将数据从User中注入到bundle中进行保存**

```
EasyBundle.toBundle(user, bundle)
```

**3. 将数据从bundle中，读取并注入到User实例中去：**

```
EasyBundle.toEntity(user, bundle)
```

效果与上方的原始写法一致。且`更加方便、更加简洁、更加强大`。

#### 重新指定key值

一般来说。直接使用`@BundleField`时。默认使用的key值是`字段名`。

但是有时候，我们会需要对key值进行重设：

```
class Entity(@BundleField("reset_name") var name:String)
```

#### 防crash开关

在进行数据存取的过程中，很难避免不会出现存取异常。比如说。你存的是`"Hello,World"`, 但是取的时候你却取成了`Int`。或者存的是json。但是读取的时候，进行json解析错误时。这些情况下都会导致抛出不可期的异常

所以`BundleField`提供了`throwable`参数:

```
@BundleField(throwable = false)
var user:User
```

`throwable`类型为Boolean。代表当存取时发生异常时。是否将此异常向上抛出。(默认为false)

### 数据注入的使用场景

上面虽然说了那么长一截，但是如果没有具体的使用场景示例的支撑。可能会有部分朋友不太理解: **你说了那么多，然而又有什么卵用？**

下面我就举例一些使用场景。进行一些具体的说明：

#### 1. 页面跳转Intent传值

这其实可以说是主要的使用场景。在Activity中进行使用，获取启动时传递的数据：

```
class UserActivity:Activity() {
	@BundleField
	lateinit var name:String
	@BundleField
	lateinit var uid:String

	override fun onCreate(saveInstanceState:Bundle?) {
		// 将intent中的数据。注入到当前类中
		EasyBundle.toEntity(this, intent?.extras)
	}
}
```

当然。其实每次有个新页面。都去写一次`EasyBundle.toEntity`也是挺蛋疼的

其实注入方法是可以放入基类的。做到`一次基类配置，所有子类共用`

```
class BaseActivity:Activity() {
	override fun onCreate(saveInstanceState:Bundle?) {
		// 将intent中的数据。注入到当前类中
		EasyBundle.toEntity(this, intent?.extras)
		...
	}
}
```

而且。使用此种方式，有个很显著的优点：比如对于上方所示的`UserActivity`页面来说。此页面需要的数据就是`name`与`uid`，一目了然~

#### 2. 现场状态保护

照原生的方式。我们在进行现场保护时，会需要自己去将`关键状态数据`一个个的`手动存入saveInstanceState`中去，需要恢复数据时，又需要一个个的去`手动读取数据`.

比如像下方的页面：

```
class PersonalActivity:Activity() {
	// 此类中含有部分的关键状态变量
	lateinit var name:String
	var isSelf:Boolean = false
	...

	// 然后需要进行现场状态保护。存储关键数据：
	override fun onSaveInstanceState(outState: Bundle?) {
	    super.onSaveInstanceState(outState)
	    outState.putString("name", name)
	    outState.putBoolean("isSelf", isSelf)
	}
	// 页面待恢复时，将数据读取出来进行恢复
	override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
	    super.onRestoreInstanceState(savedInstanceState)
	    if (saveInstanceState == null) return
	    name = saveInstanceState.getString("name")
	    isSelf = saveInstanceState.getBoolean("isSelf")
	}
}
```

这只是两个变量需要保存。如果数据量较多的环境下。这块就得把人写疯。。。

而`EasyBundle`的双向数据注入功能，在此处就能得到非常良好的表现：

```
class PersonalActivity:Activity() {
	// 此类中含有部分的关键状态变量
	@BundleField
	lateinit var name:String
	@BundleField
	var isSelf:Boolean = false
	...

	// 然后需要进行现场状态保护。存储关键数据：
	override fun onSaveInstanceState(outState: Bundle?) {
	    super.onSaveInstanceState(outState)
	    EasyBundle.toBundle(this, outState)
	}
	// 页面待恢复时，将数据读取出来进行恢复
	override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
	    super.onRestoreInstanceState(savedInstanceState)
	    EasyBundle.toEntity(this, savedInstanceState)
	}
}
```

当然，推荐的做法还是将此`配置到基类`. 使上层的代码更加简洁：

```
class BaseActivity:Activity() {
	override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        EasyBundle.toBundle(this, outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        EasyBundle.toEntity(this, savedInstanceState)
    }
}
```

当然，你也可以拓展到任意你需要使用到的地方。

#### 3. 兼容路由跳转参数传递

上面说了，`EasyBundle`支持了`基本类型`的兼容逻辑。此兼容逻辑，主要其实就是用来出来路由参数传递的问题

比如我们有以下一个路由跳转链接：

```
val url = "Haoge://page/user?name=Haoge&age=18"
```

从链接可以看出来，其实我们需要传递的参数有两个：`String`类型的`name`, `Int`类型的`age`

但是路由框架可没此目测功能，所以基本来说。解析后放入intent中传递的数据，都是`String`类型的`name`与`age`

所以照正常逻辑：我们在目标页面。对`age`的取值。会需要将数据先读取出来再`进行一次转码`后方可使用

```
class UserActivity:BaseActivity() {
	lateinit var name:String
	lateinit var age:Int

	override fun onCreate(saveInstanceState:Bundle?) {
		// 从intent中进行读取
		name = intent.getStringExtra("name")
		age = intent.getStringExtra("age").toInt()// 需要再进行一次转码
	}
}
```

而使用注入功能，则不用考虑那么多，直接怼啊！！！

```
class UserActivity:BaseActivity() {
	@BundleField
	lateinit var name:String
	@BundleField // 读取时，会进行自动转码
	lateinit var age:Int
}
```

#### 4. 指定默认值

```
@BundleField
var age:Int = 18 // 直接对变量指定默认数据即可
```

### 混淆配置

因为自动注入操作使用了反射进行操作。所以如果需要对项目进行混淆的。记得添加上以下混淆规则：

```
-keep class com.haoge.easyandroid.easy.BundleField
-keepclasseswithmembernames class * {
    @com.haoge.easyandroid.easy.BundleField <fields>;
}
```

更多使用场景。期待你的发掘~~~