package com.haoge.easyandroid.easy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import java.util.*

/**
 * 启动Activity并接收onActivityResult进行派发处理。提升activity result可维护性
 * @author haoge on 2018/6/1
 */
object EasyActivityResult {

    private val container = mutableMapOf<Activity, MutableMap<Int, (resultCode:Int, data:Intent?) -> Unit>>()
    private val codeGenerator = Random()
    private var lastTime = 0L

    @JvmStatic
    fun startActivity(context:Context, intent:Intent, callback:((resultCode:Int, data:Intent?) -> Unit)?) {
        startActivity(context, intent, callback, null)
    }

    @JvmStatic
    fun startActivity(context:Context, intent:Intent, callback:((resultCode:Int, data:Intent?) -> Unit)?, options: Bundle?) {
        val current = System.currentTimeMillis()
        val last = lastTime
        lastTime = current
        // 防暴击：两次启动间隔必须大于1秒。
        if (current - last < 1000) {
            return
        }

        if (context !is Activity) {
            context.startActivity(intent)
        } else {
            val requestCode = codeGenerator.nextInt(0x0000FFFF)
            if (options == null || Build.VERSION.SDK_INT < 16) {
                context.startActivityForResult(intent, requestCode)
            } else {
                context.startActivityForResult(intent, requestCode, options)
            }

            if (callback == null) {
                return
            }

            if (container.containsKey(context)) {
                container[context]?.put(requestCode, callback)
            } else {
                container[context] = mutableMapOf(Pair(requestCode, callback))
            }
        }
    }

    @JvmStatic
    fun dispatch(activity:Activity, requestCode:Int, resultCode:Int, data: Intent?){
        if (!container.containsKey(activity)) {
            return
        }

        container[activity]?.remove(requestCode)?.invoke(resultCode, data)

        releaseInvalidItems()
    }

    private fun releaseInvalidItems() {
        val keys = container.keys
        val iterator = keys.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.isFinishing
                    || container[next]?.isEmpty() == true
                    || Build.VERSION.SDK_INT >= 17 && next.isDestroyed) {
                iterator.remove()
            }
        }
    }
}