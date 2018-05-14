package com.haoge.usefulcodes.activities

import android.os.Bundle
import butterknife.OnClick
import com.haoge.usefulcodes.BaseActivity
import com.haoge.usefulcodes.R
import com.haoge.usefulcodes.utils.easy.EasyFormater
import com.haoge.usefulcodes.utils.easy.EasyLog
import com.haoge.usefulcodes.utils.easy.format
import com.orhanobut.logger.Logger

/**
 * @author haoge on 2018/5/11
 */
class EasyLogActivity:BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
    }

    @OnClick(R.id.logSimple)
    fun logSimple() {
        val list = listOf(1,2,3,4)
        Logger.d(list)
//        Log.println(Log.DEBUG, "EasyLog", list.format())
//        Log.e("EasyLog", "", Exception())
        EasyLog.DEFAULT.e(list.format())
        EasyFormater.newBuilder()
    }
}