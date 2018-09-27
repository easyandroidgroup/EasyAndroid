/*
 * Copyright (C) 2018 Haoge https://github.com/yjfnypeu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haoge.easyandroid.easy

import android.text.TextUtils
import android.util.Log
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

/**
 * @author haoge on 2018/5/10
 */
class EasyLog private constructor(
        private val upperName: String,
        var enable: Boolean,
        private val rules: Map<String, (StackTraceElement, Thread) -> String>,
        private val formatStyle: Format,// 多行显示时的样式
        private val singleStyle: Format?,// 单行显示时的样式
        private val formatter: EasyFormatter){
    private var tag:String = ""
    private var immediate:Boolean = false
    /**
     * 设置一个临时的tag值。在下次调用d/i/v/w/e/wtf方法进行日志输出时。进行使用。
     *
     * 使用后将自动置空。所以此tag的作用域[只在下一次打印]
     */
    fun tag(tag:String):EasyLog {
        this.tag = tag
        return this
    }

    /**
     * 指定是否使日志进行即时输出：即不讲打印任务派发到专属任务线程中，而是直接在当前线程进行打印。
     *
     * 使用后将自动置空。所以此immediate的作用域[只在下一次打印]
     */
    fun immediate(immediate:Boolean): EasyLog {
        this.immediate = immediate
        return this
    }

    fun d(message:String, vararg args:Any) {
        d(StringCombine(message, *args))
    }

    /**
     * 格式化数据any并进行Log.d()打印
     */
    fun d(any: Any?) {
        if (!enable) {
            return
        }
        dispatchToLogPrinterThread { thread, trace, TAG -> print(format(any), trace, "d", TAG, thread) }
    }

    fun i(message:String, vararg args:Any) {
        i(StringCombine(message, *args))
    }

    /**
     * 格式化数据any并进行Log.i()打印
     */
    fun i(any: Any?) {
        if (!enable) {
            return
        }
        dispatchToLogPrinterThread { thread, trace, TAG -> print(format(any), trace, "i",TAG, thread) }
    }

    fun v(message:String, vararg args:Any) {
        v(StringCombine(message, *args))
    }

    /**
     * 格式化数据any并进行Log.v()打印
     */
    fun v(any: Any?) {
        if (!enable) {
            return
        }
        dispatchToLogPrinterThread { thread, trace, TAG -> print(format(any), trace, "v",TAG, thread) }
    }

    fun w(message:String, vararg args:Any) {
        w(StringCombine(message, *args))
    }

    /**
     * 格式化数据any并进行Log.w()打印
     */
    fun w(any: Any?) {
        if (!enable) {
            return
        }
        dispatchToLogPrinterThread { thread, trace, TAG -> print(format(any), trace, "w",TAG, thread) }
    }

    fun e(message:String, vararg args:Any) {
        e(StringCombine(message, *args))
    }

    /**
     * 格式化数据any并进行Log.e()打印
     */
    fun e(any: Any?) {
        if (!enable) {
            return
        }
        dispatchToLogPrinterThread { thread, trace, TAG-> print(format(any), trace, "e",TAG, thread) }
    }

    fun wtf(message:String, vararg args:Any) {
        wtf(StringCombine(message, *args))
    }

    /**
     * 格式化数据any并进行Log.wtf()打印
     */
    fun wtf(any: Any?) {
        if (!enable) {
            return
        }
        dispatchToLogPrinterThread { thread, trace, TAG -> print(format(any), trace, "wtf",TAG, thread) }
    }

    // 将待打印数据传递到指定任务线程中去进行打印，避免出现阻塞UI线程的情况
    private fun dispatchToLogPrinterThread(invoke:(Thread, StackTraceElement, String) -> Unit) {
        val trace = findTrace()
        val current = Thread.currentThread()
        val tag = if (tag.isEmpty()) trace.fileName else tag
        this.tag = ""
        if (immediate) {
            invoke.invoke(current, trace, tag)
            immediate = false
        } else {
            EXECUTOR.execute { invoke.invoke(current, trace, tag) }
        }
    }

    private fun format(any:Any?):String {
        return when(any) {
            null ->  ""
            is StringCombine -> formatter.formatWithArgs(any.message, *any.args)
            else -> formatter.format(any)
        }
    }

    private fun print(message:String, trace: StackTraceElement, type:String, tag:String, callThread:Thread) {
        val lines = message.lines()
        val copyLineRules = ArrayList<LineRules>()
        var format = formatStyle
        if (lines.size == 1) {
            format = singleStyle?:formatStyle
        }

        for (lineRule in format.lineRules) {
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
        for ((index, lineRule) in copyLineRules.withIndex()) {
            val lineMessage:String = if (lineRule.isMessage) {
                lines[index - start]
            } else {
                start++
                ""
            }
            result.append(parseLine(lineMessage, lineRule, trace, cacheRules, callThread)).append("\n")
        }

        when(type) {
            "d" -> Log.d(tag, result.toString())
            "v" -> Log.v(tag, result.toString())
            "i" -> Log.i(tag, result.toString())
            "e" -> Log.e(tag, result.toString())
            "w" -> Log.w(tag, result.toString())
            "wtf" -> Log.wtf(tag, result.toString())
            else -> Log.d(tag, result.toString())
        }

    }

    private fun parseLine(message: String, lineRules: LineRules, trace: StackTraceElement, cacheRules: MutableMap<String, String>, callThread: Thread): String {
        var offset = 0
        var result = lineRules.origin
        val names = lineRules.names
        for ((index, name) in names) {
            val replace = when {
                name == "#M" -> message
                cacheRules[name] != null -> cacheRules[name]
                else -> {
                    cacheRules[name] = rules[name]?.invoke(trace, callThread)!!
                    cacheRules[name]
                }
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
        val DEFAULT: EasyLog by lazy { Builder(EasyLog::class.java.canonicalName).build() }
        private val EXECUTOR: ExecutorService by lazy {
            return@lazy Executors.newSingleThreadExecutor {
                val thread = Thread(it)
                thread.name = "EasyLog Printer Thread"
                thread.priority = Thread.MIN_PRIORITY
                thread.isDaemon = true
                thread.setUncaughtExceptionHandler { _, e ->
                    Log.e("EasyLog Printer ERROR", "EasyLog printer task has occurs some uncaught error. see stack traces for details:", e)
                }
                return@newSingleThreadExecutor thread
            }
        }
        @JvmStatic
        fun newBuilder(upper: String = EasyLog::class.java.canonicalName): Builder {
            return Builder(upper)
        }
    }

    class Builder internal constructor(val upperName:String) {
        /**
         * 是否开启日志输出？当为true时。才进行日志输出
         */
        var debug = true
        /**
         * 格式化日志输出样式。
         */
        var formatStyle = """
            >[EasyLog]#F
            >┌──────#T───────
            >│#M
            >└───────────────
            """.trimMargin(">")

        /**
         * 单行显示日志输出样式：当被排版后的数据为单行时，使用此样式进行输出。
         */
        var singleStyle = "[EasyLog]#f ==> #M"

        private val rules:MutableMap<String, (StackTraceElement, Thread)->String> = mutableMapOf(
                Pair("#T", { _, thread -> "[${thread.name}]"}),
                Pair("#F", { trace, _ -> "${trace.className.substringAfterLast('.')}.${trace.methodName}(${trace.fileName}:${trace.lineNumber})"}),
                Pair("#f", { trace, _ -> "(${trace.fileName}:${trace.lineNumber})"})
        )

        /**
         * 数据格式器。用于对数据进行格式化排版操作。
         */
        val formatter = DEFAULT_FORMATTER

        fun addRule(name:String, rule:(StackTraceElement, Thread) -> String) {
            rules["#$name"] = rule
        }

        fun build(): EasyLog {
            val regexBuilder = StringBuilder("(#M")
            for (rule in rules.keys) {
                regexBuilder.append("|$rule")
            }
            val regex = regexBuilder.append(")+").toString()

            val singleFormat = if (TextUtils.isEmpty(singleStyle)) null else Format(singleStyle, regex)
            return EasyLog(upperName, debug, rules, Format(formatStyle, regex), singleFormat, formatter)
        }

        companion object {
            val DEFAULT_FORMATTER: EasyFormatter by lazy {
                val builder = EasyFormatter.newBuilder()
                builder.maxMapSize = 10// 当映射型对象长度超过10时，平铺展示
                builder.maxArraySize = 10// 当数组型对象长度超过10事，平铺展示
                builder.maxLines = 100// 最高支持100行数据展示。
                return@lazy builder.build()
            }
        }
    }

    private class Format(format: String, regex: String) {

        val lineRules:ArrayList<LineRules> = ArrayList()

        init {
            var msgIndex = -1
            val lines = format.lines()
            for (index in lines.indices) {
                if (!lines[index].contains("#M"))  {
                    continue
                }

                if (msgIndex != -1) {
                    throw RuntimeException("Find [#M] in 'formatStyle' more than on times. but it requires only once!")
                }

                msgIndex = index
            }
            if (msgIndex == -1) {
                throw RuntimeException("Could not find formatStyle-style : [#T] in formatStyle-String")
            }

            for (index in lines.indices) {
                val origin = lines[index]

                val ruleSet = mutableSetOf<Pair<Int, String>>()
                val pattern = Pattern.compile(regex)
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

    private class StringCombine(val message:String, vararg val args:Any)
    private class LineRules(val origin:String, val names:Set<Pair<Int, String>>, val isMessage:Boolean)
}

