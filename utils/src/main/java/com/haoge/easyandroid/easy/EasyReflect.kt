package com.haoge.easyandroid.easy

import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

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
        try {
            val constructor = getConstructor(*args)
            return create(constructor.newInstance(*args))
        } catch (e:Exception) {
            throw ReflectException(e)
        }
    }

    /**
     * 根据传入的参数类型匹配对应的构造器
     */
    fun getConstructor(vararg args:Any?):Constructor<*> {
        val types = types(*args)
        val constructor = clazz.getDeclaredConstructor(*types)
        accessible(constructor)
        return constructor
    }

    // 成员变量操作区

    /**
     * 读取指定name的成员变量。并为此成员变量创建新的EasyReflect实例并返回
     */
    fun field(name: String):EasyReflect {
        checkInstance()
        val field = getField(name)
        val value = field.get(instance)
        return create(value?:field.type)
    }

    /**
     * 为指定name的成员变量赋值为value
     */
    fun setField(name: String, value:Any?) {
        checkInstance()
        val field = getField(name)
        field.set(instance, value)
    }

    /**
     * 根据指定name获取对应的Field
     */
    fun getField(name:String):Field {
        val field = clazz.getDeclaredField(name)
        accessible(field)
        return field
    }

    // 普通方法操作区

    /**
     * 执行指定name的方法。并返回自身的EasyReflect实例
     */
    fun call(name: String, vararg args:Any?):EasyReflect{
        checkInstance()
        getMethod(name, *args).invoke(instance, *args)
        return this
    }

    /**
     * 执行指定name的方法, 并将方法的返回值作为新数据。创建出对应的EasyReflect实例并返回
     *
     * **请注意：指定name的方法必须含有有效的返回值。**
     */
    fun method(name:String, vararg args:Any?):EasyReflect {
        checkInstance()
        val method = getMethod(name, *args)
        if (method.returnType.name == "void") {
            throw ReflectException("Method ${clazz.canonicalName}.$name not provide a valid return type" )
        }
        return create(method.invoke(instance, *args))
    }

    /**
     * 获取与此name与参数想匹配的Method实例
     */
    fun getMethod(name: String, vararg args:Any?):Method {
        val types = types(*args)
        val method = clazz.getDeclaredMethod(name, *types)
        accessible(method)
        return method
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

    // 检查是否存在有效的可操作实例。若不存在则抛出异常。
    private fun checkInstance() {
        if (instance != null) {
            return
        }

        try {
            instance = getConstructor().newInstance()
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
        fun <T:AccessibleObject> accessible(accessible:T) {
            if (!accessible.isAccessible) {
                accessible.isAccessible = true
            }
        }
    }


}

/**
 * 用于在进行反射操作过程中。对受检异常错误进行包裹。
 */
class ReflectException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
