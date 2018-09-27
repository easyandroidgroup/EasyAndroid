/*
 * Copyright (C) 2018 Haoge https://github.com/yjfnypeu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    // 缓存容器, 临时保存进行启动的activity和与之对应的callback实例。用于在接收回传数据时
    private val container = mutableMapOf<Activity, MutableMap<Int, (resultCode:Int, data:Intent?) -> Unit>>()
    private val codeGenerator = Random()// 用于进行requestCode自动生成的生成器
    private var lastTime = 0L

    /**
     * 使用 **context.startActivityForResult(intent, requestCode)** 进行页面启动。并绑定callback
     */
    @JvmStatic
    fun startActivity(context:Context, intent:Intent, callback:((resultCode:Int, data:Intent?) -> Unit)?) {
        startActivity(context, intent, callback, null)
    }

    /**
     * 使用 **context.startActivityForResult(intent, requestCode, options)** 进行页面启动。并绑定callback
     */
    @JvmStatic
    fun startActivity(context:Context, intent:Intent, callback:((resultCode:Int, data:Intent?) -> Unit)?, options: Bundle?) {
        val current = System.currentTimeMillis()
        val last = lastTime
        lastTime = current
        // 防暴击：两次启动间隔必须大于1秒。
        if (current - last < 1000) {
            return
        }

        if (context !is Activity || callback == null) {
            context.startActivity(intent)
        } else {
            // 自动生成有效的requestCode进行使用。
            val requestCode = codeGenerator.nextInt(0x0000FFFF)
            if (options == null || Build.VERSION.SDK_INT < 16) {
                context.startActivityForResult(intent, requestCode)
            } else {
                context.startActivityForResult(intent, requestCode, options)
            }

            // 保存回调缓存
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

        // 从缓存中取出与此activity绑定的、与requestCode相匹配的回调。进行回调通知
        container[activity]?.remove(requestCode)?.invoke(resultCode, data)

        // 清理无用的缓存条目。避免内存泄漏
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