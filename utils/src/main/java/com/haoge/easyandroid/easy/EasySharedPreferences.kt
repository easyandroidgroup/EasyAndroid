package com.haoge.easyandroid.easy

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.HandlerThread
import com.alibaba.fastjson.JSON
import com.google.gson.Gson
import com.haoge.easyandroid.EasyAndroid
import java.lang.reflect.Field

/**
 * 对SharedPreferences的存取操作进行封装。
 * @author haoge on 2018/6/25
 */
class EasySharedPreferences(val clazz: Class<*>):SharedPreferences.OnSharedPreferenceChangeListener {

    // 绑定的具体实体类。
    internal val entity:PreferenceSupport
    // 所有的可操作变量
    private val fields = mutableMapOf<String, Field>()
    // 绑定的SharedPreference实例
    private val preferences:SharedPreferences
    // 存储待同步的数据的key值
    private val modifierKeys = mutableListOf<String>()

    init {
        if (PreferenceSupport::class.java.isAssignableFrom(clazz).not()) {
            throw RuntimeException("The class must be subclass of PreferenceSupport")
        }

        entity = clazz.newInstance() as PreferenceSupport
        val name:String = getValid(clazz.getAnnotation(PreferenceRename::class.java)?.value, clazz.simpleName)
        preferences = EasyAndroid.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE)
        // 注册内容变动监听器
        preferences.registerOnSharedPreferenceChangeListener(this)

        // 读取所有的可操作变量字段
        var type = clazz
        while (type != PreferenceSupport::class.java) {
            for (field in type.declaredFields) {
                if (field.isAnnotationPresent(PreferenceIgnore::class.java)) {
                    // 指定过滤此字段
                    continue
                }

                val key = getValid(field.getAnnotation(PreferenceRename::class.java)?.value, field.name)

                if (!fields.containsKey(key)) {
                    // 对于父类、子类均存在的字段。使用子类的数据进行存储
                    fields[key] = field
                    if (!field.isAccessible) {
                        field.isAccessible = true
                    }
                }
            }
            type = type.superclass
        }

        // 首次创建。先读取同步一遍
        read()
    }

    private val handler:Handler = Handler(thread.looper, { msg ->
        return@Handler when(msg.what) {
            READ -> {
                // 更新指定字段的数据
                synchronized(modifierKeys) {
                    val keys = modifierKeys.toTypedArray()
                    modifierKeys.clear()
                    val map = preferences.all
                    for (key in keys) {
                        val field = fields[key]
                        val value = map[key]
                        if (field == null || value == null) continue

                        readSingle(field, value)
                    }
                }
                true
            }
            WRITE -> {
                write()
                true
            }
            else -> false
        }
    })

    // 从SP中读取数据。注入到实体类中。
    private fun read() {
        synchronized(this) {
            val map = preferences.all
            for ((name, field) in fields) {
                readSingle(field, map[name])
            }
        }
    }

    private fun readSingle(field:Field, value:Any?) {
        if (value == null) return

        val type:Class<*> = field.type
        try {
            val result:Any? = when {
                type == Int::class.java -> value as Int
                type == Long::class.java -> value as Long
                type == Boolean::class.java -> value as Boolean
                type == Float::class.java -> value as Float
                type == String::class.java -> value as String
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

            result?.let { field.set(entity, it) }
        } catch (e:ClassCastException) {
            // ignore 只过滤此类异常。其他异常正常抛出
        }
    }

    // 将实体类中的数据。注入到SP容器中。
    private fun write() {
        synchronized(this) {
            preferences.unregisterOnSharedPreferenceChangeListener(this)
            val editor = preferences.edit()
            for ((name, field) in fields) {
                val value = field.get(entity)
                val type = field.type
                when {
                    type == Int::class.java -> editor.putInt(name, value as? Int?:0)
                    type == Long::class.java -> editor.putLong(name, value as? Long?:0L)
                    type == Boolean::class.java -> editor.putBoolean(name, value as? Boolean?:false)
                    type == Float::class.java -> editor.putFloat(name, value as? Float?:0f)
                    type == String::class.java -> editor.putString(name, value as? String?:"")
                    type == Byte::class.java
                        || type == Char::class.java
                        || type == Double::class.java
                        || type == Short::class.java
                        || type == StringBuilder::class.java
                        || type == StringBuffer::class.java
                        -> editor.putString(name, (value as Byte).toString())
                    GSON -> value?.let { editor.putString(name, Gson().toJson(it)) }
                    FASTJSON -> value?.let { editor.putString(name, JSON.toJSONString(value)) }
                }
            }
            editor.apply()
            preferences.registerOnSharedPreferenceChangeListener(this)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // 将待更新的key值进行存储
        synchronized(modifierKeys) {
            key?.let { (!modifierKeys.contains(it)).let { modifierKeys.add(key) } }
        }

        // 延迟100毫秒进行数据更新
        if (handler.hasMessages(READ)) return
        handler.sendEmptyMessageDelayed(READ, 100)
    }

    /**
     * 将类中的数据同步到SP容器中去。(在子线程中进行)
     */
    fun apply() {
        if (handler.hasMessages(WRITE)) return
        handler.sendEmptyMessageDelayed(WRITE, 100)
        handler.apply {  }
    }

    private fun getValid(value:String?, default:String):String =
            if (value.isNullOrEmpty()) default else value as String

    companion object {

        private const val READ = 1
        private const val WRITE = 2
        // 缓存容器
        private val container = mutableMapOf<Class<*>, EasySharedPreferences>()
        // 后台刷新线程
        private val thread:HandlerThread by lazy {
            val thread = HandlerThread("shared_update_thread")
            thread.start()
            return@lazy thread
        }

        @JvmStatic
        internal val FASTJSON by lazy { return@lazy exist("com.alibaba.fastjson.JSON") }
        @JvmStatic
        private val GSON by lazy { return@lazy exist("com.google.gson.Gson") }
        private fun exist(name:String):Boolean = try{
            Class.forName(name)
            true
        } catch (e:Exception) {
            false
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T> load(clazz: Class<T>):T {
            synchronized(container) {
                container[clazz]?.let { return it.entity as T}

                val instance = EasySharedPreferences(clazz)
                container[clazz] = instance
                return instance.entity as T
            }
        }

        internal fun find(clazz: Class<*>):EasySharedPreferences {
            return container[clazz]?:throw RuntimeException("Could not find EasySharedPreferences by this clazz:[${clazz.canonicalName}]")
        }
    }
}

abstract class PreferenceSupport {

    fun apply() {
        // 将当前类中的修改。同步到sp中去(任务运行于子线程)
        EasySharedPreferences.find(javaClass).apply()
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
annotation class PreferenceRename(val value:String = "")

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class PreferenceIgnore