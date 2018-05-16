# UsefulCodes

此工程下用于存放一些平时开发中容易使用到的工具类库。便于需要时直接进行使用。

## AndroidCodes

AndroidCodes是为Android开发者提供的基础工具类库。

Android工具库代码已提交到JitPack仓库中。可选择依赖下载进行使用或者直接获取源码使用：

### 添加依赖

1. 添加jitpack仓库依赖
```
maven { url 'https://jitpack.io' }
```

2. 添加依赖

lastest_version = [![](https://jitpack.io/v/yjfnypeu/UsefulCodes.svg)](https://jitpack.io/#yjfnypeu/UsefulCodes)

```
implementation "com.github.yjfnypeu:UsefulCodes:$lastest_version"
```

3. 初始化

在Application中调用初识化方法：

```
SingleCache.init(application)
```

然后即可直接使用

目前版本提供以下部分工具类库:

- [EasyDimension](./docs/EasyDimension.md)
> 用于灵活的进行设备尺寸单位转换
- [EasyFormatter](./docs/EasyFormatter.md)
> 用于对任意类型数据，进行格式化输出。便于展示查看
- [EasyLog](./docs/EasyLog.md)
> 用于简单的进行日志打印输出，支持格式化输出、自定义打印格式。
- [EasyToast](./docs/EasyToast.md)
> 用于进行Toast提示，可很简单的指定输出样式。
- [APIs](./docs/APIs.md)
> 提供的一些其他零散的类库APIs

## gradleCodes

此处为平时开发过程中，有用到的一些gradle脚本.

- [javadoc-generator-android](./gradles/javadoc-generator-android.gradle)

> 提供给android module进行使用：在打包上传到maven仓库时，将javadoc文档也一起进行打包发布

- [javadoc-generator-java](./gradles/javadoc-generator-java.gradle)

> 提供给java module进行使用：在打包上传到maven仓库时，将javadoc文档也一起进行打包发布

- [local-properties-loader](./gradles/local-properties-loader.gradle)

> 用于自动加载项目根目录下的local.properties文件。将其中的属性加载到properties中供方便使用


## 联系作者

<a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=99e758d20823a18049a06131b6d1b2722878720a437b4690e238bce43aceb5e1"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="安卓交流会所" title="安卓交流会所"></a>

或者手动加入QQ群: 108895031

## License

[apache license 2.0](http://choosealicense.com/licenses/apache/)
