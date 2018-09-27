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
package com.haoge.easyandroid

import android.annotation.SuppressLint
import android.content.Context

/**
 * @author haoge on 2018/5/17
 */
@SuppressLint("StaticFieldLeak")
object EasyAndroid {

    private var context:Context? = null

    @JvmStatic
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