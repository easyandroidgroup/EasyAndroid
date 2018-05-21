package com.haoge.easyandroid.easy

import android.util.Log
import com.haoge.easyandroid.tools.CommonUtil
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
class EasyFormatter private constructor(private val builder: Builder) {

    // 格式化List/Set集合数据
    private fun formatCollection(collection: Collection<*>): StringBuilder {
        val result = StringBuilder("[")
        val isFlat = isFlat(builder.maxArraySize, collection.size)
        appendIterator(result, collection.iterator(), isFlat)
        result.append("]")
        return result
    }

    // 格式化map数据
    private fun formatMap(map: Map<*, *>): StringBuilder {
        val result = StringBuilder("{")
        val isFlat = isFlat(builder.maxMapSize, map.size)
        appendIterator(result, map.iterator(), isFlat)
        result.append("}")
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

        return StringBuilder(data)
    }

    private fun formatJSONArray(data: String): StringBuilder {
        val result = StringBuilder("[")
        try {
            val array = JSONArray(data)
            val length = array.length()
            val isFlat = isFlat(builder.maxArraySize, length)

            for (index in 0..(length - 1)) {
                val sub = StringBuilder()
                if (!isFlat) {
                    result.append("\n")
                }

                sub.append(formatString(array.optString(index)))
                if (index != length - 1) {
                    sub.append(",")
                }
                appendSubString(result, sub, isFlat)
            }
            if (!isFlat) {
                result.append("\n")
            }
        } catch (e:Exception) {
            return StringBuilder(data)
        }
        result.append("]")
        return result
    }

    private fun formatJSONObject(data: String): StringBuilder {
        val result = StringBuilder("{")
        try {
            val json = JSONObject(data)
            val length = json.length()
            val keys = json.keys()
            val isFlat = isFlat(builder.maxMapSize, length)
            var hasNext = keys.hasNext()
            while (hasNext) {
                if (!isFlat) {
                    result.append("\n")
                }

                val sub = StringBuilder()
                val next = keys.next()
                sub.append(formatString(next)).append(":").append(json.optString(next))

                hasNext = keys.hasNext()
                if (hasNext) {
                    sub.append(", ")
                }
                appendSubString(result, sub, isFlat)
            }
            if (!isFlat) {
                result.append("\n")
            }
        } catch (e:Exception) {
            return StringBuilder(data)
        }
        result.append("}")
        return result
    }

    private fun isFlat(maxSize:Int, length:Int):Boolean {
        return when {
            maxSize < 0 -> false
            length <= maxSize -> false
            else -> true
        }
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
            return StringBuilder(name)
        }

        val result = StringBuilder("{")
        val container = mutableMapOf<String, Any>()
        scanFields(any, any.javaClass, container)

        val isFlat = isFlat(builder.maxMapSize, container.size)
        appendIterator(result, container.iterator(), isFlat)

        result.append("}")
        return result
    }

    fun format(any: Any?): String {
        val format = formatAny(any)
        val lines = format.lines()
        return if (isFlat(builder.maxLines, lines.size)) {
            // 总长度大于受限长度。需要进行平铺处理
            val result = StringBuilder()
            for ((index, value) in lines.withIndex()) {
                if (index < builder.maxLines - 1) {
                    result.append(value)
                    result.append("\n")
                } else {
                    result.append(value.replace(Pattern.compile("(\t)").toRegex(), ""))
                }
            }
            result.toString()
        } else {
            format.toString()
        }
    }

    private fun formatAny(any:Any?):StringBuilder =
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
                sub.append(formatAny(next.key))
                        .append(":")
                        .append(formatAny(next.value))
            } else {
                sub.append(formatAny(next))
            }

            hasNext = iterator.hasNext()
            if (hasNext) {
                sub.append(", ")
            }
            appendSubString(container, sub, isFlat)
        }
        if (!isFlat) {
            container.append("\n")
        }
    }

    private fun appendSubString(container:StringBuilder, subString:StringBuilder, isFlat: Boolean) {
        val lines = subString.lines()
        for ((index, value) in lines.withIndex()) {
            when {
                isFlat -> container.append(value)
                index == 0 -> container.append("\t").append(value)
                value.isNotEmpty() -> container.append("\n").append("\t").append(value)
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
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder internal constructor() {

        /**
         * 最大行数，当格式化后的数据行数超过此数量后，将对超出部分数据进行平铺展示
         */
        var maxLines:Int = -1
        /**
         * 最大Array尺寸，当Array(包括List/Set/Array/JSONArray)的长度超过此数量限制时：数据以平铺模式展示。
         */
        var maxArraySize:Int = -1
        /**
         * 最大Map尺寸，当Map(包括Map/JSONObject/Bean)的长度超过此数量限制时：数据以平铺模式展示
         */
        var maxMapSize:Int = -1

        fun build(): EasyFormatter {
            return EasyFormatter(this)
        }
    }
}
