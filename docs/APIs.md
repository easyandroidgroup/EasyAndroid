# APIs

## 扩展函数APIs

- Dialog?.safeShow()
> 安全的进行Dialog.show()操作。避免出现weak-window token问题
- Dialog?.safeDismiss()
> 安全的进行Dialog.show()操作。避免出现weak-window token问题
- Any?.easyFormat()
> 对任意类型的对象进行数据格式化排版。使用默认的格式化工具EasyFormat.DEFAULT

## 基础APIs

- ActivityStack.top()
> 获取顶层Activity提供使用
- SingleCache.mainHandler
> 获取主线程的Handler进行使用
- SingleCache.getApplicationContext()
> 获取进程的Application Context实例
