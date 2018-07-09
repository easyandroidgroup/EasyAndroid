# EasyReflect

EasyReflect对常规的反射操作进行封装。让使用反射操作变得简单。

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyReflectActivity.kt)

## 用法

### 1. 初识EasyReflect

```
class EasyReflect private constructor(val clazz: Class<*>, private var instance:Any?)
```

可以看到：EasyReflect本身持有两部分数据：`clazz`与`instance`.

- clazz: 此实例所绑定操作的clazz实例。永不为null
- instance: 此实例所绑定的instance实例，为clazz类型的具体实例。可能为null。

**请注意：对于instance数据来说，当执行操作过程中需要使用此instance实例时(比如读取某个成员变量)，若此时instance为null，则将触发使用默认的空构造器进行instance创建。**

### 2. 创建EasyReflect的几种姿势

1. **只使用Class进行创建**：创建后只含有clazz数据

```
val reflect = EasyReflect.create(Test::class.java)
```

2. **使用类全名进行创建(可指定ClassLoader)**：创建后只含有clazz数据

```
val reflect = EasyReflect.create("com.haoge.sample.Test")
// 也可指定ClassLoader进行加载
val reflect = EasyReflect.create(className, classLoader)
```

3. **直接通过指定实例进行创建**：创建后clazz与instance均存在, clazz为instance.javaClass数据

```
val reflect = EasyReflect.create(any)
```

了解了EasyReflect的几种创建方式与其区别后。我们就可以正式进行使用了

### 3. 调用指定构造器进行实例创建

```
val reflect:EasyReflect = createReflect()
// 通过默认空构造器进行对象创建. 通过get方法获取创建出的对象
val instance = reflect.instance().get<Any>()
// 通过含参构造器进行对象创建
val instance2 = reflect.instance(arg0, arg1...argN).get<Any>()
```

### 4. 字段的赋值与取值

EasyReflect对字段的操作，不用再去考虑它是否是`静态的`或者还是`final修饰的`。更不用去管字段是否是`private`不可见的。我们只需要指定字段名即可。这大大增强了便利性！

- 访问指定字段的值：

```
val value:Any = reflect.getFieldValue<Any>(fieldName)
```

- 为指定字段赋值：

```
reflect.setField(fieldName, newValue)
```

- 使用指定字段的值创建新的`EasyReflect`实例进行使用

```
val newReflect = reflect.transform(fieldName)
```

### 5. 方法调用

与字段操作类似，我们也不用去担心方法的可见性问题。需要的只有`此方法存在`即可

- 调用指定方法

```
// 调用无参方法
reflect.call(methodName)
// 调用有参方法
reflect.call(methodName, arg0, arg1...argN)
```

- 调用指定方法并获取返回值

```
val value = reflect.callWithReturn(methodName, arg0, arg1...argN).get()
```

### 6. 传参包括可变参数时的调用方式

可变参数会在编译时。替换为数组的形式。所以使用反射进行可变参数的传参时，需要使用数组的形式对参数进行构建：

以下方的两个方法为例：

```
class Method{
	fun onlyVararg(vararg names:String)
	fun withVararg(preffix:Int, vararg names:String)
}
```

此类中的两个方法均为带有可变参数的方法，在反射环境下。就应该将其中的可变参数看作为是对应的数组进行使用，所以这个时候。我们需要使用如下方式进行参数传递：

```
// 1. 调用只含有可变参数的方法
reflect.call("onlyVararg", arrayOf("Hello", "World"))
// 2. 调用包含其他参数的可变参数方法
reflect.call("withVararg", 1, arrayOf("Hello", "World"))
```

在这里要特别提醒一下：由于java与kotlin的差异性，在java中调用**只含有可变参数**的方法时。需要对参数进行特殊处理(外层包裹一层`Object[]`)：

```
EasyReflect reflect = EasyReflect.create(Method.class)
reflect.call("onlyVararg", new Object[]{new String[]{"Hello", "World"}})
```

而对于`withVararg`这种带有非可变参数的方法则不用进行此特殊处理：

```
reflect.call("withVararg", 1, new String[]{"Hello", "World"})
```

这是因为在java中。若实参为一个数组时。这个时候将会对此数组进行解构平铺，所以我们需要在外层再单独套一层Object数组。才能对参数类型做到正确匹配。而在kotlin中不存在此类问题。

