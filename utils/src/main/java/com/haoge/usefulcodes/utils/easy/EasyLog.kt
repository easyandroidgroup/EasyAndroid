package com.haoge.usefulcodes.utils.easy

import android.util.Log

/**
 * @author haoge on 2018/5/10
 */
object EasyLog {

    fun d(message:String, vararg args:Any?) {
        val result = String.format(message, args)
        val trace = Exception().stackTrace[1]
        Log.e(trace.fileName, "onCreate: (DemosActivity.java:31)")
    }

}

data class LogInfo(var message:String, val trace: StackTraceElement)