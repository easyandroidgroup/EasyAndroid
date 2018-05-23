package com.haoge.easyandroid

import android.content.Context
import com.haoge.easyandroid.cache.SingleCache
import com.haoge.easyandroid.tools.ActivityStack

/**
 * @author haoge on 2018/5/17
 */
object EasyAndroid {

    fun init(context:Context) {
        SingleCache.init(context)
        ActivityStack.registerCallback(context)
    }
}