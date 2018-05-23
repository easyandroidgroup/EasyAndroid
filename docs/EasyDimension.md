# EasyDimension

EasyDimension用于快速的在系统提供的各种尺寸间(PX, DIP, SP, IN, PT, MM)进行转换。

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyDimensionActivity.kt)

## 用法

1. 传入待转换尺寸

```kotlin
// value为原始数值。unit为数值单位。
// unit单位使用系统提供的尺寸单位，如TypedValue.COMPLEX_UNIT_PX, TypedValue.COMPLEX_UNIT_DIP
dimension = EasyDimension.create(value, unit)
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
val pxResult = EasyDimension.create(30, TypedValue.COMPLEX_UNIT_DIP).toPX()
```