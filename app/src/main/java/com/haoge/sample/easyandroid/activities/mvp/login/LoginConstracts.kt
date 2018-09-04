package com.haoge.sample.easyandroid.activities.mvp.login

import com.haoge.easyandroid.easy.EasyExecutor
import com.haoge.easyandroid.mvp.MVPPresenter
import com.haoge.easyandroid.mvp.MVPView

interface LoginView:MVPView {
    fun loginSuccess()
    fun getHostView():LoginMainView
}

class LoginPresenter(view: LoginView?) : MVPPresenter<LoginView>(view) {
    val executor:EasyExecutor = EasyExecutor.newBuilder(1).build()

    fun login(username:String, password:String) {
        getView().showLoadingDialog()
        executor.async(
                {
                    // 延时2秒。模拟网络操作
                    Thread.sleep(2000)
                },
                {
                    // 异步回调中，需先过滤view detach条件。
                    if (isViewAttached().not()) return@async

                    getView().hideLoadingDialog()
                    getView().loginSuccess()

                }
        )
    }
}