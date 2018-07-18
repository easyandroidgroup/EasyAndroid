package com.haoge.easyandroid.mvp

import android.app.Activity

/**
 * View层：基础通信协议接口
 * @author haoge on 2018/5/29
 */
interface MVPView {
    fun getHostActivity():Activity
    fun showLoadingDialog()
    fun hideLoadingDialog()
    fun toastMessage(message:String)
    fun toastMessage(resId:Int)
}