package com.haoge.easyandroid.cache

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.haoge.easyandroid.components.ActivityStack


/**
 * 提供一些全局的常用数据进行使用。
 *
 * DATE: 2018/5/9
 *
 * AUTHOR: haoge
 */
@SuppressLint("StaticFieldLeak")
object SingleCache {
    private var context:Context? = null
    var mainHandler: Handler = Handler(Looper.getMainLooper())

    internal fun init(context: Context) {
        if (SingleCache.context == null) {
            SingleCache.context = context.applicationContext
            ActivityStack.registerCallback(SingleCache.context)
        }
    }

    fun getApplicationContext():Context {
        if (context == null) {
            throw RuntimeException("Please call [EasyAndroid.init(context)] first")
        } else {
            return context as Context
        }
    }
}