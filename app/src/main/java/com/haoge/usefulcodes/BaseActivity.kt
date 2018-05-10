package com.haoge.usefulcodes

import android.app.Activity
import butterknife.ButterKnife

/**
 * @author haoge on 2018/5/9
 */
open class BaseActivity:Activity() {
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        ButterKnife.bind(this)
    }
}