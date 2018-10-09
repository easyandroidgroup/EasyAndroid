# EasySharedPreferences

EasySharedPreferences对`SharedPreferences`的操作进行封装，简化存取操作

其做法为：将SharedPreferences的数据映射到指定的实体类中去。避免到处去指定key。进行硬编码存储

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasySharedPreferencesActivity.kt)

## 特性

1. 通过具体的实体类进行SharedPreferences数据存取操作。避免`key值硬编码`
2. 自动同步，即使别的地方是`直接使用SharedPreferences进行赋值`，也能自动同步相关数据。
3. 打破SharedPreferences限制。支持几乎任意类型数据存取

## 用法与原理

### 用法概览

这里先来通过一个例子来先进行一下大致的了解：

比如现在有这么个配置文件：文件名为user_info，内部存储了一些用户特有的信息：

使用原生的方式。读取时，我们需要这样写：

```
val preference = context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
val username = preference.getString("username")
val address = preference.getString("address")
val age = preference.getInt("age")
```

而在需要进行数据修改时：我们需要这样写：

```
val editor = context.getSharedPreferences("user_info", Context.MODE_PRIVATE).edit()
editor.putString("username", newName)
editor.putString("address", newAddress)
editor.putInt("age", newAge)
```

可以看到。原生的写法中含有很多的`硬编码的key值`, 这在进行大量使用时，其实是很容易出问题的。

而如果使用组件`EasySharedPreferences`来进行`SharedPreferences`的数据存取。则方便多了：

1. 创建映射实体类

```
@PreferenceRename("user_info")
class User:PreferenceSupport() {
    var username:String
    var age:Int
    var address:String
}
```

2. 进行读取

```
// 直接加载即可
val user = EasySharedPreferences.load(User::class.java)
```

3. 进行修改

```
// 直接使用load出来的user实例进行数值修改
user.age = 16
user.username = "haoge"

// 修改完毕后，apply更新修改到SharedPreferences文件。
user.apply()
```

可以看到。不管是进行`读取数据`。还是`修改数据`。`EasySharedPreferences`的操作方式都是比原生的方式方便很多的。

下面开始对`EasySharedPreferences`组件的用法做更详细的说明：

### 映射实体类的定义

`映射实体类`即是上方示例中的`User`类：通过将SP中需要的关键数据映射到具体的实体类中，可以有效的避免`key值硬编码`的问题。

`映射实体类`的定义，需要遵循以下一些规则：

1. 实体类`必须继承PreferenceSupport`, 且提供`无参构造`。

```
class Entity:PreferenceSupport()
```

2. 默认采用实体类的类名作为`SP的缓存文件名`，当需要指定特殊的缓存文件名时。需要使用`PreferenceRename`注解进行指定

```
@PreferenceRename("rename_shared_name")
class Entity:PreferenceSupport()
```

3. 通过直接在实体类中添加不同的成员变量，进行SP的属性配置：

```
var name:String // 代表此SP文件中。新增key值为name, 类型为String的属性
```

4. 也可以指定属性的key值：同样使用`PreferenceRename`注解进行指定

```
@PreferenceRename("rename_key")
var name:String
```

5. 有时候。我们会需要定义一下中间存储变量(此部分数据不需要同步存储到SP中的)。可以使用`PreferenceIgnore`注解

```
@PreferenceIgnore
val ignore:Address
```

### 支持存储任意数据

都知道，原生的SP只支持几种特定的数据进行存储：`Int`, `Float`, `Boolean`, `Long`, `String`, `Set<String>`.

而`EasySharedPreferences`组件，通过提供`中间类型`的方式。打破了此数据限制:

1. 存储时：将不支持的数据类型，转换为String格式。再进行存储：

**核心源码**

```
// type为接收者类型
// value为从SP中读取出的数据
when {
	type == Int::class.java -> editor.putInt(name, value as? Int?:0)
	type == Long::class.java -> editor.putLong(name, value as? Long?:0L)
	type == Boolean::class.java -> editor.putBoolean(name, value as? Boolean?:false)
	type == Float::class.java -> editor.putFloat(name, value as? Float?:0f)
	type == String::class.java -> editor.putString(name, value as? String?:"")
	// 不支持的类型。统统转换为String进行存储
	type == Byte::class.java
	    || type == Char::class.java
	    || type == Double::class.java
	    || type == Short::class.java
	    || type == StringBuilder::class.java
	    || type == StringBuffer::class.java
	    -> editor.putString(name, value.toString())
	GSON -> value?.let { editor.putString(name, Gson().toJson(it)) }
	FASTJSON -> value?.let { editor.putString(name, JSON.toJSONString(value)) }
}
```

2. 读取时：接收者类型与取出数据格式不匹配(此种场景取出的数据格式均为String)。进行自动转换后再赋值：

**核心源码**

```
// type为接收者类型
// value为从SP中读取出的数据
val result:Any? = when {
    type == Int::class.java -> value as Int
    type == Long::class.java -> value as Long
    type == Boolean::class.java -> value as Boolean
    type == Float::class.java -> value as Float
    type == String::class.java -> value as String
    // 不支持的类型。读取出的都是String，直接进行转换兼容
    type == Byte::class.java -> (value as String).toByte()
    type == Short::class.java -> (value as String).toShort()
    type == Char::class.java -> (value as String).toCharArray()[0]
    type == Double::class.java -> (value as String).toDouble()
    type == StringBuilder::class.java -> StringBuilder(value as String)
    type == StringBuffer::class.java -> StringBuffer(value as String)
    GSON -> Gson().fromJson(value as String, type)
    FASTJSON -> JSON.parseObject(value as String, type)
    else -> null
}
```

有细心的可以看到。这里有对GSON与FASTJSON进行兼容。

