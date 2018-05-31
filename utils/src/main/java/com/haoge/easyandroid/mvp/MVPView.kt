package com.haoge.easyandroid.mvp

import android.app.Activity

/**
 * View层：定义
 * @author haoge on 2018/5/29
 */
interface MVPView {
    fun getActivity():Activity
    fun showLoadingDialog()
    fun hideLoadingDialog()
    fun toastMessage(message:String)
    fun toastMessage(resId:Int)
}