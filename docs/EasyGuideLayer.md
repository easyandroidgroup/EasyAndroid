# EasyGuideLayer

EasyGuideLayer是用于进行蒙层引导实现的一个组件。

[Sample Activity](../app/src/main/java/com/haoge/sample/easyandroid/activities/EasyGuideLayerActivity.kt)

## 特性

- 链式调用。调用逻辑清晰直观
- 支持同时设置多个引导层
- 支持高亮区域的自定义绘制
- 支持高亮区域点击监听
- 支持指定任意View设置蒙层引导
- 支持进行蒙层展示、隐藏事件监听

## 用法

用法概览

```
// step1: 创建layer蒙层实例。并添加基础配置
val layer = EasyGuideLayer.with(activity)...

// step2: 创建引导item实例，添加引导配置并绑定给layer：
val item = GuideItem.newInstance(view, padding)...
layer.addItem(item)

// step3: 请求进行蒙层展示：
layer.show()
```

实例说明：

1. `EasyGuideLayer`：为蒙层实例。为底部半透明的覆盖层。
2. `GuideItem`: 为引导层实例。盖在蒙层之上，展示的`引导View`以及对应的`高亮区域`显示部分。

### 创建蒙层实例。

#### 1. 蒙层实例的创建分为两种方式：

```
// 1. 直接使用activity. 表示直接在content布局上展示蒙层
val layer = EasyGuideLayer.with(activity)
// 2. 手动指定对应的View.表示在此View布局上展示蒙层
val layer = EasyGuideLayer.with(view)
```

#### 2. 为蒙层添加背景色：

```
layer.setBackgroundColor(color)// 若不设置，为默认背景色：0x33000000
```

#### 3. 设置蒙层的展示/消失监听器：

```
layer.setOnGuideShownListener { isShowing:Boolean -> // isShowing为true表示蒙层在展示，否则为消失 }
```

#### 4. 设置当点击到非预期区域(即非内部view点击以及非高亮区域点击事件)的时候，是否自动关闭蒙层

```
layer.setDismissOnClickOutside(false)
```

#### 5. 设置当未添加有引导层实例GuideItem时。是否自动将蒙层关闭：

```
layer.setDismissIfNoItems(false)
```

#### 6. 设置引导层实例：

一个蒙层的引导层可以添加多个。所以也就对应有新增、移除、清空操作：

```
// 1. 添加指定引导层实例：
layer.addItem(item:GuideItem)
// 2. 移除指定引导层实例：
layer.removeItem(item)
// 3. 清空所有的引导层实例：
layer.clearItems()
```

#### 7. 配置完毕。发起蒙层展示请求：

```
layer.show()
```

### GuideItem引导层实例

在上面我们有提到`引导层`的概念，一个GuideItem实例表示一个具体的`引导层`，一个引导层相对于界面展示效果来说。
即是指的对应的`高亮块`以及`引导View`。

#### 1. 引导层实例的创建：

GuideItem提供三个方法进行创建。根据不同的方法创建参数将会创建出不同的`高亮块区域`进行绘制。

```
//高亮块区域为指定的`view`的位置再边框扩充padding尺寸(单位为px)后的大小
val item = GuideItem.newInstance(view:View, padding:Int)
```

```
// 直接指定具体的高亮矩形区域
val item = GuideItem.newInstance(rect:Rect)
```

```
// 不指定，则将高亮块区域设置为Rect(0,0,0,0)
val item = GuideItem.newInstance()
```

#### 2. 设置引导View

一个引导层`GuideItem`可以绑定一个`引导View`。此View将被添加到蒙层布局中，并展示在蒙层顶部

设置引导View的方式有两种：

- 直接指定具体的引导View的layout：

```
item.setlayout(@LayoutRes layout)
```

- 或者直接指定具体的图片drawable。让组件创建个adjustViewBounds为true的ImageView进行使用

```
item.setDrawable(@DrawableRes drawable)
```

有时候我们会需要在`引导View`被添加到布局中去的时候。进行一些额外处理。比如内部部分控件`点击监听`等。

```
item.setOnViewAttachedListener { view:View, controller:Controller ->
    // view: 被创建的具体引导View实例。
    // controller: 提供的控制器。可使用此实例在点击后让蒙层消失或对蒙层进行更新显示
    // 注意：此处view刚被添加到蒙层布局中，所以不能在此获取view的具体大小尺寸等信息
}
```

#### 3. 设置引导View的相对位置

```
item.setGravity(gravity:Int)
```

引导View的相对位置是相对于`高亮块区域rect`的, 支持以下几种相对位置关系：

- Gravity.LEFT|Gravity.TOP:     高亮区域`左上角`
- Gravity.RIGHT|Gravity.TOP:    高亮区域`右上角`
- Gravity.LEFT|Gravity.BOTTOM:  高亮区域`左下角`
- Gravity.RIGHT|Gravity.BOTTOM: 高亮区域`右下角`
- Gravity.LEFT:                 高亮区域`左侧`
- Gravity.TOP:                  高亮区域`顶部`
- Gravity.RIGHT:                高亮区域`右侧`
- Gravity.BOTTOM:               高亮区域`底部`
- Gravity.NO_GRAVITY:           不指定。与高亮区域共同同一顶点

#### 4. 对引导View的位置进行微调

`gravity`位置是比较单一的，所以为了满足更多的位置要求。组件也提供的对应的回调允许进行`位置偏移调整`

```
item.setOffsetProvider {
    point:Point, // 顶点位置
    rect:RectF, // 高亮块区域
    view:View -> // 具体的引导View实例。此处view已被测量大小。可以直接获取width、height数据
    // TODO 在此计算好偏移量后。设置给point调整顶点位置即可
}
```

#### 5. 设置高亮绘制模式

```
item.setHighLightShape(shapeType:Int)
```

shapeType(高亮显示效果)组件自带三种模式：

- GuideItem.SHAPE_NONE: 默认模式，表示不进行高亮块绘制。
- GuideItem.SHAPE_RECT: 矩形模式，表示将高亮块绘制为矩形进行展示
- GuideItem.SHAPE_OVAL: 椭圆模式，表示将高亮块绘制为椭圆进行展示

#### 6. 自定义高亮绘制逻辑

自带的绘制模式永远不可能满足设计师们的要求。所以也提供了回调做`自定义高亮块绘制`：

```
item.setOnDrawHighLightCallback {
    canvas:Canvas, // 蒙层的画布
    rect:RectF, // 高亮块
    paint:Paint -> // 绘制的画笔。模式为PorterDuff.Mode.CLEAR。
    // 在此进行自定义绘制
}
```

需要注意的点：

- 绘制回调运行在onDraw方法中。所以请避免在此进行`对象创建操作`
- 避免对提供的paint的属性进行修改，如果需要，请在外部额外创建单独的paint提供使用
- 当配置了绘制回调时，则说明不再使用本身的`shapeType`模式进行高亮绘制。

#### 7. 添加高亮块点击监听

高亮块的点击，只有在绘制模式为`非 SHAPE_NONE`时，才能被触发。

```
item.setOnHighLightClickListener { controller:Controller ->
    // 进行点击后的操作
}
```

### Controller控制说明

Controller类用于对蒙层进行控制：

1. 让蒙层消失：

```
controller.dismiss()
```

2. 更新蒙层信息并展示：

```
// 获取蒙层示例
val layer = controller.getLayer()
...// 操作蒙层示例。更新蒙层数据
// 更新数据后。重新发起蒙层展示请求。更新蒙层展示
layer.show()
```

