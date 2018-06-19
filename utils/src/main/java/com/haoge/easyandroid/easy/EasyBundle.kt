package com.haoge.easyandroid.easy

import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.google.gson.Gson
import java.io.Serializable
import java.lang.StringBuilder
import java.lang.reflect.Field

/**
 * 用于方便的进行Bundle数据存取
 * @author haoge on 2018/6/14
 */
class EasyBundle private constructor(val bundle: Bundle){

    fun put(map:Map<String, Any?>):EasyBundle {
        for ((key, value) in map) {
            put(key, value)
        }
        return this
    }

    fun put(vararg items:Pair<String, Any?>):EasyBundle {
        for ((name, value) in items) {
            put(name, value)
        }
        return this
    }

    fun put(name:String, value:Any?):EasyBundle {
        if (TextUtils.isEmpty(name) || value == null) {
            return this
        }

        // 根据value的类型，选择合适的api进行存储
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is Int -> bundle.putInt(name, value)
            is Long -> bundle.putLong(name, value)
            is CharSequence -> bundle.putCharSequence(name, value)
            is String -> bundle.putString(name, value)
            is Float -> bundle.putFloat(name, value)
            is Double -> bundle.putDouble(name, value)
            is Char -> bundle.putChar(name, value)
            is Short -> bundle.putShort(name, value)
            is Boolean -> bundle.putBoolean(name, value)
            is Serializable -> bundle.putSerializable(name, value)
            is Bundle -> bundle.putBundle(name, value)
            is Parcelable -> bundle.putParcelable(name, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> bundle.putCharSequenceArray(name, value as Array<out CharSequence>)
                value.isArrayOf<String>() -> bundle.putStringArray(name, value as Array<out String>?)
                value.isArrayOf<Parcelable>() -> bundle.putParcelableArray(name, value as Array<out Parcelable>?)
                else -> bundle.putString(name, toJSON(value))
            }
            is IntArray -> bundle.putIntArray(name, value)
            is LongArray -> bundle.putLongArray(name, value)
            is FloatArray -> bundle.putFloatArray(name, value)
            is DoubleArray -> bundle.putDoubleArray(name, value)
            is CharArray -> bundle.putCharArray(name, value)
            is ShortArray -> bundle.putShortArray(name, value)
            is BooleanArray -> bundle.putBooleanArray(name, value)
            else -> bundle.putString(name, toJSON(value))
        }

        return this
    }

    inline fun <reified T> get(key:String):T? {
        return get(key, T::class.java)
    }

    @Suppress("UNCHECKED_CAST")
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

        value = value as String
        if (value.isEmpty()) {
            // 过滤空数据
            returnsValue(null, type)
        }

        // 处理两种情况下的数据自动转换：
        val result = when(type.canonicalName) {
            "byte", "java.lang.Byte" -> value.toByte()
            "short", "java.lang.Short" -> value.toShort()
            "int", "java.lang.Integer" -> value.toInt()
            "long", "java.lang.Long" -> value.toLong()
            "float", "java.lang.Float" -> value.toFloat()
            "double", "java.lang.Double" -> value.toDouble()
            "char", "java.lang.Character" -> value.toCharArray()[0]
            "boolean", "java.lang.Boolean" -> value.toBoolean()
            "java.lang.StringBuilder" -> StringBuilder(value)
            "java.lang.StringBuffer" -> StringBuffer(value)
            else -> parseJSON(value, type)
        }
        return result as T
    }

    // 兼容java环境使用，对返回数据进行二次处理。避免对基本数据类型返回null
    private fun returnsValue(value:Any?, type:Class<*>):Any? {
        if (value != null) return value

        return when (type.canonicalName) {
            "byte" -> 0.toByte()
            "short" -> 0.toShort()
            "int" -> 0
            "long" -> 0.toLong()
            "float" -> 0.toFloat()
            "double" -> 0.toDouble()
            "char" -> '0'
            "boolean" -> false
            else -> null
        }
    }

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
    
    companion object {
        @JvmStatic
        private val FASTJSON by lazy { return@lazy exist("com.alibaba.fastjson.JSON") }
        @JvmStatic
        private val GSON by lazy { return@lazy exist("com.google.gson.Gson") }
        @JvmStatic
        private val injector = BundleInjector()

        @JvmStatic
        fun create(source:Bundle? = null): EasyBundle {
            return EasyBundle(source?: Bundle())
        }

        fun toEntity(entity:Any?, bundle: Bundle?):Any? {
            if (entity == null || bundle == null) return null
            return injector.toEntity(entity, bundle)
        }

        fun toBundle(entity:Any?, bundle: Bundle?):Bundle? {
            if (entity == null || bundle == null) return bundle
            return injector.toBundle(entity, bundle)
        }

        @JvmStatic
        private fun exist(name:String):Boolean = try{
            Class.forName(name)
            true
        } catch (e:Exception) {
            false
        }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class BundleField(val value:String = "", val throwable:Boolean = true)

private class BundleInjector {
    // 缓存注解与字段的匹配信息。进行加速
    private val container = mutableMapOf<Class<*>, Map<String, Pair<Field, BundleField>>>()

    fun parseFields(clazz:Class<*>):Map<String, Pair<Field, BundleField>> {
        if (container.containsKey(clazz)) {
            return container.getValue(clazz)
        }

        // 将自身以及父类中配有BundleField注解的字段进行解析存储。
        var type = clazz
        val fields = HashMap<String, Pair<Field, BundleField>>()
        while (true) {
            val name = type.canonicalName
            if (name.startsWith("android")
                    || name.startsWith("java")
                    || name.startsWith("javax")
                    || name.startsWith("kotlin")) {
                // 对系统类进行跳过
                break
            }

            for (field in type.declaredFields) {
                val bundleField = field.getAnnotation(BundleField::class.java) ?: continue

                if (field.isAccessible.not()) {
                    field.isAccessible = true
                }

                fields[if (bundleField.value.isEmpty()) field.name else bundleField.value] = Pair(field, bundleField)
            }

            type = type.superclass
        }
        container[clazz] = fields
        return fields
    }

    // 将bundle中的数据注入到entity的对应字段中去。
    fun toEntity(entity:Any, bundle: Bundle):Any {
        val map = parseFields(entity.javaClass)
        val easyBundle = EasyBundle.create(bundle)
        for ((name, pair) in map) {
            try {
                if (bundle.containsKey(name).not()) continue

                val value = easyBundle.get(name, pair.first.type) ?: continue

                pair.first.set(entity, value)
            } catch (e:Exception) {
                if (pair.second.throwable) {
                    throw e
                }
                e.printStackTrace()
            }
        }
        return entity
    }

    // 将entity中的指定数据注入到bundle中去
    fun toBundle(entity:Any, bundle: Bundle):Bundle {
        val map = parseFields(entity.javaClass)
        val easyBundle = EasyBundle.create(bundle)
        for ((name, pair) in map) {
            try {
                val value = pair.first.get(entity) ?: continue
                easyBundle.put(name, value)
            } catch (e:Exception) {
                if (pair.second.throwable) {
                    throw e
                }
            }
        }
        return bundle
    }
}