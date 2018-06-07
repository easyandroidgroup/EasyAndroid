package com.haoge.sample.easyandroid.activities

import android.view.Gravity
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
    val creator by lazy { EasyToast.create(R.layout.toast_style, R.id.toast_tv, Toast.LENGTH_SHORT) }

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

    @OnClick(R.id.showDefaultCenterWithGravity)
    fun showDefaultCenterWithGravity() {
        default.setGravity(Gravity.CENTER, 0, 0).show("使用默认样式在中心展示")
    }

    @OnClick(R.id.showCustomTopWithGravity)
    fun showCustomTopWithGravity() {
        creator.setGravity(Gravity.TOP, 0, 20).show("使用自定义样式在顶部展示")
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