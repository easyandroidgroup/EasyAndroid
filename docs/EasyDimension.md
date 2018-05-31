# EasyDimension

EasyDimension用于快速的在系统提供的各种尺寸间(PX, DIP, SP, IN, PT, MM)进行转换。

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyDimensionActivity.kt)

## 特性

- api简单直观

## 用法

1. 传入待转换尺寸

```kotlin
dimension = EasyDimension.withPX(value:Float)//原始尺寸单位PX
dimension = EasyDimension.withDIP(value:Float)//原始尺寸单位PX
dimension = EasyDimension.withSP(value:Float)//原始尺寸单位SP
dimension = EasyDimension.withPT(value:Float)//原始尺寸单位PT
dimension = EasyDimension.withIN(value:Float)//原始尺寸单位IN
dimension = EasyDimension.withMM(value:Float)//原始尺寸单位MM
```

2. 输出转换后的不同尺寸的数值

```kotlin
dimension.toPX()
dimension.toDIP()
dimension.toSP()
dimension.toPT()
dimension.toIN()
dimension.toMM()
```

## Example

将30dp转换为px

```kotlin
val pxResult = EasyDimension.withDIP(30).toPX()
```