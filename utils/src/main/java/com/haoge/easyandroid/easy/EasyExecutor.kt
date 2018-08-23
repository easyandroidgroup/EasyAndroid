package com.haoge.easyandroid.easy

import android.os.Handler
import android.os.Looper
import java.util.concurrent.*

/**
 * @author haoge on 2018/6/6
 */
private typealias SUCCESS = (String) -> Unit                // 任务执行成功时的回调
private typealias ERROR = (String, Throwable) -> Unit       // 任务执行失败。出现异常的回调
private typealias START = (String) -> Unit                  // 任务开始执行时的回调

private typealias RESULT<T> = (T) -> Unit                   // 异步结果回调
private typealias NORMAL_TASK = (Notifier) -> Unit          // 普通task类型
private typealias ASYNC_TASK<T> = (Notifier) -> T           // 异步返回task类型
private typealias PROGRESS = (Long, Long) -> Unit           // 进度条通知回调

class EasyExecutor private constructor(val executor: ExecutorService,
                                       private val builder: Builder) {

    // 临时配置缓存，这些配置将在任意一次任务启动之后进行重置。
    private var delay:Long = 0
    private var name:String? = null
    private var success:SUCCESS? = null
    private var error:ERROR? = null
    private var start:START? = null
    private var deliver:Executor? = null
    private var progress:PROGRESS? = null

    /**
     * 设置临时任务名[线程名]
     */
    fun setName(name:String):EasyExecutor {
        this.name = name
        return this
    }

    /**
     * 设置任务启动延迟时间：单位为毫秒
     */
    fun setDelay(delay:Long):EasyExecutor {
        this.delay = Math.max(delay, 0)
        return this
    }

    /**
     * 设置临时[任务执行成功回调通知]
     */
    fun onSuccess(success:SUCCESS):EasyExecutor {
        this.success = success
        return this
    }

    /**
     * 设置临时[任务指定失败回调通知]
     */
    fun onError(error:ERROR): EasyExecutor {
        this.error = error
        return this
    }

    /**
     * 设置临时[任务开始执行回调通知]
     */
    fun onStart(start:START):EasyExecutor {
        this.start = start
        return this
    }


    fun onProgressChanged(progress: PROGRESS): EasyExecutor {
        this.progress = progress
        return this
    }

    /**
     * 设置临时[回调通知派发器]
     */
    fun setDeliver(deliver: Executor): EasyExecutor {
        this.deliver = deliver
        return this
    }

    /**
     * 执行[普通异步任务]
     */
    fun execute(task:NORMAL_TASK) {
        postDelay {
            executor.execute(TaskWrapper<Any>(
                    builder = builder,
                    task = task,
                    executor = this))

            reset()
        }
    }

    /**
     * 执行[普通异步任务]
     */
    fun execute(task:Runnable) {
        execute {
            task.run()
        }
    }

    /**
     * 执行[异步回调任务]
     */
    fun <T> async(task:ASYNC_TASK<T>, result:RESULT<T>? = null) {
        postDelay {
            executor.execute(TaskWrapper(
                    async = task,
                    builder = builder,
                    executor = this,
                    result = result))
            reset()
        }
    }

    /**
     * 执行[异步回调任务]
     */
    fun <T> async(task:Callable<T>, result: RESULT<T>? = null) {
        async({
            task.call()
        }, result)
    }

    private fun postDelay (after:() -> Unit) {
        if (delay > 0) {
            dispatcher.schedule({after.invoke()}, delay, TimeUnit.MILLISECONDS)
        } else {
            after.invoke()
        }
    }

    private fun reset() {
        this.delay = 0
        this.success = null
        this.error = null
        this.start = null
        this.deliver = null
        this.progress = null
    }

    companion object {
        private val mainHandler = Handler(Looper.getMainLooper())

        private val UIDeliver:Executor = Executor {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                it.run()
            } else {
                mainHandler.post { it.run() }
            }
        }

        private val dispatcher:ScheduledExecutorService = Executors.newScheduledThreadPool(1, {
            val thread = Thread(it)
            thread.name = "Easy-task-Dispatcher"
            thread.priority = Thread.MAX_PRIORITY
             thread
        })

        @JvmStatic
        fun newBuilder(size:Int):Builder {
            return Builder(size)
        }
    }

    class Builder internal constructor(private var size:Int) {
        private var name:String = "EasyExecutor"
        private var priority:Int = Thread.NORM_PRIORITY
        private var success:SUCCESS? = null
        private var error:ERROR? = null
        private var start:START? = null
        private var deliver:Executor = UIDeliver

        // getter
        fun getName() = name
        fun getPriority() = priority
        fun getSuccess() = success
        fun getError() = error
        fun getStart() = start
        fun getDeliver() = deliver

        /**
         * 设置默认任务名[线程名]
         */
        fun setName(name:String):Builder {
            if (name.isNotEmpty()) {
                this.name = name
            }
            return this
        }

        /**
         * 设置创建的任务的[线程优先级]
         */
        fun setPriority(priority:Int):Builder {
            when {
                priority < Thread.MIN_PRIORITY -> this.priority = Thread.MIN_PRIORITY
                priority > Thread.MAX_PRIORITY -> this.priority = Thread.MAX_PRIORITY
                else -> this.priority = priority
            }
            return this
        }

        /**
         * 设置默认[任务成功回调]
         */
        fun onSuccess(success:SUCCESS):Builder {
            this.success = success
            return this
        }

        /**
         * 设置默认[任务失败回调]
         */
        fun onError(error:ERROR): Builder {
            this.error = error
            return this
        }

        /**
         * 设置默认[任务启动回调]
         */
        fun onStart(start: START):Builder {
            this.start = start
            return this
        }

        /**
         * 设置默认[任务派发器]
         */
        fun setDeliver(deliver:Executor):Builder{
            this.deliver = deliver
            return this
        }

        fun build():EasyExecutor {
            val executor = when {
                size <= 0 -> Executors.newCachedThreadPool(createFactory())
                else -> Executors.newFixedThreadPool(size, createFactory())
            }
            return EasyExecutor(executor, this)
        }

        private fun createFactory() = ThreadFactory {
            val thread = Thread(it)
            thread.name = name
            thread.priority = priority
            return@ThreadFactory thread
        }
    }

    private class TaskWrapper<T>(val task:NORMAL_TASK? = null,     // 待执行的普通任务
                                  val async:ASYNC_TASK<T>? = null,  // 待执行的异步回调任务
                                  val result:RESULT<T>? = null,     // 与异步回调任务对应的：结果回调
                                  val builder:Builder,              // 一些默认配置的存储容器
                                  executor:EasyExecutor             // 从此容易中读取一些临时配置进行使用
                                    ) :Runnable {

        private var deliver:Executor = executor.deliver?:builder.getDeliver()
        private var success:SUCCESS? = executor.success
        private var error:ERROR? = executor.error
        private var start:START? = executor.start
        private var name:String = executor.name?:builder.getName()
        private val notifier = Notifier(deliver, executor.progress)

        override fun run() {
            Thread.currentThread().setUncaughtExceptionHandler {
                _, e ->
                deliver.execute {
                    builder.getError()?.invoke(name, e)
                    error?.invoke(name, e)
                }
            }
            Thread.currentThread().name = name
            deliver.execute {
                builder.getStart()?.invoke(name)
                start?.invoke(name)
            }
            if (task != null) {
                task.invoke(notifier)
            } else if (async != null) {
                @Suppress("UNCHECKED_CAST")
                val result:T = async.invoke(notifier)
                deliver.execute { this.result?.invoke(result) }
            }
            deliver.execute {
                builder.getSuccess()?.invoke(name)
                success?.invoke(name)
            }
        }
    }
}

class Notifier(private val deliver: Executor, private val progress: PROGRESS?) {
    /**
     * 通知任务处理进度有改变：
     */
    fun progressChanged(current:Long, total:Long) {
        progress?.let { deliver.execute { it.invoke(current, total) } }
    }
}