# EasyToast

一个简单易用的Toast封装类。用于提供易用的、多样式的Toast组件进行使用

## 特性

1. 支持在任意线程下进行toast提示
2. 非常方便的进行任意样式的定制
3. 不管当前是否正在展示之前的数据。有新消息通知时，直接展示新消息，无需等待

## 用法

### 直接使用默认Toast样式(系统默认样式)

```
EasyToast.Default.show(message, args)
// 或者
EasyToast.Default.show(R.string.toast_message)
```

### 创建自定义样式并进行使用

```kotlin
// 1. 进行实例创建
EasyToast custom = EasyToast.create(
    R.layout.toast_style, // 自定义样式的布局
    R.id.toast_tv, // 布局中用于展示文字信息的TextView的ID
    Toast.LENGTH_SHORT)

// 2. 进行界面展示
custom.show(message, args)
```