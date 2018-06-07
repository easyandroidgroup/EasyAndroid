package com.haoge.easyandroid.easy

import android.annotation.TargetApi
import android.app.Activity
import android.app.Fragment
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.haoge.easyandroid.tools.ActivityStack

/**
 * @author haoge on 2018/6/4
 */
class EasyPermissions private constructor(val permissions:Array<out String>){

    internal var rational:((String, RationalChain) -> Boolean)? = null
    internal var callback:((Boolean) -> Unit)? = null

    /**
     * 设置权限申请说明文案。向用户展示为什么需要申请此权限
     *
     * 闭包参数为待申请的权限名。需要返回说明文案。或者返回null:代表不进行文案说明提示
     */
    fun rational(rational:((String, RationalChain) -> Boolean)):EasyPermissions {
        this.rational = rational
        return this
    }

    /**
     * 设置授权结果回调，
     *
     * 闭包参数为是否授权成功。boolean类型
     */
    fun callback(callback:((Boolean) -> Unit)): EasyPermissions {
        this.callback = callback
        return this
    }

    fun request() {
        if (Build.VERSION.SDK_INT < 23) {
            callback?.invoke(true)
            return
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            requestInternal(permissions)
        } else {
            mainHandler.post { requestInternal(permissions) }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    internal fun requestInternal(permissions: Array<out String>) {
        val activity = ActivityStack.top<Activity>()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            callback?.invoke(false)
            return
        }

        val fragment = PermissionFragment.findOrCreate(activity)

        val denies = mutableListOf<String>()
        for (permission in permissions) {
            if (fragment.isGrant(permission)) {
                continue
            }
            // 过滤重复权限
            denies.remove(permission)
            denies.add(permission)
        }

        if (denies.isEmpty()) {
            callback?.invoke(true)
            return
        }

        RationalChain(denies.iterator(), fragment, rational, { accept:Boolean ->
            if (accept) {
                fragment.requestPermissions(denies, callback)
            } else {
                callback?.invoke(false)
            }
        }).process()
    }

    companion object {

        internal val mainHandler by lazy { return@lazy Handler(Looper.getMainLooper()) }

        @JvmStatic
        fun create(vararg permissions:String): EasyPermissions {
            return EasyPermissions(permissions)
        }
    }
}

@TargetApi(Build.VERSION_CODES.M)
class RationalChain internal constructor(internal val denies: MutableIterator<String>,
                                         internal val fragment: PermissionFragment,
                                         internal val rational: ((String, RationalChain) -> Boolean)?,
                                         internal val result: (Boolean) -> Unit) {

    fun process() {
        // 到底了。用户未拦截
        if (!denies.hasNext()) {
            noticeAccept(true)
            return
        }

        val deny = denies.next()
        val next = RationalChain(denies, fragment, rational, result)
        // 不要求显示提示文案。直接请求下次
        if (!fragment.shouldShowRequestPermissionRationale(deny)) {
            next.process()
            return
        }

        if (rational == null || !rational.invoke(deny, next)) {
            next.process()
        }
    }

    fun cancel() {
        noticeAccept(false)
    }

    internal fun noticeAccept(accept:Boolean) {
        result.invoke(accept)
    }
}

// 用于进行权限请求的Fragment
class PermissionFragment: Fragment() {

    internal var callback:((Boolean) -> Unit)? = null

    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissions(denies:List<String>, callback:((Boolean) -> Unit)?) {
        this.callback = callback
        requestPermissions(denies.toTypedArray(), REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CODE) return

        if (permissions.isEmpty()) {
            callback?.invoke(false)
            return
        }

        for (grant in grantResults) {
            if (grant == PackageManager.PERMISSION_DENIED) {
                callback?.invoke(false)
                return
            }
        }

        callback?.invoke(true)
    }

    fun isGrant(permission:String):Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                || activity.packageManager.isPermissionRevokedByPolicy(permission, activity.packageName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    companion object {
        private const val REQUEST_CODE = 25
        private const val TAG = "EasyPermission:PermissionFragment"

        @JvmStatic
        fun findOrCreate(activity: Activity):PermissionFragment {
            var fragment:PermissionFragment? = activity.fragmentManager.findFragmentByTag(TAG) as PermissionFragment?
            if (fragment == null) {
                fragment = PermissionFragment()
                activity.fragmentManager.beginTransaction()
                        .add(fragment, TAG)
                        .commitAllowingStateLoss()
                activity.fragmentManager.executePendingTransactions()
            }
            return fragment
        }

    }
}