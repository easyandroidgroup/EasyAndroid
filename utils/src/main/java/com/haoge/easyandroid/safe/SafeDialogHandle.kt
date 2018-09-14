package com.haoge.easyandroid.safe

import android.app.Activity
import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ContextThemeWrapper

/**
 * 对Dialog的show/dismiss操作进行封装，避免出现weakWindowToken问题。
 *
 * DATE: 2018/5/9
 *
 * AUTHOR: haoge
 */
object SafeDialogHandle {

    private val mainHandler = Handler(Looper.getMainLooper())

    fun safeShowDialog(dialog: Dialog?) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            safeShowDialogOnMainThread(dialog)
            return
        }

        mainHandler.post { safeShowDialogOnMainThread(dialog) }
    }

    private fun safeShowDialogOnMainThread(dialog: Dialog?) {
        if (dialog == null || dialog.isShowing) {
            return
        }
        val bindAct = getActivity(dialog)

        if (bindAct == null || bindAct.isFinishing) {
            Log.d("Dialog shown failed:", "The Dialog bind's Activity was recycled or finished!")
            return
        }

        dialog.show()
    }

    private fun getActivity(dialog: Dialog): Activity? {
        var bindAct: Activity? = null
        var context = dialog.context
        do {
            if (context is Activity) {
                bindAct = context
                break
            } else if (context is ContextThemeWrapper) {
                context = context.baseContext
            } else {
                break
            }
        } while (true)
        return bindAct
    }

    fun safeDismissDialog(dialog: Dialog?) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            safeDismissDialogOnMainThread(dialog)
            return
        }

        mainHandler.post { safeDismissDialogOnMainThread(dialog) }
    }

    private fun safeDismissDialogOnMainThread(dialog: Dialog?) {
        if (dialog == null || !dialog.isShowing) {
            return
        }

        val bindAct = getActivity(dialog)
        if (bindAct != null && !bindAct.isFinishing) {
            dialog.dismiss()
        }
    }
}
