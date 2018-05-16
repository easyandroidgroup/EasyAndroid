# UsefulCodes

此工程下用于存放一些平时开发中容易使用到的代码。便于需要时直接进行使用。

## AndroidCodes



- [EasyDimension](./docs/EasyDimension.md)
>
- [EasyFormatter](./docs/EasyFormatter.md)
- [EasyLog](./docs/EasyLog.md)
- [EasyToast](./docs/EasyToast.md)
- [SafeExtensions](./docs/SafeExtensions.md)

## gradleCodes

- [javadoc-generator-android](./gradles/javadoc-generator-android.gradle)

> 提供给android module进行使用：在打包上传到maven仓库时，将javadoc文档也一起进行打包发布

- [javadoc-generator-java](./gradles/javadoc-generator-java.gradle)

> 提供给java module进行使用：在打包上传到maven仓库时，将javadoc文档也一起进行打包发布

- [local-properties-loader](./gradles/local-properties-loader.gradle)

> 用于自动加载项目根目录下的local.properties文件。将其中的属性加载到properties中供方便使用