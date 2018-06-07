package com.haoge.easyandroid

import android.annotation.SuppressLint
import android.content.Context
import com.haoge.easyandroid.tools.ActivityStack

/**
 * @author haoge on 2018/5/17
 */
@SuppressLint("StaticFieldLeak")
object EasyAndroid {

    /**
     * 提供给框架内部使用的常量。判断是否是debug|release包
     *
     * 其为**BuildConfig.DEBUG**的值
     */
    internal val DEBUG by lazy {
        return@lazy try {
            val clazz = Class.forName(context!!.packageName + ".BuildConfig")
            val field = clazz.getDeclaredField("DEBUG")
            field.get(clazz) as Boolean
        } catch (e:Exception) {
            false
        }
    }

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
        ActivityStack.registerCallback(this.context)
    }
}