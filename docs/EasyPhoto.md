# EasyPhoto
------
EasyPhoto是开源库[EasyAndroid](https://github.com/yjfnypeu/EasyAndroid)的基础组件之一，主要应用情景在于更换头像。

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyPhotoActivity.kt)

### 特性

 * 支持链式调用
 * 支持图片指定宽高、指定宽高比例进行裁剪
 * 支持图片输出到指定地址
 * 支持任意线程选择进行图片选择
 
### 流程图
------

下图为EasyPhoto的执行流程：

![](https://user-gold-cdn.xitu.io/2018/8/22/165611440c4e9867?w=659&h=759&f=png&s=30622)


**再来一个效果图：**

![](https://user-gold-cdn.xitu.io/2018/8/22/1656115feb822b03?w=340&h=654&f=gif&s=4629676)

### 用法举例
------

**请注意！EasyPhoto本身并不具备动态请求权限的功能。所以在调用之前，请一定注意先申请必要权限：**

此处所用的动态权限申请库为集成组件中的EasyPermissions。当然你也可以选择使用其他的权限库。

```
//拒绝权限将无法使用
EasyPermissions.create(
		Manifest.permission.WRITE_EXTERNAL_STORAGE,
		Manifest.permission.READ_EXTERNAL_STORAGE,
		Manifest.permission.CAMERA)
	.callback { grant ->
		//拒绝直接关闭
		if(!grant){
		   finish()
		} 
	}.request(activity)
```

**重要的说三遍：运行时权限 运行时权限 运行时权限**

#### 完整调用api展示

```
val photo = EasyPhoto()// 创建EasyPhoto实例
    // 是否需要进行裁剪
    .setCrop(true|false)
    // 指定创建的图片地址。
    .setImgPath(imgPath:String)

// 通过设置回调，获取选择到的文件
photo.setCallback { file:File ->
    // TODO 使用选择的文件进行操作
}

// 当然。不排除出现非预期的问题。所以也提供了错误回调
photo.setError { error:Exception ->
    // TODO
}

// 跳转拍照并获取图片
photo.takePhoto(activity)

// 或者跳转图库进行图片选择
photo.selectPhoto(activity)
```


请注意：启动拍照或图库选择时。传入的activity需要为当前正在展示的页面的activity实例！