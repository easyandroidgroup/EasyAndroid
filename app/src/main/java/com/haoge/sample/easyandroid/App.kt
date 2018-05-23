package com.haoge.sample.easyandroid

import android.app.Application
import com.haoge.easyandroid.EasyAndroid

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        EasyAndroid.init(this)
    }
}
