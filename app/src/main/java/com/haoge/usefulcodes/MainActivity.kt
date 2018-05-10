package com.haoge.usefulcodes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import butterknife.ButterKnife
import butterknife.OnClick
import com.haoge.usefulcodes.activities.EasyToastActivity

/**
 * @author haoge on 2018/5/9
 */
class MainActivity:BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
    }

    @OnClick(R.id.testEasyToast)
    fun toToastTestActivity() {
        startActivity(Intent(this, EasyToastActivity::class.java))
    }
}