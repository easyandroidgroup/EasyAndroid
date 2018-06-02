# MVP

### 写在前面

MVP本身只是种编程分层架构思想，所以MVP并没有严格意义上的标准实现方式。

因为每个app所使用的基础Actvitiy/Fragment都是不统一的。所以在MVP层的封装中，
并没有将BaseMVPActivity的具体实现放入集成库中，而是在sample代码中提供了一份推荐的实现示例：

[BaseMVPActivity](../app/src/main/java/com/haoge/sample/easyandroid/activities/mvp/BaseMVPActivity.kt)

需要使用MVP模式的。请记得仿照上方的示例代码。或者copy进入项目并修改成自身所需要的BaseActivity/BaseFragment等进行使用。

### 示例说明：

1. 创建页面通信协议接口：

```
interface CustomView:MVPView {
    // 定义通信协议方法：P层将使用此方法驱动V层进行界面更新
    fun updateUI()
}
```

2. 创建Presenter。并与对应的通信协议相绑定：

```
class CustomPresenter(view:CustomView):MVPPresenter(view) {
    // 直接创建对应的启动方法，供V层进行启动调用
    fun requestData() {
        // TODO 进行数据业务处理。并在处理完成后
        // 通过绑定的V层协议接口。通知到V层去进行界面更新
        view?.updateUI()

        // 或者如果执行的是异步任务：比如进行api访问。
        // 需要在回调后进行绑定逻辑判断：是否已经与V层解绑.
        // 避免造成由于已解绑后继续执行造成crash
        if (!isViewAttached()) return
    }
}
```

3. 创建具体的V层(Activity or Fragment),并绑定Presenter进行使用：

```
class CustomActivity:BaseMVPActivity<CustomPresenter>, CustomView {
    override fun updateUI() {// TODO 进行界面更新}

    fun initPage() {
        // 通过绑定的Presenter发起任务
        presenter?.requestData()
    }
}
```

[请参考此处示例代码](../app/src/main/java/com/haoge/sample/easyandroid/activities/mvp/MVPDemoActivity.kt)

### BaseMVPActivity配置说明

```
abstract class BaseMVPActivity<out P:MVPPresenter<*>>:Activity(), MVPView{

    // 一个Activity持有一个唯一的Dispatcher派发器。
    val mvpDispatcher by lazy { MVPDispatcher.create() }

    // 一般来说。一个Activity只需要绑定一个Presenter即可。
    // 所以此处绑定一个默认的Presenter。方便进行使用
    val presenter:P? by lazy { return@lazy createPresenter()}

    // 然后再对应生命周期进行派发
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 将默认presenter进行绑定。
        if (presenter != null) {
            mvpDispatcher.addPresenter(presenter!!)
        }
        mvpDispatcher.dispatchOnCreate(intent?.extras)
    }

    override fun onStart() {
        super.onStart()
        mvpDispatcher.dispatchOnStart()
    }

    override fun onResume() {
        super.onResume()
        mvpDispatcher.dispatchOnResume()
    }

    override fun onPause() {
        super.onPause()
        mvpDispatcher.dispatchOnPause()
    }

    override fun onStop() {
        super.onStop()
        mvpDispatcher.dispatchOnStop()
    }

    override fun onRestart() {
        super.onRestart()
        mvpDispatcher.dispatchOnRestart()
    }

    override fun onDestroy() {
        super.onDestroy()
        mvpDispatcher.dispatchOnDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mvpDispatcher.dispatchOnActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mvpDispatcher.dispatchOnSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        mvpDispatcher.dispatchOnRestoreInstanceState(savedInstanceState)
    }

    // MVPView中本身也已定制了部分通用协议方法。在此进行默认实现
    final override fun getActivity(): Activity {
        return this
    }

    override fun showLoadingDialog() {
        progressDialog.safeShow()
    }

    override fun hideLoadingDialog() {
        progressDialog.safeDismiss()
    }

    override fun toastMessage(message: String) {
        EasyToast.DEFAULT.show(message)
    }

    override fun toastMessage(resId: Int) {
        EasyToast.DEFAULT.show(resId)
    }
    // 创建默认的Presenter实例。如果你需要。
    // 也可以考虑直接读取泛型指定的类型。直接通过反射进行创建
    abstract fun createPresenter():P
}
```