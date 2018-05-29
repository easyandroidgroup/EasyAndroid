package com.haoge.easyandroid.easy

import java.lang.reflect.*

/**
 * 一个简单的反射封装库。
 * @author haoge on 2018/5/21
 */
class EasyReflect private constructor(val clazz: Class<*>, var instance:Any?){

    // 构造方法操作区
    /**
     * 使用匹配参数的构造函数创建一个对象实例，并生成新的EasyReflect实例返回
     */
    fun instance(vararg args:Any?):EasyReflect {
        return getConstructor(*types(*args)).newInstance(*args)
    }

    /**
     * 根据传入的参数类型匹配对应的构造器
     */
    fun getConstructor(vararg types:Class<*>):ConstructorReflect {
        return ConstructorReflect(accessible(clazz.getDeclaredConstructor(*types)), this)
    }

    fun getConstructors():List<ConstructorReflect> {
        val list = mutableListOf<ConstructorReflect>()
        for (constructor in clazz.declaredConstructors) {
            list.add(ConstructorReflect(accessible(constructor), this))
        }
        return list
    }

    // 成员变量操作区
    /**
     * 为指定name的成员变量赋值为value
     */
    fun setField(name: String, value:Any?):EasyReflect {
        getField(name).setValue(value)
        return this
    }

    fun <T> getFieldValue(name: String):T?{
        return getField(name).getValue()
    }

    /**
     * 根据指定name获取对应的FieldReflect
     */
    fun getField(name:String):FieldReflect {
        var type:Class<*>? = clazz

        val field = try {
            accessible(type!!.getField(name))
        } catch (e:NoSuchFieldException){
            var find:Field? = null
            do {
                try {
                    find = accessible(type!!.getDeclaredField(name))
                    if (find != null) {
                        break
                    }
                } catch (ignore:NoSuchFieldException) { }
                type = type!!.superclass
            } while (type != null)

            find?: throw ReflectException(e)
        }
        return FieldReflect(field, this)
    }

    fun getFields():List<FieldReflect> {
        val list = mutableListOf<FieldReflect>()
        var type:Class<*>? = clazz
        do {
            for (field in type!!.declaredFields) {
                list.add(FieldReflect(accessible(field), this))
            }
            type = type.superclass
        } while (type != null)
        return list
    }

    // 普通方法操作区
    /**
     * 执行指定name的方法。并返回自身的EasyReflect实例
     */
    fun call(name: String, vararg args:Any?):EasyReflect{
        getMethod(name, *types(*args)).call(*args)
        return this
    }

    /**
     * 执行指定name的方法, 并将方法的返回值作为新数据。创建出对应的EasyReflect实例并返回
     *
     * **请注意：指定name的方法必须含有有效的返回值。**
     */
    fun callWithReturn(name:String, vararg args:Any?):EasyReflect {
        return getMethod(name, *types(*args)).callWithReturn(*args)
    }

    /**
     * 获取与此name与参数想匹配的Method实例
     */
    fun getMethod(name: String, vararg types:Class<*>):MethodReflect {
        var type:Class<*>? = clazz
        val method = try {
            accessible(type!!.getDeclaredMethod(name, *types))
        } catch (e:NoSuchFieldException){
            do {
                try {
                    accessible(type!!.getDeclaredMethod(name, *types))
                } catch (ignore:NoSuchMethodException) {}
                type = type!!.superclass
            } while (type != null)
            throw ReflectException(e)
        }
        return MethodReflect(method, this)
    }

    fun getMethods():List<MethodReflect> {
        val list = mutableListOf<MethodReflect>()
        var type:Class<*>? = clazz
        do {
            for (method in type!!.declaredMethods) {
                list.add(MethodReflect(accessible(method), this))
            }
            type = type.superclass
        } while (type != null)
        return list
    }

    /**
     * 获取与此EasyReflect相绑定的实例。
     */
    fun <T> get():T? {
        return try {
            @Suppress("UNCHECKED_CAST")
            instance as T?
        } catch (e:ClassCastException) {
            null
        }
    }

