package com.haoge.sample.easyandroid.activities.mvp.login

import android.os.Bundle
import butterknife.OnClick
import com.haoge.easyandroid.mvp.MVPPresenter
import com.haoge.sample.easyandroid.R
import com.haoge.sample.easyandroid.activities.mvp.base.BaseMVPFragment

/**
 * @author haoge on 2018/9/4
 */
class RegisterFragment : BaseMVPFragment(), RegisterView {

    val presenter = RegisterPresenter(this)
    override fun createPresenters() = arrayOf(presenter)
    override fun getLayoutId() = R.layout.fragment_register
    override fun initPage(savedInstanceState: Bundle?) {}

    @OnClick(R.id.to_login)
    fun toLogin() {
        getHostView().toLoginFragment()
    }

    @OnClick(R.id.register)
    fun onRegisterClick() {
        presenter.register("123456","123456")
    }

    override fun getHostView(): LoginMainView {
        return getHostActivity() as LoginMainView
    }

    override fun registerSuccess() {
        getHostView().loginSuccess()
    }
}