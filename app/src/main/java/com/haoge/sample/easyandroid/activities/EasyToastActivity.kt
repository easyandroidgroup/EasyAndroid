package com.haoge.sample.easyandroid.activities

import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import butterknife.OnClick
import com.haoge.easyandroid.easy.EasyToast
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author haoge on 2018/5/9
 */
class EasyToastActivity : BaseActivity(){

    val default by lazy { EasyToast.DEFAULT }
    val creator by lazy {
        // 创建自定义的Toast.
        val layout = LayoutInflater.from(this).inflate(R.layout.toast_style, null)
        EasyToast.newBuilder(layout, R.id.toast_tv)
                .setDuration(Toast.LENGTH_LONG)
                .setGravity(Gravity.CENTER, 0, 0)
                .build()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_easy_toast
    }

    @OnClick(R.id.showOnMainThreadWithDefault)
    fun showOnMainThreadWithDefault() {
        default.show("使用默认样式在主线程中进行展示, Thread: ${Thread.currentThread()}")
    }

    @OnClick(R.id.showOnSubThreadWithDefault)
    fun showOnSubThreadWithDefault() {
        Instance.pool.execute { default.show("使用默认样式在子线程中进行展示, Thread: ${Thread.currentThread()}") }
    }

    @OnClick(R.id.showOnMainThreadWithCreated)
    fun showOnMainThreadWithCreated() {
        creator.show("使用自定义样式在主线程中进行展示, Thread: ${Thread.currentThread()}")
    }

    @OnClick(R.id.showOnSubThreadWithCreate)
    fun showOnSubThreadWithCreate() {
        Instance.pool.execute { creator.show("使用自定义样式在子线程中进行展示, Thread: ${Thread.currentThread()}") }
    }

    @OnClick(R.id.showMultiTimeToast)
    fun showMultiTimeToast() {
        Instance.pool.execute {
            for (index in 0..10) {
                default.show("自动更新无延迟提醒：$index")
                Thread.sleep(300)
            }
            default.show("循环完毕")
        }
    }

    // 为减小内存开销，创建一次线程池
    object Instance {
        val pool:ExecutorService = Executors.newSingleThreadExecutor()
    }
}