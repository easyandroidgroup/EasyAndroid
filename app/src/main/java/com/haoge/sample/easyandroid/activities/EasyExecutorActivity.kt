package com.haoge.sample.easyandroid.activities

import android.util.Log
import butterknife.OnClick
import com.haoge.easyandroid.easy.EasyExecutor
import com.haoge.easyandroid.easy.EasyLog
import com.haoge.easyandroid.easy.EasyToast
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R
import java.util.concurrent.Callable
import java.util.concurrent.Executor

/**
 * @author haoge on 2018/6/6
 */
class EasyExecutorActivity:BaseActivity() {

    val executor by lazy { return@lazy EasyExecutor.newBuilder(1)
            .setName("Sample Executor")
            .setPriority(Thread.NORM_PRIORITY)
            .build() }

    override fun getLayoutId(): Int {
        return R.layout.activity_executor
    }

    @OnClick(R.id.normalTask)
    fun normalTask() {
        executor.execute(Runnable {
            EasyToast.DEFAULT.show("执行普通的线程任务")
        })
    }

    @OnClick(R.id.asyncTask)
    fun asyncTask() {
        executor.async(Callable<String> {
            EasyToast.DEFAULT.show("执行异步任务。并返回数据")
            return@Callable "AsyncTask Returns Value"
        }, {
            EasyLog.DEFAULT.d("异步任务数据返回：$it")
        })
    }

    @OnClick(R.id.delayTask)
    fun delayTask() {
        executor.setDelay(3000)// 单位为毫秒
                .execute(Runnable { EasyToast.DEFAULT.show("延迟任务执行") })
    }

    @OnClick(R.id.launchWithCallback)
    fun launchWithCallback() {
        // 回调分为全局回调与局部回调

        // 创建实例时。指定全局回调：
        val executor = EasyExecutor.newBuilder(1)
                .setName("callback thread shown")
                .onStart {name -> EasyLog.DEFAULT.e("[全局回调] 任务[$name]执行开始") }
                .onError {name, e ->  EasyLog.DEFAULT.e("[全局回调] 任务[$name]执行出现异常：${e.message}") }
                .onSuccess { name -> EasyLog.DEFAULT.e("[全局回调] 任务[$name]执行完成") }
                .build()

        executor.onStart {name -> EasyLog.DEFAULT.e("[局部回调] 任务[$name]执行开始") }
                .onError {name, e ->  EasyLog.DEFAULT.e("[局部回调] 任务[$name]执行出现异常：${e.message}") }
                .onSuccess { name -> EasyLog.DEFAULT.e("[局部回调] 任务[$name]执行完成") }
                .execute(Runnable {  })
    }

    @OnClick(R.id.customDeliver)
    fun customDeliver() {
        executor.setDeliver(Executor {
            // 将回调通知派发到指定线程：
            val thread = Thread(it)
            thread.name = "callback-Thread"
            thread.start()
                }).onSuccess { EasyLog.DEFAULT.d("任务执行成功，当前线程为:${Thread.currentThread().name}") }
                .execute(Runnable { })
    }
}