    fun <T> proxy(proxy:Class<T>):T {
        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(proxy.classLoader, arrayOf(proxy), {proxy, method, args ->
            try {
                return@newProxyInstance this@EasyReflect.call(method.name, *args)
            } catch (e:Exception) {
                return@newProxyInstance when (method.returnType.name) {
                    "int", "byte", "char", "long", "double", "float", "short" -> 0
                    "boolean" -> false
                    else -> method.defaultValue
                }
            }
        }) as T
    }

    // 检查是否存在有效的可操作实例。若不存在则抛出异常。
    private fun checkInstance() {
        if (instance != null) {
            return
        }

        try {
            instance = getConstructor().constructor.newInstance()
        } catch (e:Exception) {
            throw ReflectException("Could not fount default constructor for [${clazz.canonicalName}] to create instance")
        }
    }

    override fun toString(): String {
        return "EasyReflect(clazz=$clazz, instance=$instance)"
    }

    companion object {
        fun create(clazz: Class<*>, any:Any? = null):EasyReflect {
            return EasyReflect(clazz, any)
        }

        fun create(any:Any):EasyReflect {
            return create(any.javaClass, any)
        }

        fun create(name:String, loader:ClassLoader? = null): EasyReflect {
            return try {
                if (loader == null) {
                    create(Class.forName(name))
                } else {
                    create(Class.forName(name, true, loader))
                }
            } catch (e:Exception) {
                throw ReflectException(e)
            }
        }

        @JvmStatic
        fun types(vararg args:Any?):Array<Class<*>> {
            if (args.isEmpty()) {
                return arrayOf()
            }
            return Array(args.size, { index -> args[index]?.javaClass ?: Void::class.java})
        }

        @JvmStatic
        fun <T:AccessibleObject> accessible(accessible:T):T {
            if (!accessible.isAccessible) {
                accessible.isAccessible = true
            }
            return accessible
        }
    }

    class ConstructorReflect(val constructor: Constructor<*>, val upper:EasyReflect) {
        fun newInstance(vararg args:Any?):EasyReflect {
            return create(constructor.newInstance(*args))
        }
    }

    // 成员方法反射操作类
    class MethodReflect(val method:Method, val upper:EasyReflect) {
        val isStatic = Modifier.isStatic(method.modifiers)

        fun call(vararg args:Any?):MethodReflect {
            if (isStatic) {
                method.invoke(upper.clazz, *args)
            } else {
                upper.checkInstance()
                method.invoke(upper.instance, *args)
            }
            return this
        }

        fun callWithReturn(vararg args:Any?):EasyReflect {
            val value = if (isStatic) {
                method.invoke(upper.clazz, *args)
            } else {
                upper.checkInstance()
                method.invoke(upper.instance, *args)
            }

            return create(value?:method.returnType)
        }
    }

    // 成员变量反射操作类
    class FieldReflect(val field:Field, val upper:EasyReflect){
        val isStatic = Modifier.isStatic(field.modifiers)

        @Suppress("UNCHECKED_CAST")
        fun <T> getValue():T? {
            return try {
                if (isStatic) {
                    field.get(upper.clazz) as T
                } else {
                    upper.checkInstance()
                    field.get(upper.instance) as T
                }
            } catch (e:Exception) {
                null
            }
        }

        fun setValue(value: Any?):FieldReflect {
            if (isStatic) {
                field.set(upper.clazz, value)
            } else {
                upper.checkInstance()
                field.set(upper.instance, value)
            }
            return this
        }

        fun transform():EasyReflect {
            val value = if (isStatic) {
                field.get(upper.clazz)
            } else {
                upper.checkInstance()
                field.get(upper.instance)
            }
            return create(value?:field.type)
        }
    }
}

/**
 * 用于在进行反射操作过程中。对受检异常错误进行包裹。
 */
class ReflectException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}
