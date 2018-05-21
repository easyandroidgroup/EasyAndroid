package com.haoge.easyandroid

import android.app.Dialog
import com.haoge.easyandroid.easy.EasyFormatter
import com.haoge.easyandroid.safe.SafeDialogHandle

fun Dialog?.safeShow() = SafeDialogHandle.safeShowDialog(this)
fun Dialog?.safeDismiss() = SafeDialogHandle.safeDismissDialog(this)
fun Any?.easyFormat():String = EasyFormatter.DEFAULT.format(this)