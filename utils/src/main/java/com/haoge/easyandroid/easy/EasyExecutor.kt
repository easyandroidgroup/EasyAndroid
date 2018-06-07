package com.haoge.easyandroid.easy

import android.os.Handler
import android.os.Looper
import java.util.concurrent.*

/**
 * @author haoge on 2018/6/6
 */
private typealias SUCCESS = (String) -> Unit
private typealias ERROR = (String, Throwable) -> Unit
private typealias START = (String) -> Unit

class EasyExecutor private constructor(val executor: ExecutorService,
                                       private val builder: Builder) {

    private var delay:Long = 0
    private var name:String? = null
    private var success:SUCCESS? = null
    private var error:ERROR? = null
    private var start:START? = null
    private var deliver:Executor? = null

    fun setName(name:String):EasyExecutor {
        this.name = name
        return this
    }

    fun setDelay(delay:Long):EasyExecutor {
        this.delay = Math.max(delay, 0)
        return this
    }

    fun onSuccess(success:SUCCESS):EasyExecutor {
        this.success = success
        return this
    }

    fun onError(error:ERROR): EasyExecutor {
        this.error = error
        return this
    }

    fun onStart(start:START):EasyExecutor {
        this.start = start
        return this
    }

    fun setDeliver(deliver: Executor): EasyExecutor {
        this.deliver = deliver
        return this
    }

    fun execute(runnable: Runnable) {
        postDelay {
            executor.execute(
                    TaskWrapper<Any>(
                    runnable = runnable,
                    builder = builder,
                    executor = this)
            )

            reset()
        }

    }

    fun <T> async(callable: Callable<T>, result:((T) -> Unit)? = null) {
        postDelay {
            executor.execute(TaskWrapper(callable = callable,
                    builder = builder,
                    executor = this,
                    result = result))
            reset()
        }
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
    }

    companion object {
        internal val mainHandler by lazy { return@lazy Handler(Looper.getMainLooper()) }

        @JvmStatic
        internal val UIDeliver:Executor = Executor {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                it.run()
            } else {
                mainHandler.post { it.run() }
            }
        }

        @JvmStatic
        internal val dispatcher:ScheduledExecutorService = Executors.newScheduledThreadPool(1, {
            val thread = Thread(it)
            thread.name = "Easy-task-Dispatcher"
            thread.priority = Thread.MAX_PRIORITY
             thread
        })

        fun newBuilder(size:Int):Builder {
            return Builder(size)
        }
    }

    class Builder internal constructor(private var size:Int) {
        internal var name:String = "EasyExecutor"
        internal var priority:Int = Thread.NORM_PRIORITY
        internal var success:SUCCESS? = null
        internal var error:ERROR? = null
        internal var start:START? = null
        internal var deliver:Executor = UIDeliver

        fun setName(name:String):Builder {
            if (name.isNotEmpty()) {
                this.name = name
            }
            return this
        }

        fun setPriority(priority:Int):Builder {
            when {
                priority < Thread.MIN_PRIORITY -> this.priority = Thread.MIN_PRIORITY
                priority > Thread.MAX_PRIORITY -> this.priority = Thread.MAX_PRIORITY
                else -> this.priority = priority
            }
            return this
        }

        fun onSuccess(success:SUCCESS):Builder {
            this.success = success
            return this
        }

        fun onError(error:ERROR): Builder {
            this.error = error
            return this
        }

        fun onStart(start: START):Builder {
            this.start = start
            return this
        }

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

    internal class TaskWrapper<T>(val callable: Callable<T>? = null,
                                  val runnable: Runnable? = null,
                                  val result:((T) -> Unit)? = null,
                                  executor:EasyExecutor,
                                  val builder:Builder) :Runnable {

        private var deliver:Executor = executor.deliver?:builder.deliver
        private var success:SUCCESS? = executor.success
        private var error:ERROR? = executor.error
        private var start:START? = executor.start
        private var name:String = executor.name?:builder.name

        override fun run() {
            Thread.currentThread().setUncaughtExceptionHandler {
                _, e ->
                deliver.execute {
                    builder.error?.invoke(name, e)
                    error?.invoke(name, e)
                }
            }
            Thread.currentThread().name = name
            deliver.execute {
                builder.start?.invoke(name)
                start?.invoke(name)
            }
            if (runnable != null) {
                runnable.run()
            } else if (callable != null) {
                @Suppress("UNCHECKED_CAST")
                val result:T = callable.call() as T
                deliver.execute { this.result?.invoke(result) }
            }
            deliver.execute {
                builder.success?.invoke(name)
                success?.invoke(name)
            }
        }
    }
}