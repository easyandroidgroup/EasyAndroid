package com.haoge.sample.easyandroid.activities.mvp

import android.os.Bundle
import com.haoge.easyandroid.easy.EasyExecutor
import com.haoge.easyandroid.easy.EasyToast
import com.haoge.easyandroid.mvp.MVPPresenter
import com.haoge.easyandroid.mvp.MVPView
import com.haoge.sample.easyandroid.R
import java.util.concurrent.Callable

/**
 * @author haoge on 2018/5/30
 */
class MVPDemoActivity:BaseMVPActivity<DemoPresenter>(),DemoView {
    override fun createPresenter() = DemoPresenter(this)

    override fun onQuerySuccess(message: String?) {
        // 接收数据请求任务的返回数据并展示
        EasyToast.DEFAULT.show(message)
    }

    override fun initPage(savedInstanceState: Bundle?) {
        // 发起数据请求任务
        presenter?.requestData()
    }

    override fun getLayoutId() = R.layout.activity_mvp_demo
}

// V层接口，定制界面UI更新的协议方法
interface DemoView:MVPView {
    fun onQuerySuccess(message:String?)
}

// P层数据处理：处理与界面无关的、与数据处理相关的逻辑。并用于连接M与V层的中间件
class DemoPresenter(view:DemoView):MVPPresenter<DemoView>(view) {
    val executor: EasyExecutor by lazy { EasyExecutor.newBuilder(1)
            .onError { _, _ ->
                if (!isViewAttached()) return@onError
                view.hideLoadingDialog()
            }
            .build() }

    fun requestData() {
        view?.showLoadingDialog()

        // 使用线程池模拟网络请求环境。
        executor.async(Callable {
            Thread.sleep(3000)
            return@Callable "Hello world!"
        }) {
            // 对于异步网络操作，回调时尽量先判断一下是否处于View绑定状态。
            if (!isViewAttached()) return@async

            view?.hideLoadingDialog()
            view?.onQuerySuccess(it)
        }
    }

}

