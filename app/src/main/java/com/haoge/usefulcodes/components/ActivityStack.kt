package com.haoge.usefulcodes.components

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import java.util.*

/**
 * 利用系统本身提供的回调作自动Activity栈管理。
 * @author haoge
 */
object ActivityStack {
    private var application: Application? = null
    private val stack:LinkedList<Activity> = LinkedList()

    @Suppress("UNCHECKED_CAST")
    fun <T:Activity> top():T? = if (stack.isEmpty()) null else stack.last as T

    fun registerCallback(context: Context?) {
        if (application != null || context == null) {
            return
        }

        application = context.applicationContext as Application
        application!!.registerActivityLifecycleCallbacks(Callback())
    }

    private class Callback : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) {
            if (!ActivityStack.stack.contains(activity)) {
                ActivityStack.stack.push(activity)
            }
        }

        override fun onActivityStarted(activity: Activity) { }

        override fun onActivityResumed(activity: Activity) { }

        override fun onActivityPaused(activity: Activity) { }

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (ActivityStack.stack.contains(activity)) {
                ActivityStack.stack.remove(activity)
            }
        }
    }
}
