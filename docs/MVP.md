# MVP

### 写在前面

MVP本身只是种编程分层架构思想，所以MVP并没有严格意义上的标准实现方式。

因为每个app所使用的基础Actvitiy/Fragment都是不统一的。所以在MVP层的封装中，
并没有将BaseMVPPresenter的具体封装实现放入集成库中，而是在sample代码中提供了一份
推荐的实现示例：

[BaseMVPActivity](../app/src/main/java/com/haoge/sample/easyandroid/activities/mvp/BaseMVPActivity.kt)

需要使用MVP模式的。请记得仿照上方的示例代码。或者copy进入进行相应修改后再进行使用。

### 示例说明：

[请参考此处示例代码](../app/src/main/java/com/haoge/sample/easyandroid/activities/mvp/MVPDemoActivity.kt)