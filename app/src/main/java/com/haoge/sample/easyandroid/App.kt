package com.haoge.sample.easyandroid

import android.app.Application
import com.haoge.easyandroid.EasyAndroid
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        EasyAndroid.init(this)
        Logger.addLogAdapter(AndroidLogAdapter())
    }
}
