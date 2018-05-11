package com.haoge.usefulcodes.activities

import android.os.Bundle
import android.widget.Toast
import butterknife.OnClick
import com.haoge.usefulcodes.BaseActivity
import com.haoge.usefulcodes.R
import com.haoge.usefulcodes.utils.easy.EasyToast
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author haoge on 2018/5/9
 */
class EasyToastActivity :BaseActivity(){

    // 创建EasyToast实例需要在主线程进行初始化，所以就直接在外面一次性创建了
    val default = EasyToast.DEFAULT
    val creator = EasyToast.create(R.layout.toast_style, R.id.toast_tv, Toast.LENGTH_SHORT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_easy_toast)


    }

    @OnClick(R.id.showOnMainThreadWithDefault)
    fun showOnMainThreadWithDefault() {
        default.show("使用默认样式在主线程中进行展示")
    }

    @OnClick(R.id.showOnSubThreadWithDefault)
    fun showOnSubThreadWithDefault() {
        Instance.pool.execute { default.show("使用默认样式在子线程中进行展示") }
    }

    @OnClick(R.id.showOnMainThreadWithCreated)
    fun showOnMainThreadWithCreated() {
        creator.show("使用自定义样式在主线程中进行展示")
    }

    @OnClick(R.id.showOnSubThreadWithCreate)
    fun showOnSubThreadWithCreate() {
        Instance.pool.execute { creator.show("使用自定义样式在子线程中进行展示") }
    }

    @OnClick(R.id.showMultiTimeToast)
    fun showMultiTimeToast() {
        Instance.pool.execute {
            for (index in 0..10) {
                default.show("自动更新无延迟提醒：$index")
                Thread.sleep(500)
            }
            default.show("循环完毕")
        }
    }

    // 为减小内存开销，创建一次线程池
    object Instance {
        val pool:ExecutorService = Executors.newSingleThreadExecutor()
    }
}