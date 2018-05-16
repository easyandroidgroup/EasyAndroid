package com.haoge.usefulcodes.utils

import android.app.Dialog
import com.haoge.usefulcodes.utils.easy.EasyFormatter
import com.haoge.usefulcodes.utils.safe.SafeDialogHandle

fun Dialog?.safeShow() = SafeDialogHandle.safeShowDialog(this)
fun Dialog?.safeDismiss() = SafeDialogHandle.safeDismissDialog(this)
fun Any?.easyFormat():String = EasyFormatter.DEFAULT.format(this)