### 7. 使用动态代理进行托管

当你需要对某个类进行访问，但是又不想通过写死名字的方式去调用时，可以使用此特性：

**通过动态代理的方式，创建一个代理类来对反射操作进行托管**

还是通过举例来说明：

```
class Test {
	private fun function0(name:String)
}
```

对于上面所举例的类来说。要通过反射调用它的function0方法，那么需要这样写：

```
val reflect = EasyReflect.create(Test::class.java)
reflect.call("function0", "This is name")
```

而若使用托管的方式。配置先创建代理接口，然后通过代理接口进行方法访问：

```
// 创建代理接口
interface ITestProxy {
	fun function0(name:String)
}

val reflect = EasyReflect.create(Test::class.java)
// 创建托管代理实例：
val proxy:ITestProxy = reflect.proxy(ITestProxy::class.java)
// 然后直接通过代理类进行操作:
proxy.function0("proxy name")
```

当然，如果只能对方法进行托管那这功能就有点太弱逼了。托管方案也同时支持了对变量的操作：

```
class Test {
	// 添加一个字段
	private val user:User
	private fun function0(name:String)
}

// 创建代理接口
interface ITestProxy {
	fun function0(name:String)

	//==== 对user字段进行赋值
	// 方式一：使用setXXX方法进行赋值
	fun setUser(user:User)
	// 方式二：使用set(name, value)方法进行赋值
	fun set(fieldName:String, value:String)

	//==== 对user进行取值
	// 方式一：使用getXXX方法进行取值
	fun getUser()
	// 方式二：使用get(name)方法进行取值
	fun get(fieldName:String)
}

val reflect = EasyReflect.create(Test::class.java)
// 创建托管代理实例：
val proxy:ITestProxy = reflect.proxy(ITestProxy::class.java)
// 然后直接通过代理类进行操作:
proxy.setUser(user)// 方式一赋值
proxy.set("user", user)// 方式二赋值

proxy.getUser()	// 方式一取值
proxy.get("user")// 方式二取值
```

从某种程度上来说：代理托管方案会比一般的直接反射操作要繁琐一点。但是带来的便利也是显而易见的：

首当其冲的优点就是：托管方式写的代码比直接进行反射操作更加优雅~

其次，在某些使用环境下，比如在团队协作中：可以将对外不方便直接提供实例的通过托管代理实例进行提供。

## 更多示例

为了方便大家更好的理解，这一节中我会列举一些小案例，方便大家更清楚的知道应该怎么去使用：

### 1. 使用指定构造器创建实例

假设我们有以下一个类需要创建：

```
data class Example private constructor(val name: String)
```

那么我们可以这样来进行创建：

```
val example = EasyReflect.create(Example::class.java)// 指定实体类的class
	.instance("Hello World")// 根据对应的构造器的入参类型进行传参
	.get<Example>() // 获取创建好的实体类实例
```

### 2. 对类中指定成员变量进行取值、赋值

假设我们需要对此类中name变量进行操作

```
class Example {
	private name:String? = null
}
```

因为是成员变量的操作。而成员变量是属于`具体实例`的。所以首先应该需要获取到具体实例进行操作：

```
val example = Example()
val reflect = EasyReflect.create(example)
```

获取到实例后，即可直接操作了：

**赋值**

```
reflect.setField("name"/*变量名*/, "Hello"/*赋的值*/)
```

**取值**

```
val name = reflect.getFieldValue<String>("name")
```

### 3. 对类中的静态变量进行取值、赋值

```
class Example {
    companion object {
        @JvmStatic
        val staticName:String? = null
    }
}
```

因为静态变量是隶属于类本身的，所以与成员变量的访问不同：静态变量的访问只需要使用类本身即可：

```
val reflect = EasyReflect.create(Example::class.java)
```

当然，你也可以跟成员变量的操作一样的，直接提供具体的类实例进行使用, 都是一样的：

```
val reflect = EasyReflect.create(example)
```

**赋值**

```
reflect.setField("staticName", "Haoge")
```

**取值**

```
val staticName:String = reflect.getFieldValue<String>("staticName")
```

### 4. 访问成员方法

```
class Example {
	private fun transform(name:String):User {...}
}
```

与`成员变量`类似，`成员方法`也是隶属于具体的类实例的，所以创建时需要提供具体的类实例

