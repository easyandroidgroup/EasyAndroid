package com.haoge.sample.easyandroid.activities.mvp.login

import android.os.Bundle
import butterknife.OnClick
import com.haoge.sample.easyandroid.R
import com.haoge.sample.easyandroid.activities.mvp.base.BaseMVPFragment

/**
 * @author haoge on 2018/9/4
 */
class LoginFragment: BaseMVPFragment(), LoginView {

    val presenter = LoginPresenter(this)
    override fun createPresenters() = arrayOf(presenter)
    override fun getLayoutId() = R.layout.fragment_login
    override fun initPage(savedInstanceState: Bundle?) {}

    @OnClick(R.id.to_register)
    fun toRegister() {
        getHostView().toRegisterFragment()
    }

    @OnClick(R.id.login)
    fun onLoginClick() {
        presenter.login("123456", "123456")
    }

    override fun loginSuccess() {
        getHostView().loginSuccess()
    }

    override fun getHostView(): LoginMainView {
        return getHostActivity() as LoginMainView
    }
}