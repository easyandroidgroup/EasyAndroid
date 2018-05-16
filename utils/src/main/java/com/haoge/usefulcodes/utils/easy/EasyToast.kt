package com.haoge.usefulcodes.utils.easy

import android.annotation.SuppressLint
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.haoge.usefulcodes.utils.cache.SingleCache

/**
 * 一个简单易用的Toast封装类。用于提供易用的、多样式的Toast组件进行使用
 *
 * DATE: 2018/5/9
 *
 * AUTHOR: haoge
 */
class EasyToast private constructor(private val toast: Toast, private val tv: TextView?, private val isDefault: Boolean) {

    fun show(resId:Int) {
        show(SingleCache.getApplicationContext().getString(resId))
    }

    fun show(message:String, vararg any: Any) {
        if (TextUtils.isEmpty(message)) {
            return
        }

        var result = message
        if (any.isNotEmpty()) {
            result = String.format(message, any)
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            showInternal(result)
        } else {
            SingleCache.mainHandler.post { showInternal(result) }
        }
    }

    private fun showInternal(message: String) {
        if (isDefault) {
            toast.setText(message)
            toast.show()
        } else {
            tv?.text = message
            toast.show()
        }
    }

    companion object {
        /**
         * 默认提供的Toast实例，在首次使用时进行加载。
         */
        val DEFAULT: EasyToast by lazy { default() }

        private fun default(): EasyToast {
            checkThread()
            @SuppressLint("ShowToast")
            val toast = Toast.makeText(SingleCache.getApplicationContext(), "", Toast.LENGTH_SHORT)
            return EasyToast(toast, null, true)
        }

        fun create(layoutId: Int, tvId: Int, duration: Int): EasyToast {
            checkThread()
            val container = LayoutInflater.from(SingleCache.getApplicationContext()).inflate(layoutId, null)
            val tv:TextView = container.findViewById(tvId)
            val toast = Toast(SingleCache.getApplicationContext())
            toast.view = container
            toast.duration = duration
            return EasyToast(toast, tv, false)
        }

        // Toast限制：在执行了Looper.prepare()的线程中才能创建Toast实例
        // 这里加强限制，仅限主线程创建
        private fun checkThread() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                throw RuntimeException("the toast-create method must called on main-thread")
            }
        }
    }
}