```
val reflect = EasyReflect.create(Example())
```

然后，`EasyReflect`中，对方法提供两种操作：

**1. 直接访问指定的方法**

```
reflect.call(
	"transform"/*第一个参数:方法名*/,
	"Haoge"	/*可变参数，需要与具体方法的传参类型一致*/
	)
```

**2. 访问指定方法，并使用返回值创建新的Reflect实例进行返回**

```
val userReflect = reflect.callWithReturn(
	"transform"/*第一个参数:方法名*/,
	"Haoge"	/*可变参数，需要与具体方法的传参类型一致*/
	)

// 获取到返回的user实例
val user:User = userReflect.get<User>()
// 也可以根据返回的userReflect继续对User类进行操作
...
```

### 5. 访问静态方法：

`静态方法`也是隶属于`类本身`的，所以说：与`静态变量的访问`类似，只需要指定操作的类即可直接访问了:

```
val reflect = EasyReflect.create(Example::class.java)
```

其他具体的操作方式与成员方法的操作均一致。

### 6. 使用指定Field的值创建新的EasyReflect实例提供使用

```
class Example {
	val user:InternalUser
}

private class InternalUser {
	val name:String
}
```

类似上方代码：我们需要获取变量`user`中的`name`数据，但是user的类是私有的，外部不能访问。

所以，按照上面我们提供的方式。我们需要这样来做一次中转后再使用：

```
val reflect = EasyReflect.create(example)
// 因为外部不能访问到InternalUser,所以用顶层类进行接收
val user:Any = reflect.getFieldValue<Any>("user")
// 再使用具体的user实例创建出新的操作类
val userReflect = EasyReflect.create(user)
// 最后再读取InternalUser中的name字段：
val name:String = userReflect.getFieldValue<String>("name")
```

可以看到这种方式还是比较繁琐的，所以`EasyReflect`专门针对此种操作，提供了`transform`方法，最终你可以使用下方的代码做到与上面同样的效果：

```
val name:String = EasyReflect.create(example)
	.transform("user")// 使用user变量的数据生成新的reflect实例
	.get<String>("name")// 读取InternalUser中的name数据。
```

### 7. 使用动态代理进行托管管理

所谓的动态代理托管，即是通过`动态代理`的方式，将一个`代理接口`与`实体类`进行绑定，使得可以通过`操作绑定的代理类`做到`操作对应实体类`的效果

#### 1. 映射代理方法

```
class Example {
	fun function() {}
}
```

比如这里我们需要使用`动态代理托管`的方式访问此`function`方法，那么首先，需要先创建一个`代理接口`

```
interface Proxy {
	// 与Example.function方法对应，需要同名同参数才能正确映射匹配。
	fun function();
}
```

现在我们将此`代理接口`与对应的`被代理实例example`进行绑定：

```
val proxy:Proxy = EasyReflect.create(example).proxy(Proxy::class.java)
```

绑定后即可通过自动创建的`代理实例proxy`直接进行操作了：

```
proxy.function() ==等价于==> example.function()
```

#### 2. 映射代理变量

当然，也可以对`被代理实例`中的变量进行代理，比如：

```
class Example {
	val user:User = User()
}
```

当我们需要对此类中的`user`变量进行取值时。可以创建以下几种的`代理方法`

- 第一种：方法名为`get`,且参数`有且只有一个`,类型为String的：

```
fun get(name:String):Any?
```

- 第二种：创建`getter`方法：

```
fun getUser():User? {}
```

此两种`代理方法`都是等价的，所以通过代理接口进行操作时。他们的效果都是一致的：

```
proxy.get("user")		==等价于==>	example.user
proxy.getUser()			==等价于==>	example.user
```

同样的，当我们需要对类中变量进行赋值时，也提供两种`代理方法创建方式`

- 第一种：方法名为`set`,且`有钱仅有两个`参数。第一个参数类型为`String`：

```
fun set(name:String, value:Any)
```

- 第二种：`setter`方法：

```
fun setUser(user:User)
```

所以，使用此赋值方法的效果为：

```
proxy.set("user", newUser)		==等价于==> example.user = newUser
proxy.setUser(newUser)			==等价于==> example.user = newUser
```

希望这些示例能让大家在使用时有所帮助~
