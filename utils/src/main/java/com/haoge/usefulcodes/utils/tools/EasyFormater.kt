package com.haoge.usefulcodes.utils.tools

import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * 对数据进行格式化
 *
 * @author haoge on 2018/5/10
 */
object EasyFormater {

    fun formatCollection(collection: Collection<*>): StringBuilder {
        val result = StringBuilder("[")
        if (!collection.isEmpty()) {
            result.append("\n")
        }
        for (item in collection) {
            formatAny(item).lines().forEach {
                appendLines(result, it)
            }
        }
        result.append("]")
        return result
    }

    fun formatMap(map: Map<*, *>): StringBuilder {
        val result = StringBuilder("{")
        if (!map.isEmpty()) {
            result.append("\n")
        }
        for ((key, value) in map) {
            formatAny(key).append(":").append(formatAny(value))
                    .lines().forEach { appendLines(result, it) }
        }
        result.append("}")
        return result
    }

    fun formatString(data: String): StringBuilder {
        if (data.startsWith("{")
            && data.endsWith("}")) {
            return formatJSONObject(data)
        } else if (data.startsWith("[")
            && data.endsWith("]")) {
            return formatJSONArray(data)
        }

        return StringBuilder(if (data.isEmpty()) "" else "\"$data\"")
    }

    fun formatJSONArray(data: String): StringBuilder {
        val result = StringBuilder("[")
        try {
            val array = JSONArray(data)
            val length = array.length()
            if (length != 0) {
                result.append("\n")
            }
            for (index in 0..length) {
                val item = array.optString(index)
                formatString(item).lines().forEach {
                    appendLines(result, it)
                }
            }
        } catch (e:Exception) {
            return StringBuilder(data)
        }
        result.append("]")
        return result
    }

    fun formatJSONObject(data: String): StringBuilder {
        val result = StringBuilder("{")
        try {
            val obj = JSONObject(data)
            val keys = obj.keys()
            if (obj.length() != 0) {
                result.append("\n")
            }
            while (keys.hasNext()) {
                val key = keys.next()
                val value = obj.optString(key)
                StringBuilder(formatString(key)).append(":")
                        .append(formatString(value))
                        .lines().forEach { appendLines(result, it) }
            }
        } catch (e:Exception) {
            return StringBuilder(data)
        }
        result.append("}")
        return result
    }

    private fun formatOther(any: Any): StringBuilder {
        val name = any.javaClass.canonicalName
        if (name.startsWith("android")
                || name.startsWith("java")
                || name.startsWith("javax")
                || name.startsWith("kotlin")) {
            // 不对系统提供的类进行格式化
            return StringBuilder(name)
        }

        val result = StringBuilder("{")
        val container = mutableMapOf<String, Any>()
        scanFields(any, any.javaClass, container)
        if (container.isNotEmpty()) {
            result.append("\n")
        }
        val keys = container.keys
        for (key in keys) {
            formatString(key).append(":").append(formatAny(container[key]))
                    .lines().forEach { appendLines(result, it) }
        }
        result.append("}")
        return result
    }

    fun formatAny(any: Any?): StringBuilder {
        return when(any) {
            null -> StringBuilder()
            is Collection<*> -> formatCollection(any)
            is Map<*, *> -> formatMap(any)
            is String -> formatString(any)
            is Int, is Boolean, is Short, is Char, is Byte, is Long, is Float, is Double
                    -> StringBuilder(any.toString())
            else -> EasyFormater.formatOther(any)
        }
    }

    private fun appendLines(result:StringBuilder, data:String) {
        if (data.isEmpty()) {
            return
        }
        result.append("\t").append(data).append("\n")
    }

    private fun scanFields(any: Any, clazz: Class<*>, container: MutableMap<String, Any>) {
        val name = clazz.canonicalName
        if (name.startsWith("android")
                || name.startsWith("java")
                || name.startsWith("javax")
                || name.startsWith("kotlin")) {
            // 不对系统提供的类进行格式化
            return
        }

        val fields = clazz.declaredFields
        for (field in fields) {
            val modifiers = field.modifiers
            if (Modifier.isNative(modifiers)) {
                continue
            }

            if (!field.isAccessible) {
                field.isAccessible = true
            }

            val value = field.get(any)
            if (CommonUtil.isEmpty(value)) {
                continue
            }

            container[field.name] = value
        }

        scanFields(any, clazz.superclass, container)
    }
}

fun Any?.format():String = EasyFormater.formatAny(this).toString()
