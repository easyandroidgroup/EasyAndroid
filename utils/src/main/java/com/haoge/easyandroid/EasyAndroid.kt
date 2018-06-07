package com.haoge.easyandroid

import android.annotation.SuppressLint
import android.content.Context

/**
 * @author haoge on 2018/5/17
 */
@SuppressLint("StaticFieldLeak")
object EasyAndroid {

    private var context:Context? = null

    fun getApplicationContext():Context {
        if (context == null) {
            throw RuntimeException("Please call [EasyAndroid.init(context)] first")
        } else {
            return context as Context
        }
    }

    @JvmStatic
    fun init(context:Context) {
        if (this.context != null) return

        this.context = context.applicationContext
    }
}