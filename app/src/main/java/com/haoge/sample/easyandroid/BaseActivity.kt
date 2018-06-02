package com.haoge.sample.easyandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import butterknife.ButterKnife

/**
 * @author haoge on 2018/5/9
 */
abstract class BaseActivity:AppCompatActivity() {

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        ButterKnife.bind(this)
        initPage(savedInstanceState)
    }

    abstract fun getLayoutId():Int
    open fun initPage(savedInstanceState: Bundle?){}
}