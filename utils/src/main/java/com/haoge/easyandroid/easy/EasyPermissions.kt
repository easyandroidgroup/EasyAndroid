package com.haoge.easyandroid.easy

import android.annotation.TargetApi
import android.app.Activity
import android.app.Fragment
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper

/**
 * @author haoge on 2018/6/4
 */
class EasyPermissions private constructor(private val permissions:Array<out String>){

    private var rational:((String, RationalChain) -> Boolean)? = null
    private var callback:((Boolean) -> Unit)? = null

    /**
     * 设置权限申请说明文案。向用户展示为什么需要申请此权限
     */
    fun rational(rational:((String, RationalChain) -> Boolean)):EasyPermissions {
        this.rational = rational
        return this
    }

    /**
     * 设置授权结果回调，
     */
    fun callback(callback:((Boolean) -> Unit)): EasyPermissions {
        this.callback = callback
        return this
    }

    fun request(activity: Activity) {
        if (Build.VERSION.SDK_INT < 23) {
            callback?.invoke(true)
            return
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            requestInternal(permissions, activity)
        } else {
            mainHandler.post { requestInternal(permissions, activity) }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestInternal(permissions: Array<out String>, activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) {
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

        private val mainHandler = Handler(Looper.getMainLooper())

        @JvmStatic
        fun create(vararg permissions:String): EasyPermissions {
            return EasyPermissions(permissions)
        }
    }
}

@TargetApi(Build.VERSION_CODES.M)
class RationalChain internal constructor(private val denies: MutableIterator<String>,
                                         private val fragment: PermissionFragment,
                                         private val rational: ((String, RationalChain) -> Boolean)?,
                                         private val result: (Boolean) -> Unit) {

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