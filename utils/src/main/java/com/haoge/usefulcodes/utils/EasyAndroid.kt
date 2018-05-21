package com.haoge.usefulcodes.utils

import android.content.Context
import com.haoge.usefulcodes.utils.cache.SingleCache

/**
 * @author haoge on 2018/5/17
 */
object EasyAndroid {

    fun init(context:Context) {
        SingleCache.init(context)

    }
}

