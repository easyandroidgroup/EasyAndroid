package com.haoge.sample.easyandroid.activities

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import butterknife.OnClick
import com.haoge.easyandroid.easy.EasyLog
import com.haoge.easyandroid.easy.EasyPermissions
import com.haoge.easyandroid.easy.EasyToast
import com.haoge.easyandroid.easy.PermissionAlwaysDenyNotifier
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R
import java.util.concurrent.Executors

/**
 * @author haoge on 2018/6/4
 */
class EasyPermissionsActivity:BaseActivity() {

    private val pool = Executors.newSingleThreadExecutor()
    private val callback:(Boolean) -> Unit = {grant ->
        EasyToast.DEFAULT.show("权限申请${if (grant) "成功" else "失败"}")
    }
    private val denyNotifier = DenyNotifier()

    override fun getLayoutId() = R.layout.activity_easy_permissions

    @TargetApi(Build.VERSION_CODES.M)
    override fun initPage(savedInstanceState: Bundle?) {
        super.initPage(savedInstanceState)
    }

    @OnClick(R.id.permissionSingle)
    fun permissionSingle() {
        EasyPermissions.create(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .alwaysDenyNotifier(denyNotifier)
                .callback(callback)
                .request(this)
    }

    @OnClick(R.id.permissionOnSubThread)
    fun permissionOnSubThread() {
        pool.execute {
            EasyPermissions.create(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .callback(callback)
                    .request(this)
        }
    }

    @OnClick(R.id.permissionMultiple)
    fun permissionMultiple() {
        EasyPermissions.create(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.WRITE_CONTACTS
                )
                .alwaysDenyNotifier(denyNotifier)
                .callback(callback)
                .request(this)
    }

    @OnClick(R.id.permissionWithRational)
    fun permissionWithRational() {
        EasyPermissions.create(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .alwaysDenyNotifier(denyNotifier)
                .rational { permission, chain ->
                    AlertDialog.Builder(this)
                            .setTitle("权限申请说明")
                            .setMessage("应用需要此权限：\n$permission")
                            .setNegativeButton("拒绝", {_, _ -> chain.cancel() })
                            .setPositiveButton("同意", {_, _ -> chain.process() })
                            .show()
                    return@rational true
                }.callback(callback)
                .request(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EasyLog.DEFAULT.e("PermissionFragment has destroy")
    }
}

class DenyNotifier:PermissionAlwaysDenyNotifier() {
    override fun onAlwaysDeny(permissions: Array<String>, activity: Activity) {
        val message = StringBuilder("以下部分权限已被默认拒绝，请前往设置页将其打开:\n\n")
        EasyPermissions.getPermissionGroupInfos(permissions, activity).forEach {
            message.append("${it.label} : ${it.desc} \n")
        }
        AlertDialog.Builder(activity)
                .setTitle("权限申请提醒")
                .setMessage(message)
                .setPositiveButton("确定", { _, _ ->  goSetting(activity)})
                .setNegativeButton("取消", {_,_ -> cancel(activity)})
                .show()
    }
}