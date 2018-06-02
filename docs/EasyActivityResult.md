# EasyActivityResult

EasyActivityResult主要用于解决onActivityResult业务逻辑臃肿的问题

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyResultActivity.kt)

## 特性

- **业务解耦分离**：
> 各自启动业务线处理各自的回调逻辑，不用再在onActivityResult中堆代码
- **去除requestCode**：
> 进行启动时自动生成随机的requestCode, 不用再为每个启动任务分别配置请求码了。
- 防暴击
> 防止快速点击时启动多个重复页面

## 用法

1. 首先，在基类Activity中配置上回调派发入口：

```
class BaseActivity:Activity() {
    ...
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 进行返回信息过滤派发
        EasyActivityResult.dispatch(this, requestCode, resultCode, data)
    }
}
```

2. 然后即可直接启动使用：

```
EasyActivityResult.startActivity(activity,intent,
        { resultCode:Int, data:Intent? ->
            // 直接在此进行返回数据处理
        })
```