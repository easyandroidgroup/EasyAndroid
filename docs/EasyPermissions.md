# EasyPermissions

EasyPermissions用于进行版本号6.0+的动态权限申请操作

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyPermissionsActivity.kt)


## 特性

- api链式调用，调用链更丝滑~
- 支持定制权限申请说明弹窗
- 支持同时申请多个权限
- 多权限申请时进行去重
- 自动使用顶层Activity执行权限请求
- 支持在任意线程进行权限申请

## 流程图

下面的图为通用的动态权限申请流程图。EasyPermissions的执行流程也是与此一致的

![](https://user-gold-cdn.xitu.io/2018/6/8/163de22dd83e89d4?w=1738&h=1552&f=png&s=320955)

## 用法举例

### 1. 申请写入联系人权限：(单一权限申请)

```
EasyPermissions.create(Manifest.permissions.WRITE_CONTACTS)
	.request(activity)
```

**PS: 请注意此处的request方法传入的Activity，需要为当前顶层的Activity实例，否则将可能导致无法接收权限返回信息的问题**

### 2. 同时申请多个权限

```
EasyPermissions.create(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.WRITE_CONTACTS
                ).request(this)
```

### 3. 接收权限申请结果

```
EasyPermissions.create(permission1, permission2 ... permissionN)
		.callback {grant:Boolean -> // grant为true表示所有权限均申请成功}
		.request(activity)
```

### 4. 定制权限申请说明弹窗

`权限申请说明`部分的流程为上方流程图中的Rational部分。这部分流程系统只提供了`shouldShowRequestPermissionRationale`方法提示开发者：**这里需要向用户展示申请此权限的原因，以达到更好的用户体验**。

所以，EasyPermissions也对应提供了rational方法，进行方便的创建说明提醒：

```
EasyPermissions.create(permissions)
	.retional {
		permission:String, // 需要进行用户提示的权限
		chain: RationalChain -> // 内部API。若需要提示时，则需要使用此链表在用户操作后接入后续流程
		// 返回true。表示此permission权限将会进行提醒说明,
		// 先暂时对权限申请流程进行阻塞，待后续用户操作后，通过chain实例进行流程唤醒
		return@rational true|false
	}
	.request(activity)
```

举个具体例子

```
EasyPermissions.create(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    .rational { permission, chain ->
        AlertDialog.Builder(this)
                .setTitle("权限申请说明")
                .setMessage("应用需要此权限：\n$permission")
                .setNegativeButton("拒绝", {_, _ -> chain.cancel()// 通知用户拒绝 })
                .setPositiveButton("同意", {_, _ -> chain.process()// 用户同意，继续流程 })
                .show()
        return@rational true
    }.callback(callback)
    .request(this)
```

### 5. 定制权限默认拒绝时的通知说明

当申请的权限中。有`被默认拒绝`的权限时。此时需要提醒用户手动去权限管理页。主动将权限打开后再进行操作

所以，EasyPermission提供了`alwaysDenyNotifier`方法. 便于在需要的时候进行提醒：

```
class DenyNotifier:PermissionAlwaysDenyNotifier() {
    override fun onAlwaysDeny(permissions: Array<String>, activity: Activity) {
        val message = StringBuilder("以下部分权限已被默认拒绝，请前往设置页将其打开:\n\n")
        EasyPermissions.getPermissionGroupInfos(permissions, activity).forEach {
            message.append("${it.label} : ${it.desc} \n")
        }
        AlertDialog.Builder(activity)
                .setTitle("权限申请提醒")
                .setMessage(message)
                .setPositiveButton("确定", {_,_-> goSetting(activity)// 前往设置页})
                .setNegativeButton("取消", {_,_-> cancel(activity)// 取消当前的权限请求任务})
                .show()
    }

    override fun createIntent(activity: Activity): Intent {
        // EasyPermissions本身并没做不同厂商的`跳转权限设置页`的针对性适配。
        // 所以如果需要的话。可以通过复写此方法。主动的去针对性适配
    }
}

EasyPermissions.create(permissions)
    .alwaysDenyNotifier(DenyNotifier())
    .request(activity)
```

