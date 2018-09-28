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
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.text.Html
import android.util.Log
import android.widget.TextView
import com.bumptech.glide.Glide
import com.haoge.easyandroid.EasyAndroid
import java.lang.ref.WeakReference

private typealias ImageGetterLoader = (String) -> Drawable?
class EasyImageGetter:Html.ImageGetter {

    private var loader:ImageGetterLoader? = null
    private var placeHolder:Drawable? = null
    private var error:Drawable? = null
    private var container:TextView? = null
    private var tagHandler:Html.TagHandler? = null

    /** 设置图片在加载时的占位图*/
    @Suppress("DEPRECATION")
    fun setPlaceHolder(placeHolder:Int): EasyImageGetter {
        this.placeHolder = EasyAndroid.getApplicationContext().resources.getDrawable(placeHolder)
        return this
    }

    /** 设置当图片加载失败时的占位图*/
    @Suppress("DEPRECATION")
    fun setError(error:Int): EasyImageGetter {
        this.error = EasyAndroid.getApplicationContext().resources.getDrawable(error)
        return this
    }

    /**
     * 设置额外的加载器。将src数据解析为drawable进行展示。
     *
     * 在解析过程中。若有设置此加载器，则将首先通过此加载器进行drawable创建。
     *
     * 当此加载器所返回的drawable为null时。则将触发组件本身自带的加载方式(通过glide进行加载)
     */
    fun setLoader(loader: ImageGetterLoader): EasyImageGetter {
        this.loader = loader
        return this
    }

    /** 参考[Html.TagHandler]*/
    fun setTagHandler(handler: Html.TagHandler): EasyImageGetter {
        this.tagHandler = handler
        return this
    }

    override fun getDrawable(source: String?): Drawable {
        val drawable = FutureDrawable(placeHolder)
        InternalAsyncTask(error, drawable, loader, WeakReference<TextView>(container)).execute(source)
        return drawable
    }

    fun loadHtml(html:String, container: TextView) {
        this.container = container

        val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY, this, tagHandler)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html, this, tagHandler)
        }

        container.text = spanned
    }

    companion object {
        fun create():EasyImageGetter = EasyImageGetter()

        // 判断执行环境中是否包含有glide图片解析库
        private val glideSupport = exist("com.bumptech.glide.Glide")
        private fun exist(name:String):Boolean = try{
            Class.forName(name)
            true
        } catch (e:Exception) {
            false
        }
    }

    class InternalAsyncTask(private val error: Drawable?,
                            private val drawable: FutureDrawable,
                            private val loader: ImageGetterLoader?,
                            private var container: WeakReference<TextView>): AsyncTask<String, Int, Drawable>() {

        override fun doInBackground(vararg params: String?): Drawable? {
            try {
                if (checkContextValid().not()) return null

                val url = params[0]?:throw RuntimeException("URL is null")
                // 先使用用户设置的loader进行加载
                try {
                    val result = loader?.invoke(url)
                    if (result != null) return result
                } catch (e:Exception) {
                    Log.e("EasyImageGetter", "A error occurs with loader:[${loader?.javaClass?.canonicalName}]", e)
                }

                // 当用户设置的loader加载失败时(返回null), 则使用内部机制进行drawable获取
                if (glideSupport.not()) throw RuntimeException("Internal loader requires glide to load drawable!")
                val context = container.get()?.context?:throw RuntimeException("Fetch context failed from container")
                return Glide.with(context).load(url).submit().get()
            } catch (e:Exception) {
                Log.e("EasyImageGetter", "A error occurs with internal loader", e)
                return null
            }
        }

        override fun onPostExecute(result: Drawable?) {
            if (checkContextValid().not()) return
            val textView = container.get()?:return

            val real = result?:error
            real?.let { drawable.apply(it) }
            textView.invalidate()
            textView.text = textView.text
        }

        // 从绑定的TextView中获取activity实例进行生命周期判断。避免页面销毁后还占用大量资源去进行load
        private fun checkContextValid():Boolean {
            val activity = (container.get()?.context?:return false) as? Activity ?: return false
            return !(activity.isFinishing || (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed))
        }
    }

    // 用于进行异步加载Drawable时的占位Drawable：
    @Suppress("DEPRECATION")
    class FutureDrawable(private val placeholder:Drawable?): BitmapDrawable() {

        private var drawable:Drawable? = null

        private fun setBounds(drawable:Drawable) {
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        }

        fun apply(drawable: Drawable) {
            this.drawable = drawable
            setBounds(drawable)
        }

        override fun draw(canvas: Canvas?) {
            val real = drawable?:placeholder
            real?.draw(canvas)
        }

        init {
            placeholder?.let { setBounds(it) }
        }
    }
}

