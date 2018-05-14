package com.haoge.usefulcodes.utils.easy

import android.util.Log
import java.util.regex.Pattern

/**
 * @author haoge on 2018/5/10
 */
class EasyLog private constructor(
        private val upperName: String,
        private val enable: Boolean,
        private val rules: Map<String, (StackTraceElement) -> String>,
        private val formatStyle: Format,
        private val formater: EasyFormater
){

    fun d(any: Any?) {
        if (!enable) {
            return
        }
        val trace = findTrace()
        print(formater.formatAny(any), trace, "d")
    }
    fun i(any: Any?) {
        if (!enable) {
            return
        }
        val trace = findTrace()
        print(formater.formatAny(any), trace, "i")
    }
    fun v(any: Any?) {
        if (!enable) {
            return
        }
        val trace = findTrace()
        print(formater.formatAny(any), trace, "v")
    }
    fun w(any: Any?) {
        if (!enable) {
            return
        }
        val trace = findTrace()
        print(formater.formatAny(any), trace, "w")
    }
    fun e(any: Any?) {
        if (!enable) {
            return
        }
        val trace = findTrace()
        print(formater.formatAny(any), trace, "e")
    }
    fun wtf(any: Any?) {
        if (!enable) {
            return
        }
        val trace = findTrace()
        print(formater.formatAny(any), trace, "wtf")
    }

    private fun print(message:String, trace: StackTraceElement, type:String) {
        val lines = message.lines()
        val copyLineRules = ArrayList<LineRules>()

        for (lineRule in formatStyle.lineRules) {
            if (!lineRule.isMessage) {
                copyLineRules.add(lineRule)
                continue
            }

            for (index in lines.indices) {
                copyLineRules.add(lineRule)
            }
        }

        val result = StringBuilder("")
        val cacheRules = mutableMapOf<String, String>()
        var start = 0
        for (index in copyLineRules.indices) {
            val lineRule = copyLineRules[index]
            val lineMessage:String = if (lineRule.isMessage) {
                lines[index - start]
            } else {
                start++
                ""
            }
            result.append(parseLine(lineMessage, lineRule, trace, cacheRules)).append("\n")
        }

        when(type) {
            "d" -> Log.d(trace.fileName, result.toString())
            "v" -> Log.v(trace.fileName, result.toString())
            "i" -> Log.i(trace.fileName, result.toString())
            "e" -> Log.e(trace.fileName, result.toString())
            "w" -> Log.w(trace.fileName, result.toString())
            "wtf" -> Log.wtf(trace.fileName, result.toString())
            else -> Log.d(trace.fileName, result.toString())
        }

    }

    private fun parseLine(message: String, lineRules: LineRules, trace: StackTraceElement, cacheRules: MutableMap<String, String>): String {
        var offset = 0
        var result = lineRules.origin
        val names = lineRules.names
        for ((index, name) in names) {
            val replace = if (name == "#M") {
                message
            } else if (cacheRules[name] != null){
                cacheRules[name]
            } else {
                cacheRules[name] = rules[name]?.invoke(trace)!!
                cacheRules[name]
            }
            val start = index.plus(offset)
            result = result.substring(0, start) + replace + result.substring(start + 2)
            offset = offset.plus((replace?.length ?: 0) - 2)
        }
        return result
    }

    private fun findTrace():StackTraceElement {
        val traces = Exception().stackTrace
        var trace:StackTraceElement? = null
        var matched = false
        for (item in traces) {
            if (!matched && item.className.startsWith(upperName)) {
                matched = true
                continue
            }

            if (matched && !item.className.startsWith(upperName)) {
                trace = item
                break
            }
        }

        if (trace == null) {
            throw RuntimeException("Could not matched class info. Please check your upper name.")
        }

        return trace
    }

    companion object {
        @JvmStatic
        val DEFAULT: EasyLog by lazy { Builder(EasyLog::class.java.canonicalName).build() }
        @JvmStatic
        fun newBuilder(upper: Class<*>):Builder {
            return Builder(upper.canonicalName)
        }
    }

    class Builder internal constructor(val upperName:String) {
        var debug = true
        /**
         * #T -> ThreadName
         * #F -> FileLine
         * #M -> Message
         */
        var format = """
            > [EasyLog]#F
            >┌──────#T───────
            >│#M
            >└───────────────
            """.trimMargin(">")

        private val rules:MutableMap<String, (StackTraceElement)->String> = mutableMapOf(
                Pair("#T", { _ -> "[${Thread.currentThread().name}]"}),
                Pair("#F", { trace -> "(${trace.fileName}:${trace.lineNumber})"})
        )

        val formater by lazy {
            val builder = EasyFormater.newBuilder()
            builder.maxMapSize = 10
            builder.maxArraySize = 10
            builder.maxLines = 100
            return@lazy builder.build()
        }

        fun addRule(name:String, rule:(StackTraceElement) -> String) {
            rules["#$name"] = rule
        }

        fun build():EasyLog {

            return EasyLog(upperName, debug, rules, Format(format), formater)
        }
    }

    private class Format(format:String) {

        val lineRules:ArrayList<LineRules> = ArrayList()

        init {
            var msgIndex = -1
            val lines = format.lines()
            for (index in lines.indices) {
                if (!lines[index].contains("#M"))  {
                    continue
                }

                if (msgIndex != -1) {
                    throw RuntimeException("Find [#M] in 'format' more than on times. but it requires only once!")
                }

                msgIndex = index
            }
            if (msgIndex == -1) {
                throw RuntimeException("Could not find format-style : [#T] in format-String")
            }
            for (index in lines.indices) {
                val origin = lines[index]
                val ruleSet = mutableSetOf<Pair<Int, String>>()
                val pattern = Pattern.compile("#([TFM])+")
                val matcher = pattern.matcher(origin)
                while(matcher.find()) {
                    val name = matcher.group()
                    val indexOf = origin.indexOf(name)
                    ruleSet.add(Pair(indexOf, name))
                }
                lineRules.add(LineRules(origin, ruleSet, index == msgIndex))
            }
        }
    }

    private class LineRules(val origin:String, val names:Set<Pair<Int, String>>, val isMessage:Boolean)
}