`EasySharedPreference`组件。会在运行时判断当前运行环境是否存在具体的JSON解析库。然后选择存在的解析库进行`中间类型数据`的生成器与解析器：而组件本身是没有直接强制依赖此两种解析库的：

```
private val FASTJSON by lazy { return@lazy exist("com.alibaba.fastjson.JSON") }
private val GSON by lazy { return@lazy exist("com.google.gson.Gson") }
```

所以。如果你需要存储一个原生不支持的类型。直接添加即可，比如需要存储一个address_detail:

```
@PerferenceRename("address_detail")
var detail:Address
```

### 缓存加速

在上面的例子中。我们是直接通过`load`方法进行的数据加载读取：

```
val user = EasySharedPreferences.load(User::class.java)
```

这样一行代码，起到的效果即是：

> 1. 加载User类所对应的SharedPreferences文件数据
> 2. 创建User实例，并将SP文件中的数据。注入到User类中的对应变量中去。

所以相对来说。load方法其实是会有一定的耗时。毕竟注入操作都离不开反射，当然，如果你不在同一个SP文件中去`存储大量的数据内容`的话，其实对于现在的机型来说。影响还是可以忽略不计的。

但是毕竟如果每次去读取都去读取注入的话。总归是一种性能影响，也不便于体验。

所以组件提供了对应的缓存控制处理：只在首次加载时进行读取与注入：

```
fun <T> load(clazz: Class<T>):T {
	container[clazz]?.let { return it.entity as T}

	val instance = EasySharedPreferences(clazz)
	container[clazz] = instance
	return instance.entity as T
}
```

所以。**通过同一个clazz加载读取出来的实例，都是同一个实例！**

### 自动同步

因为`缓存加速`的原因，我们通过`load`方法加载出来的实例都是一样的，所以应该会有人担心：当在使用`EasySharedPreferences`组件的同时。如果在别的业务线上，有人对此SP文件`直接使用原生的方式进行了修改`，会不会导致数据出现不同步？即`数据污染`现象？

讲道理。这是不会的！因为`EasySharedPreferences`组件，专门针对此种场景进行了兼容：

#### 原理说明

原生的`SharedPreferences`提供了`OnSharedPreferenceChangeListener`监听器。此监听器的作用为：**对当前的SharedPreferences容器中的数据做监听。当容器中有数据改变了。则通过此接口对外通知。便于进行刷新**

```
public interface OnSharedPreferenceChangeListener {
    void onSharedPreferenceChanged(
    			SharedPreferences sharedPreferences, // 被监听的容器实例
    			String key);// 被修改的数据的key。
}
```

然后，需要指出的是：其实系统本身也有对SharedPreferences容器实例做缓存。所以：**通过同样的文件名获取到的SharedPreferences实例，其实都是同一个对象实例**

所以，同步的流程即是：**只要对组件中自身绑定的`SharedPreferences`容器，注册此监听器，即可在外部进行修改时。同步获取到被修改的key值。再相对的进行指定key的数据同步即可：**

所以，最终的自动同步逻辑核心逻辑代码即是：

```
class EasySharedPreferences(val clazz: Class<*>):SharedPreferences.OnSharedPreferenceChangeListener {

	// 绑定的SharedPreference实例
	private val preferences:SharedPreferences
	init {
		// 创建时，注册内容变动监听器
		preferences.registerOnSharedPreferenceChangeListener(this)
		...
	}

	override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
		// 回调中进行数据同步处理
	}

	fun write() {
		synchronized(this) {
			// 自身的修改需要更新到文件中去时，暂时注销掉监听器。不对自身的数据处理做监听
			preferences.unregisterOnSharedPreferenceChangeListener(this)
			...
			preferences.registerOnSharedPreferenceChangeListener(this)
		}
	}
}
```

### PreferenceIgnore的使用场景

在[映射实体类的定义](https://juejin.im/post/5b34a970f265da59567953a3#heading-4)这一节的最后。我们有提到使用`PreferenceIgnore`注解配置中间存储变量。当时只是简单提了一句，所以可能会有部分朋友对此注解的使用场景存在疑惑

这里我将通过举一个具体的例子进行使用场景说明：

比如说需要存储登录用户的信息，比如登录时的`密码`(当然只是举例，对于密码类型的数据。推荐的存储容器还是使用sql)。我们想把它存储到`SharedPreferences`中去:

```
@PreferenceRename("login_info")
class Login:PreferenceSupport() {
    var password:String
}
```

但是我们又不能直接对密码进行明文存储。所以我们需要在每次进行使用的时候，主动的去再进行`加密`、`解密`：

```
// 读取时进行解密：
var password = EncryptTool.decode(user.password)

// 存储时进行加密：
user.password = EncryptTool.encode(password)
```

但是这样的用法相当不优雅。所以我们推荐使用`PreferenceRename`创建一个中间存储数据出来：

```
@PreferenceRename("login_info")
class Login:PreferenceSupport() {
    // 将实际存储的密码使用private修饰，避免外部直接修改
    private var password:String
    @PreferenceIgnore
    var passwordWithEncrypt:String
        get() { return EncryptTool.decode(password) }
        set(value:String) { this.password = EncryptTool.encode(value)}
}
```

通过配置一个中间的存储变量，自动去进行存取时的加解密操作。对上层隐藏具体的加解密逻辑。这样上层使用起来就相当优雅了：

```
// 读取
var password = user.passwordWithEncrypty

// 存储
user.passwordWithEncrypty = password
```

### 混淆配置

最后，为了避免混淆后导致使用异常，请添加以下混淆配置：

```
-keep class * implements com.haoge.easyandroid.easy.PreferenceSupport { *; }
```