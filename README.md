# EasyAndroid

EasyAndroid用于为Andorid开发者提供一系列**小而精**的基础组件进行使用。

### 添加依赖

1. 添加jitpack仓库依赖
```
maven { url 'https://jitpack.io' }
```

2. 添加依赖

lastest_version = [![](https://jitpack.io/v/yjfnypeu/EasyAndroid.svg)](https://jitpack.io/#yjfnypeu/EasyAndroid)

```
implementation "com.github.yjfnypeu:EasyAndroid:$lastest_version"
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

## 联系作者

<a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=99e758d20823a18049a06131b6d1b2722878720a437b4690e238bce43aceb5e1"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="安卓交流会所" title="安卓交流会所"></a>

或者手动加入QQ群: 108895031

## License

[apache license 2.0](http://choosealicense.com/licenses/apache/)
