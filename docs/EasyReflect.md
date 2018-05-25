# EasyReflect

EasyReflect对常规的反射操作进行封装。达到更便于使用反射的效果

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyReflectActivity.kt)

## 特性

- 链式调用
- api友好，逻辑清晰

## 用法

### 使用EasyReflect

EasyReflect操作类包装了两部分数据：

```
// 1. clazz:类的Class数据。总是存在
// 2. instance:具体的对象实例。可能不存在。当进行的操作会需要使用此数据时。会自动匹配默认构造器进行创建
class EasyReflect private constructor(val clazz: Class<*>, var instance:Any?)
```

#### 创建EasyReflect实例的几种方式：

1. 通过指定class创建实例。此种方式创建数据**不存在**具体的instance实例

```
var reflect = EasyReflect.create(clazz:Class<*>)
```

2. 通过具体数据对象创建实例：此种方式创建数据**存在**具体的instance实例

```
var reflect = EasyReflect.create(any:Any)
```

3. 通过完整的对象名与ClassLoader创建实例：此种方式创建数据**不存在**具体的instance实例

```
var reflect = EasyReflect.create(name:String, loader:ClassLoader?)
```

4. 通过具体的构造器进行实例创建：此种方式创建数据**存在**具体的instance实例

```
// 可变参数args需要与具体的构造器匹配
var reflect = EasyReflect.create(clazz).instance(varage args:Any?)
```

#### 对字段进行取值/赋值

```
var reflect = ...
// 取值
reflect.getFieldValue(name:String)
// 赋值
reflect.setField(name:String, value:Any?)
```

#### 对方法进行调用

```
var reflect = ...
// 仅调用:name为方法名。args为所需参数数据
reflect.call(name:String, varage args:Any?)
// 调用并使用方法返回值构建新的EasyReflect实例并返回.
var newReflect = reflect.callWithReturn(name:String, varage args:Any?)
```

### 使用ConstructorReflect操作构造器

```
// 此实例同样包裹两部分数据：
// 1. constructor:此实例操作所使用的构造器
// 2. 上级对象。
class ConstructorReflect(val constructor: Constructor<*>, val upper:EasyReflect)
```

#### 获取实例

```
// 仍然是首先创建EasyReflect实例
val reflect = ...
// 1. 读取此类所有的构造器：
val list:List<ConstructorReflect> = reflect.getConsturctors()
// 2. 根据参数类型匹配指定的构造器
val constructor:ConstructorReflect = reflect.getConstructor(varage types:Class<*>)
```

#### 使用

```
val constructorReflect = ...
// 使用指定构造器创建新的EasyReflect实例
val newReflect:EasyReflect = constructorReflect.newInstance(varage args:Any?)
```

### 使用FieldReflect操作变量

```
class FieldReflect(val field:Field, val upper:EasyReflect)
```

#### 获取实例

```
// 仍然是首先创建EasyReflect实例
val reflect = ...
// 1. 读取此类所有的成员变量：
val fields:List<FieldReflect> = reflect.getFields()
// 2. 根据参数类型匹配指定的构造器
val field:FieldReflect = reflect.getField(name:String)
```

#### 使用

```
val fieldReflect = ...

// 1. 获取此变量的具体值
val value = fieldReflect.getValue()
// 2. 为此变量设置值
fieldReflect.setValue(value:Any?)
// 使用此变量的数据创建新的EasyReflect实例提供使用
val newReflect:EasyReflect = fieldReflect.transform()
```

### 使用MethodReflect

```
class MethodReflect(val method:Method, val upper:EasyReflect)
```

#### 获取实例

```
// 仍然是首先创建EasyReflect实例
val reflect = ...
// 1. 读取此类所有的方法
val methods:List<MethodReflect> = reflect.getMethods()
// 2. 根据指定方法名name与参数类型types匹配对应的方法
val method:MethodReflect = reflect.getMethod(name:Stirng, varage types:Class<*>)
```

#### 使用

```
val methodReflect = ..

// 1. 执行此方法：
methodReflect.call(varage args:Any?)
// 2. 执行此方法。并将返回值作为数据。创建出新的EasyReflect实例返回
val newReflect = methodReflect.callWithReturn(varage args:Any?)
```