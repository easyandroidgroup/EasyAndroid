# EasyActivityResult

EasyActivityResult主要用于解决onActivityResult业务逻辑臃肿的问题

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyResultActivity.kt)

## 特性

1. 接管`onActivityResult`方法，解耦数据回传逻辑
2. 在需要时，`自动创建`requestCode提供使用，免去每次都需要定义`不重复的requestCode`步骤
3. 防暴击：两次启动间隔必须大于1秒。

## 用法

1. 首先，在基类的`onActivityResult`方法中添加中转方法：

```
class BaseActivity:Activity() {
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	    // 进行回传数据过滤转发
	    EasyActivityResult.dispatch(this, requestCode, resultCode, data)
	}
}
```

2. 最后，使用EasyActivityResult.start()方法进行启动即可：

```
EasyActivityResult.startActivity(
		context,
		intent,
		{result:Int, data:Intent -> // 数据回调接口
			// TODO
			// result:对应回传的resultCode
			// data:  对应回传的intent数据
		})
```

使用方式就是这么简单！而且细心点的可以发现： **根本不用再配置requestCode了！** 又省了一步操作。美滋滋~~

不用配置requestCode的原因是: 在组件内部。当你配置有数据回调接口，那么就会自动生成一个随机的requestCode提供使用：

```
if (context !is Activity || callback == null) {
	context.startActivity(intent)
} else {
	val requestCode = codeGenerator.nextInt(0x0000FFFF)
	...
}
```

而且，由于我们也接管了启动入口。所以也能很方便的进行防暴击过滤：

以下方的模拟暴击点击为例：

```
@OnClick(R.id.violentStart)
fun violentStart() {
    // 暴击启动测试：同时被调用启动多次，应只有第一次启动成功
    EasyActivityResult.startActivity(this,
            Intent(this, EasyToastActivity::class.java),
            {_, _ -> EasyToast.DEFAULT.show("暴击启动测试：第一次启动任务 接收返回信息") })

    EasyActivityResult.startActivity(this,
            Intent(this, EasyToastActivity::class.java),
            {_, _ -> EasyToast.DEFAULT.show("暴击启动测试：第二次启动任务 接收返回信息") })

    EasyActivityResult.startActivity(this,
            Intent(this, EasyToastActivity::class.java),
            {_, _ -> EasyToast.DEFAULT.show("暴击启动测试：第三次启动任务 接收返回信息") })
}
```

**页面效果展示：**

![](https://user-gold-cdn.xitu.io/2018/6/14/163fc140de78535e?w=320&h=569&f=gif&s=287165)