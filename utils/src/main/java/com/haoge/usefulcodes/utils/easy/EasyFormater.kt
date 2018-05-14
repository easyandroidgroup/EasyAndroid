package com.haoge.usefulcodes.utils.easy

import android.util.Log
import com.haoge.usefulcodes.utils.tools.CommonUtil
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Modifier
import java.util.*
import java.util.regex.Pattern

/**
 * 对数据进行格式化
 *
 * @author haoge on 2018/5/10
 */
class EasyFormater private constructor(private val builder: Builder) {

    // 格式化List/Set集合数据
    private fun formatCollection(collection: Collection<*>): StringBuilder {
        val result = StringBuilder("[")
        val isFlat = collection.size > builder.maxArraySize
        appendIterator(result, collection.iterator(), isFlat)
        result.append(if (isFlat) "]" else "\n]")
        return result
    }

    // 格式化map数据
    private fun formatMap(map: Map<*, *>): StringBuilder {
        val result = StringBuilder("{")
        val isFlat = map.size > builder.maxMapSize
        appendIterator(result, map.iterator(), isFlat)
        result.append(if (isFlat) "}" else "\n}")
        return result
    }

    private fun formatString(data: String): StringBuilder {
        if (data.startsWith("{")
            && data.endsWith("}")) {
            return formatJSONObject(data)
        } else if (data.startsWith("[")
            && data.endsWith("]")) {
            return formatJSONArray(data)
        }

        return StringBuilder(if (data.isEmpty()) "" else "\"$data\"")
    }

    private fun formatJSONArray(data: String): StringBuilder {
        val result = StringBuilder("[")
        var isFlat = true
        try {
            val array = JSONArray(data)
            val length = array.length()
            isFlat = length > builder.maxArraySize

            for (index in 0..(length - 1)) {
                val sub = StringBuilder()
                if (!isFlat) {
                    result.append("\n")
                }

                sub.append(formatString(array.optString(index)))
                if (index != length - 1) {
                    sub.append(",")
                }
                appendSubString(result, sub)
            }

        } catch (e:Exception) {
            return StringBuilder(data)
        }
        result.append(if (isFlat) "]" else "\n]")
        return result
    }

    private fun formatJSONObject(data: String): StringBuilder {
        val result = StringBuilder("{")
        var isFlat = true
        try {
            val obj = JSONObject(data)
            isFlat = obj.length() > builder.maxMapSize
            appendIterator(result, obj.keys(), isFlat)
        } catch (e:Exception) {
            return StringBuilder(data)
        }
        result.append(if (isFlat) "}" else "\n}")
        return result
    }

    private fun formatException(any: Throwable): StringBuilder {
        return StringBuilder(Log.getStackTraceString(any))
    }

    private fun formatOther(any: Any): StringBuilder {
        val name = any.javaClass.canonicalName
        if (name.startsWith("android")
                || name.startsWith("java")
                || name.startsWith("javax")
                || name.startsWith("kotlin")) {
            // 不对系统提供的类进行格式化
            return StringBuilder("\"$name\"")
        }

        val result = StringBuilder("{")
        val container = mutableMapOf<String, Any>()
        scanFields(any, any.javaClass, container)

        val isFlat = container.size > builder.maxMapSize
        appendIterator(result, container.iterator(), isFlat)

        result.append(if (isFlat) "}" else "\n}")
        return result
    }

    fun formatAny(any: Any?): String {
        val result = formatAnyInternal(any)
        return if (result.lines().size > builder.maxLines) {
            result.replace(Pattern.compile("\n").toRegex(), "")
        } else {
            result.toString()
        }
    }

    private fun formatAnyInternal(any:Any?):StringBuilder =
        when(any) {
            null -> StringBuilder()
            is Collection<*> -> formatCollection(any)
            is Map<*, *> -> formatMap(any)
            any::class.java.isArray -> formatCollection(Arrays.asList(any))
            is String -> formatString(any)
            is Throwable -> formatException(any)
            is Int, is Boolean, is Short, is Char, is Byte, is Long, is Float, is Double
            -> StringBuilder(any.toString())
            else -> formatOther(any)
        }

    private fun appendIterator(container:StringBuilder, /*数据存储容器*/
                               iterator:Iterator<*>,
                               isFlat:Boolean) {
        var hasNext = iterator.hasNext()
        while (hasNext) {
            if (!isFlat) {
                container.append("\n")
            }

            val sub = StringBuilder()
            val next = iterator.next()
            if (next is Map.Entry<*, *>) {
                sub.append(formatAnyInternal(next.key))
                        .append(":")
                        .append(formatAnyInternal(next.value))
            } else {
                sub.append(formatAnyInternal(next))
            }

            hasNext = iterator.hasNext()
            if (hasNext) {
                sub.append(", ")
            }
            appendSubString(container, sub)
        }
    }

    private fun appendSubString(container:StringBuilder, subString:StringBuilder) {
        val lines = subString.lines()
        for ((index, value) in lines.withIndex()) {
            if (index == 0) {
                container.append("\t").append(value)
            } else if (value.isNotEmpty()) {
                container.append("\n").append("\t").append(value)
            }
        }
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

    companion object {
        val DEFAULT by lazy { return@lazy Builder().build() }
        fun newBuilder():Builder {
            return Builder()
        }
    }

    class Builder internal constructor() {

        /**
         * 最大行数，当格式化后的数据行数超过此数量后，将对数据进行平铺：只剩一行
         */
        var maxLines:Int = 50
        /**
         * 最大Array尺寸，当Array(包括List/Set/Array/JSONArray)的长度超过此数量限制时：数据以平铺模式展示。
         */
        var maxArraySize:Int = 20
        /**
         * 最大Map尺寸，当Map(包括Map/JSONObject/Bean)的长度超过此数量限制时：数据以平铺模式展示
         */
        var maxMapSize:Int = 20

        fun build():EasyFormater {
            return EasyFormater(this)
        }
    }
}

fun Any?.format():String = EasyFormater.DEFAULT.formatAny(this)
