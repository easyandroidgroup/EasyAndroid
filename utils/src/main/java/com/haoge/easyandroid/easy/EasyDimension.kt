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

import android.util.DisplayMetrics
import android.util.TypedValue
import com.haoge.easyandroid.EasyAndroid

/**
 * 尺寸数值转换器。用于方便的在不同的尺寸单位之间进行转换
 * @author haoge on 2018/5/10
 */
class EasyDimension private constructor(private val pixel:Float, private val metrics: DisplayMetrics){

    fun toPX() = pixel

    fun toDIP() = pixel / metrics.density

    fun toSP() = pixel / metrics.scaledDensity

    fun toPT() = pixel / metrics.xdpi / (1.0f / 72)

    fun toIN() = pixel / metrics.xdpi

    fun toMM() = pixel / metrics.xdpi / (1.0f / 25.4f)

    companion object {
        @JvmStatic
        fun create(value:Float, unit:Int): EasyDimension {
            val metrics = EasyAndroid.getApplicationContext().resources.displayMetrics
            return EasyDimension(TypedValue.applyDimension(unit, value, metrics), metrics!!)
        }

        @JvmStatic
        fun withPX(value:Float) = create(value, TypedValue.COMPLEX_UNIT_PX)

        @JvmStatic
        fun withDIP(value:Float) = create(value, TypedValue.COMPLEX_UNIT_DIP)

        @JvmStatic
        fun withSP(value:Float) = create(value, TypedValue.COMPLEX_UNIT_SP)

        @JvmStatic
        fun withPT(value:Float) = create(value, TypedValue.COMPLEX_UNIT_PT)

        @JvmStatic
        fun withIN(value:Float) = create(value, TypedValue.COMPLEX_UNIT_IN)

        @JvmStatic
        fun withMM(value:Float) = create(value, TypedValue.COMPLEX_UNIT_MM)
    }
}