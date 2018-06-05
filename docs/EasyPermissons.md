# EasyPermissions

EasyPermissions用于进行版本号6.0+的动态权限申请操作

## 特性

- 链式调用
- 支持定制权限申请说明弹窗
- 支持同时申请多个权限
- 多权限申请时进行去重与空过滤
- 自动使用顶层Activity执行权限请求
- 支持在任意线程进行权限申请

## 用法示例

### 权限申请

```
EasyPermissions
    // 直接指定任意多个待申请权限
    .permissions(permission1, permission2...)
    // 指定授权结果回调
    .callback{grant:Boolean ->
        if (grant) {
            // grant为true，表示所有权限均申请成功
        } else {
            // grant为false,表示至少有一条权限申请失败
        }
    }
    // 定制权限申请说明:
    // permission:
    .rational { permission:String, chain:RationalChain ->
        // TODO 在此进行此权限的说明弹窗创建。并使用chain链接后续操作
        // 当用户同意进行权限请求时：调用chain.process()
        // 当用户拒绝进行权限请求时：调用chain.cancel()

        return@rational true|false // 当需要进行弹窗通知时。需要返回true。否则返回false
    }.request()// 发起动态权限请求任务
```

### 示例说明:

```
EasyPermissions.create(
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_CALENDAR,
    Manifest.permission.WRITE_CONTACTS
)
    .rational { permission, chain ->
        AlertDialog.Builder(this)
                .setTitle("权限申请说明")
                .setMessage("应用需要此权限：\n$permission")
                .setNegativeButton("拒绝", {_, _ -> chain.cancel() })
                .setPositiveButton("同意", {_, _ -> chain.process() })
                .show()

        return@rational true
    }.callback { grant ->
        EasyToast.DEFAULT.show("权限申请${if (grant) "成功" else "失败"}")
    }
    .request()
```



