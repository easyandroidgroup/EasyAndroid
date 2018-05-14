package com.haoge.usefulcodes

import android.app.Application
import com.haoge.usefulcodes.utils.cache.SingleCache
import com.haoge.usefulcodes.utils.components.ActivityStack
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SingleCache.init(this)
        ActivityStack.registerCallback(this)
        Logger.addLogAdapter(AndroidLogAdapter())
    }
}
