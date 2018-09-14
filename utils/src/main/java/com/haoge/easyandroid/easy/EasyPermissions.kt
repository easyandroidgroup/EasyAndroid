package com.haoge.easyandroid.easy

import android.annotation.TargetApi
import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings

/**
 * @author haoge on 2018/6/4
 */
private typealias PermissionRational = (String, RationalChain) -> Boolean
private typealias PermissionCallback = (Boolean) -> Unit
private typealias PermissionDenied   = PermissionAlwaysDenyNotifier
class EasyPermissions private constructor(private val permissions:Array<out String>){

    private var rational:PermissionRational? = null
    private var callback:PermissionCallback? = null
    private var denied:PermissionDenied? = null

    /**
     * 设置权限申请说明文案。向用户展示为什么需要申请此权限
     */
    fun rational(rational:PermissionRational?):EasyPermissions {
        this.rational = rational
        return this
    }

    /**
     * 当请求的权限中，有被默认拒绝的权限时，将会通知待此[denied]通知器中。
     *
     * 可在此提醒用户前往权限设置。打开权限。
     */
    fun alwaysDenyNotifier(denied:PermissionDenied?):EasyPermissions {
        this.denied = denied
        return this
    }

    /**
     * 设置授权结果回调，
     */
    fun callback(callback:PermissionCallback?): EasyPermissions {
        this.callback = callback
        return this
    }

    /**
     * 启动动态权限请求
     */
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
                fragment.requestPermissions(denies, callback, denied, this)
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

        fun getPermissionGroupInfos(permissions: Array<String>, context: Context):List<PermissionGroupInfo> {
            val names = mutableListOf<String>()// 进行过滤去重处理的临时变量
            val groups = mutableListOf<PermissionGroupInfo>()
            for (permission in permissions) {
                val info = context.packageManager.getPermissionInfo(permission, PackageManager.GET_META_DATA)
                if (names.contains(info.group)) continue
                names.add(info.group)

                val groupInfo = context.packageManager.getPermissionGroupInfo(info.group, PackageManager.GET_META_DATA)
                groups.add(PermissionGroupInfo(
                        context.resources.getString(groupInfo.labelRes),
                        context.resources.getString(groupInfo.descriptionRes)
                ))
            }
            return groups
        }

        fun getPermissionInfos(permissions: Array<String>, context: Context):List<PermissionInfo> {
            val infos = mutableListOf<PermissionInfo>()
            val names = mutableListOf<String>()
            for (permission in permissions) {
                val info = context.packageManager.getPermissionInfo(permission, PackageManager.GET_META_DATA)
                if (names.contains(info.name)) continue
                names.add(info.name)

                infos.add(PermissionInfo(
                        context.resources.getString(info.labelRes),
                        context.resources.getString(info.descriptionRes)
                ))
            }
            return infos
        }
    }
}

class PermissionGroupInfo(val label:String,
                          val desc:String)

class PermissionInfo(val label:String,
                     val desc: String)

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

    private fun noticeAccept(accept:Boolean) {
        result.invoke(accept)
    }
}

// 用于进行权限请求的Fragment
class PermissionFragment: Fragment() {

    private var callback:PermissionCallback? = null
    private var denied:PermissionDenied? = null
    private var request:EasyPermissions? = null

    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissions(denies:List<String>,
                           callback:PermissionCallback?,
                           denied:PermissionDenied?,
                           request:EasyPermissions) {
        this.callback = callback
        this.denied = denied
        this.request = request
        requestPermissions(denies.toTypedArray(), REQUEST_CODE_PERMISSION_GRANT)
    }

    fun goSetting(intent: Intent) {
        startActivityForResult(intent, REQUEST_CODE_RESULT_SETTING)
    }

    fun cancel() {
        callback?.invoke(false)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CODE_PERMISSION_GRANT) return

        val denies = mutableListOf<String>()
        // 存储拒绝授权权限的容器。
        grantResults.forEachIndexed { index, grant ->
            if (grant == PackageManager.PERMISSION_DENIED) {
                denies.add(permissions[index])
            }
        }

        if (denies.isNotEmpty() && denied != null) {
            // 当存在被拒绝的权限且通知器不为null。检查是否存在默认拒绝的权限并处理
            for (deny in denies) {
                if (shouldShowRequestPermissionRationale(deny).not()) {
                    denied?.onAlwaysDeny(denies.toTypedArray(), activity)
                    return
                }
            }
        }

        callback?.invoke(denies.isEmpty())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_RESULT_SETTING) return
        // 从设置页回来，重启权限申请
        request?.request(activity)
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
        private const val REQUEST_CODE_PERMISSION_GRANT = 23742
        private const val REQUEST_CODE_RESULT_SETTING = 11432
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

    override fun onDestroy() {
        super.onDestroy()
        EasyLog.DEFAULT.e("PermissionFragment has destroy")
    }
}

abstract class PermissionAlwaysDenyNotifier{

    /**
     * 当申请的权限中有被默认拒绝的时候，通知到此方法。[permissions]中存在所有的**被默认拒绝**的权限
     */
    abstract fun onAlwaysDeny(permissions: Array<String>, activity: Activity)
    /**
     * 提供默认的intent。跳转到应用详情页。需要进行各种机型适配跳转的。可以复写此方法提供具体的Intent即可
     */
    protected open fun createIntent(activity: Activity) =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
    /** 跳转到setting页*/
    fun goSetting(activity: Activity) = PermissionFragment.findOrCreate(activity).goSetting(createIntent(activity))
    /** 取消*/
    fun cancel(activity: Activity) = PermissionFragment.findOrCreate(activity).cancel()
}
