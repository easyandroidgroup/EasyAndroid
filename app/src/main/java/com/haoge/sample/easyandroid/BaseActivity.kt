package com.haoge.sample.easyandroid

import android.support.v7.app.AppCompatActivity
import butterknife.ButterKnife

/**
 * @author haoge on 2018/5/9
 */
open class BaseActivity:AppCompatActivity() {
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        ButterKnife.bind(this)
    }
}