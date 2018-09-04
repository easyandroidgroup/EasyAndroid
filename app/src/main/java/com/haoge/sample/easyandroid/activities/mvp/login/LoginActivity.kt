package com.haoge.sample.easyandroid.activities.mvp.login

import android.os.Bundle
import com.haoge.easyandroid.easy.EasyToast
import com.haoge.easyandroid.mvp.MVPView
import com.haoge.sample.easyandroid.R
import com.haoge.sample.easyandroid.activities.mvp.base.BaseMVPActivity

/**
 * 使用一个相对完整一点的Login模块作为MVP使用展示demo。
 *
 * @author haoge on 2018/9/3
 */
class LoginActivity:BaseMVPActivity(), LoginMainView {
    private val loginFragment = LoginFragment()
    private val registerFragment = RegisterFragment()

    override fun toRegisterFragment() {
        fragmentManager.beginTransaction()
                .show(registerFragment)
                .hide(loginFragment)
                .commitAllowingStateLoss()
    }

    override fun toLoginFragment() {
        fragmentManager.beginTransaction()
                .show(loginFragment)
                .hide(registerFragment)
                .commitAllowingStateLoss()
    }

    override fun getLayoutId(): Int = R.layout.activity_login

    override fun initPage(savedInstanceState: Bundle?) {
        fragmentManager.beginTransaction()
                .add(R.id.container, loginFragment)
                .add(R.id.container, registerFragment)
                .commitAllowingStateLoss()

        toLoginFragment()
    }

    override fun loginSuccess() {
        EasyToast.DEFAULT.show("登录成功！")
        finish()
    }
}

// 单独提供一个View让LoginActivity实现。作为与Fragment之间回调通信使用
interface LoginMainView:MVPView {
    // 通知登录成功
    fun loginSuccess()
    // 切换到注册页
    fun toRegisterFragment()
    // 切换到登录页
    fun toLoginFragment()
}