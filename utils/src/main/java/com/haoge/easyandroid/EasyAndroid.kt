package com.haoge.easyandroid

import android.content.Context
import com.haoge.easyandroid.cache.SingleCache

/**
 * @author haoge on 2018/5/17
 */
object EasyAndroid {

    fun init(context:Context) {
        SingleCache.init(context)

    }
}

