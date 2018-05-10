package com.haoge.usefulcodes

import android.app.Application
import com.haoge.usefulcodes.utils.cache.SingleCache
import com.haoge.usefulcodes.utils.components.ActivityStack

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SingleCache.init(this)
        ActivityStack.registerCallback(this)
    }
}
