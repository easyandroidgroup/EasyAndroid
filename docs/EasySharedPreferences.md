# EasySharedPreferences

EasySharedPreferences对`SharedPreferences`的操作进行封装，简化存取操作

其做法为：将SharedPreferences的数据映射到指定的实体类中去。避免到处去指定key。进行硬编码存储

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasySharedPreferencesActivity.kt)

## 特性

1. 通过具体的实体类进行SP数据存储操作。避免`key值硬编码`
2. 自动同步，即使别的地方是`直接使用SharedPreferences进行赋值`，也能自动同步相关数据。
3. 打破SharedPreferences限制。支持几乎任意类型数据存取

## 用法

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

### 实体类的定义说明

在上面的示例中。我们已经定义了一个对应的映射实体类了：

```
@PreferenceRename("user_info")
class User:PreferenceSupport() {
    var username:String
    var age:Int
    var address:String
}
```

下方的配置说明，可结合此具体的映射实体类进行理解：

1. 映射实体类，必须继承自`PreferenceSupport`类。且提供`无参构造器`。

```
class User:PreferenceSupport()
```

2. 当需要指定使用的SP的文件名时。使用`PreferenceRename`注解进行指定。否则将使用类名作为文件名：

比如这里需要使用的SP文件名为user_info:

```
@PreferenceRename("user_info")
class User:PreferenceSupport()
```

3. 通过直接在实体类中添加不同的成员变量，进行SP的属性配置：

```
var name:String // 代表此SP文件中。新增key值为name, 类型为String的属性
```

4. 也可以指定属性的key值：同样使用`PreferenceRename`注解

```
@PreferenceRename("rename_key")
var name:String
```

5. 有时候。我们会需要定义一下中间存储变量(此部分数据不需要同步存储到SP中的)。可以使用`PreferenceIgnore`注解

```
@PreferenceIgnore
val ignore:Address
```

### 打破存储类型限制

我们都知道。原生的`SharedPreferences`只支持很少量的数据类型进行存储：`Int`, `Float`, `Boolean`, `Long`, `String`,`Set<String>`

而有时候我们会需要存储一些其他类型的数据进行缓存。比如`Array`,`List`,`Bean`对象。这个时候`SharedPreferences`的存储功能就捉襟见肘了。

所以，这时就会需要：不然`重选存储方式(数据库存储)`， 不然`将数据转为SP支持的数据格式`来进行存储。

`EasySharedPreferences`组件即是采用的`第二种方式`来进行的存储：

所以。当我们需要指定存储的其他类型数据时。直接添加即可：(比如存储一个列表数据)

```
var list:List<String>
```

对此数据进行存储时。将会自动将其转换为`JSON`数据再进行存储；同样在进行读取时，也会进行`JSON反序列化`后再进行赋值。

### 缓存加速

在上面的例子中可以看到。加载`SharedPreferences`数据并读取到实体类中去。只需要调用一行代码即可：

```
// 直接加载即可
val user = EasySharedPreferences.load(User::class.java)
```

看到这里的时候。肯定会有很多人担心使用时的性能问题。所以我先贴一个`load`的源码进行说明：

```
fun <T> load(clazz: Class<T>):T {
    synchronized(container) {
        container[clazz]?.let { return it.entity as T}

        val instance = EasySharedPreferences(clazz)
        container[clazz] = instance
        return instance.entity as T
    }
}
```

可以看到：只有当`第一次使用此clazz`进行加载时。才会走加载流程。后面的都是直接读取的缓存。所以请放心使用

### 自动同步

`EasySharedPreferences`组件，其本质是对`SharedPreferences`的存取操作进行封装。

但是很难避免的是：会有部分朋友在写的时候，还是在上层使用`SharedPreferences`直接数据存储。

而在上面的也展示了。其实我们在load的时候并没有每次都去重新加载。而是读取的`已存在的缓存`。

`EasySharedPreferences`组件则对此场景做了兼容。并不会导致数据不同步的问题。

#### 自动同步原理说明

首先需要了解的一点是。在系统层面，同一个文件名的`SharedPreferences`实例。其实都是一样的。因为系统本身就是对SP进行了缓存处理：

所以我们就可以直接使用`SharedPreferences`本身提供的`OnSharedPreferenceChangeListener`去进行数据改变时的监听操作：

```
public interface SharedPreferences {
    // 提供的数据改变时的监听器。当此SharedPreferences对应的某个属性被改变时。将会被触发进行回调
    public interface OnSharedPreferenceChangeListener {

        void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key);
    }
    // 注册监听器。
    void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener);

    void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener);
```

所以。自动同步其实很简单：直接接入此监听器。同步回调中指定的key的数据即可

