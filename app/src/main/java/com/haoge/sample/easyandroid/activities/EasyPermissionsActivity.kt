package com.haoge.sample.easyandroid.activities

import android.Manifest
import android.annotation.TargetApi
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import butterknife.OnClick
import com.haoge.easyandroid.easy.EasyPermissions
import com.haoge.easyandroid.easy.EasyToast
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

    override fun getLayoutId() = R.layout.activity_easy_permissions

    @TargetApi(Build.VERSION_CODES.M)
    override fun initPage(savedInstanceState: Bundle?) {
        super.initPage(savedInstanceState)
    }

    @OnClick(R.id.permissionSingle)
    fun permissionSingle() {
        EasyPermissions.create(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .callback(callback)
                .request()
    }

    @OnClick(R.id.permissionOnSubThread)
    fun permissionOnSubThread() {
        pool.execute {
            EasyPermissions.create(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .callback(callback)
                    .request()
        }
    }

    @OnClick(R.id.permissionMultiple)
    fun permissionMultiple() {
        EasyPermissions.create(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.WRITE_CONTACTS
                ).callback(callback).request()
    }

    @OnClick(R.id.permissionWithRational)
    fun permissionWithRational() {
        EasyPermissions.create(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .rational { permission, chain ->
                    AlertDialog.Builder(this)
                            .setTitle("权限申请说明")
                            .setMessage("应用需要此权限：\n$permission")
                            .setNegativeButton("拒绝", {_, _ -> chain.cancel() })
                            .setPositiveButton("同意", {_, _ -> chain.process() })
                            .show()
                    return@rational true
                }.callback(callback)
                .request()
    }

}