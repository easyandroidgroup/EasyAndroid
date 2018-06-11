# EasyReflect

EasyReflect对常规的反射操作进行封装。让使用反射操作变得简单。

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyReflectActivity.kt)

## 用法

### 1. 初识EasyReflect

```
class EasyReflect private constructor(val clazz: Class<*>, var instance:Any?)
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
// 通过默认空构造器进行对象创建
val instance1 = EasyReflect.instance().instance
// 通过与可变参数类型匹配的构造器进行对象创建
val instance2 = EasyReflect.instance(arg0, arg1...argN).instance
```

### 4. 字段的赋值与取值

EasyReflect对字段的操作，不用再去考虑它是否是`静态的`或者还是`final修饰的`。更不用去操心字段是否是`private`不可见的。我们只需要指定字段名即可。这大大增强了便利性！

- 访问指定字段的值：

```
val value = reflect.getFieldValue(fieldName)
```

- 为指定字段赋值：

```
reflect.setField(fieldName, newValue)
```

- 获取指定字段的EasyReflect实例进行使用

```
val newReflect = reflect.getField(fieldName).transform()
```

### 5. 方法调用

与字段操作类似，我们也不用去担心方法的可见性问题。需要的只有`此方法存在`即可

- 调用指定方法

```
// 调用指定方法，不含参数
reflect.call(methodName)
// 调用指定方法，含指定参数
reflect.call(methodName, arg0, arg1...argN)
```

- 调用指定方法并获取返回值

```
val value = reflect.callWithReturn(methodName, arg0, arg1...argN).instance
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

最后，贴上proxy方法代码，希望能对理解托管代理方法的执行原理起到一定的帮助：

```
fun <T> proxy(proxy:Class<T>):T {
    @Suppress("UNCHECKED_CAST")
    return Proxy.newProxyInstance(proxy.classLoader, arrayOf(proxy), {_, method, args ->
        try {
            // 优先匹配存在的方法
            return@newProxyInstance this@EasyReflect.callWithReturn(method.name, *args).get()
        } catch (e:Exception) {
            // 不能匹配到已存在的方法，则匹配set/get方法进行字段托管
            try {
                val methodName = method.name
                if (methodName == "get" && args.size == 1 && args[0] is String) {
                    return@newProxyInstance getFieldValue(args[0] as String)
                } else if (methodName == "set" && args.size == 2 && args[0] is String) {
                    setField(args[0] as String, args[1])
                } else if (methodName.startsWith("get") && method.returnType != Void::class.java) {
                    val name = methodName.substring(3,4).toLowerCase() + methodName.substring(4)
                    return@newProxyInstance getFieldValue(name)
                } else if (methodName.startsWith("set") && args.size == 1) {
                    val name = methodName.substring(3,4).toLowerCase() + methodName.substring(4)
                    setField(name, args[0])
                }
            } catch (e:Exception) {
                // ignore
            }
            // 最后，兼容不存在的方法，提供默认值的返回值。
            return@newProxyInstance when (method.returnType.name) {
                "int", "byte", "char", "long", "double", "float", "short" -> 0
                "boolean" -> false
                else -> method.defaultValue
            }
        }
    }) as T
}
```