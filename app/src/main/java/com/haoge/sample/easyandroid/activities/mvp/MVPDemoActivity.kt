package com.haoge.sample.easyandroid.activities.mvp

import com.haoge.easyandroid.easy.EasyToast
import com.haoge.easyandroid.mvp.MVPPresenter
import com.haoge.easyandroid.mvp.MVPView
import com.haoge.sample.easyandroid.R
import com.lzh.easythread.AsyncCallback
import com.lzh.easythread.EasyThread

/**
 * @author haoge on 2018/5/30
 */
class MVPDemoActivity:BaseMVPActivity<DemoPresenter>(),DemoView {
    override fun createPresenter() = DemoPresenter(this)

    override fun onQuerySuccess(message: String?) {
        // 接收数据请求任务的返回数据并展示
        EasyToast.DEFAULT.show(message)
    }

    override fun initPage() {
        // 发起数据请求任务
        presenter?.requestData()
    }

    override fun getLayoutId() = R.layout.activity_mvp_demo
}

interface DemoView:MVPView {
    fun onQuerySuccess(message:String?)
}

class DemoPresenter(view:DemoView):MVPPresenter<DemoView>(view) {
    val executor: EasyThread by lazy { EasyThread.Builder.createSingle().build() }

    fun requestData() {
        view?.showLoadingDialog()

        // 使用线程池模拟网络请求环境。
        executor.async({
            Thread.sleep(3000)
            return@async "Hello world"
        }, object : AsyncCallback<String>{
            override fun onSuccess(t: String?) {
                // 对于异步网络操作，回调时尽量先判断一下是否处于View绑定状态。
                if (!isViewAttached()) return

                view?.hideLoadingDialog()
                view?.onQuerySuccess(t)
            }

            override fun onFailed(t: Throwable?) {
                view?.hideLoadingDialog()
                view?.toastMessage("获取数据失败：Cause by -> ${t?.message?:"未知错误"}")
            }
        })
    }

}

