@file:Suppress("unused","RtlHardcoded")

package com.haoge.easyandroid.easy

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.annotation.IntDef
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import com.haoge.easyandroid.EasyAndroid

private typealias OnGuideShowListener = (Boolean) -> Unit
private typealias OnGuideViewCreatedListener = (View, EasyGuideLayer.Controller) -> Unit
private typealias OnDrawHighLightCallback = (canvas:Canvas, rect:RectF, paint:Paint) -> Boolean
private typealias OnHighLightClickListener = (EasyGuideLayer.Controller) -> Unit
private typealias OnGuideViewOffsetProvider = (Point, RectF, View) -> Unit
class EasyGuideLayer private constructor(private val anchor: View){

    private val items = mutableListOf<GuideItem>()
    private var onGuideShowListener:OnGuideShowListener? = null
    private var backgroundColor:Int = 0x33000000
    private var dismissOnClickOutside = false
    private var dismissIfNoItems = false

    /** 设置蒙层的背景色[color]*/
    fun setBackgroundColor(color:Int): EasyGuideLayer {
        this.backgroundColor = color
        return this
    }

    /** 设置蒙层的展示/消失事件监听器。
     *
     * 回调参数：Boolean, True表示蒙层为展示状态
     */
    fun setOnGuideShownListener(listener: OnGuideShowListener?): EasyGuideLayer {
        this.onGuideShowListener = listener
        return this
    }

    /** 设置当点击到非预期区域(即非内部view点击内部消费事件)的时候，是否自动关闭蒙层, True表示自动关闭*/
    fun setDismissOnClickOutside(dismiss: Boolean): EasyGuideLayer {
        this.dismissOnClickOutside = dismiss
        return this
    }

    /** 设置是否当此layer不存在绑定的GuideItem时，自动关闭蒙层。 True表示自动关闭*/
    fun setDismissIfNoItems(dismiss: Boolean): EasyGuideLayer {
        this.dismissIfNoItems = dismiss
        return this
    }

    /** 添加引导层item*/
    fun addItem(item:GuideItem): EasyGuideLayer {
        if (items.contains(item).not()) items.add(item)
        return this
    }

    /** 移除制定的引导层item*/
    fun removeItem(item: GuideItem): EasyGuideLayer {
        if (items.contains(item)) items.remove(item)
        return this
    }

    /** 清理所有的已添加的引导层*/
    fun clearItems(): EasyGuideLayer {
        items.clear()
        return this
    }

    fun show() {
        val activity = getActivity(anchor)?:throw RuntimeException("")
        val parent = (anchor.parent as ViewGroup?)?:throw RuntimeException("")
        // 从指定布局节点处寻找或创建guideLayout容器。进行绑定展示
        val guideLayout = if (anchor is FrameLayout && getLastView(anchor) is GuideLayout) {
            getLastView(anchor) as GuideLayout
        } else if (parent is FrameLayout && getLastView(parent) is GuideLayout) {
            getLastView(parent) as GuideLayout
        } else if (anchor is FrameLayout) {
            val guide = GuideLayout(activity)
            anchor.addView(guide, FrameLayout.LayoutParams(-1, -1))
            guide
        } else {
            val guide = GuideLayout(activity)
            val frame = FrameLayout(activity)
            val params = anchor.layoutParams
            val index = parent.indexOfChild(anchor)
            parent.removeView(anchor)
            parent.addView(frame, index, params)
            frame.addView(anchor, FrameLayout.LayoutParams(-1, -1))
            frame.addView(guide, FrameLayout.LayoutParams(-1, -1))
            guide
        }

        if (items.isEmpty() && dismissIfNoItems) {
            guideLayout.dismiss()
        } else {
            guideLayout.bindLayer(this)
        }
    }

    private fun getLastView(view:ViewGroup):View? =
            if (view.childCount > 0) view.getChildAt(view.childCount - 1) else null

    private fun getActivity(view:View):Activity? {
        var bindAct: Activity? = null
        var context = view.context
        do {
            if (context is Activity) {
                bindAct = context
                break
            } else if (context is ContextWrapper) {
                context = context.baseContext
            } else {
                break
            }
        } while (true)
        return bindAct
    }

    companion object {
        fun with(anchor:View) = EasyGuideLayer(anchor)
        fun with(activity: Activity) = EasyGuideLayer.with(activity.findViewById<View>(android.R.id.content))
    }

    class GuideLayout(context: Context?) : FrameLayout(context), Controller, View.OnClickListener {
        private var layer:EasyGuideLayer? = null
        private var container = mutableMapOf<RectF, GuideItem>()
        private val paint = Paint()

        init {
            paint.isAntiAlias = true
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            paint.maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.INNER)
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            setWillNotDraw(true)
            setOnClickListener(this)
        }

        override fun getGuideLayer(): EasyGuideLayer = layer?:throw RuntimeException("")

        override fun dismiss() {
            container.clear()
            val parent = parent as ViewGroup?
            parent?.removeView(this)
            layer = null
        }

        fun bindLayer(layer:EasyGuideLayer) {
            removeAllViews()
            this.layer = layer
            setBackgroundColor(layer.backgroundColor)
            layer.items.forEach { item ->
                val child = createItemView(item)
                child.setTag(GuideItem.TAG_ID, item)
                addView(child)
                item.getOnViewCreatedListener()?.invoke(child, this)
            }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            layer?.onGuideShowListener?.invoke(true)
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            layer?.onGuideShowListener?.invoke(false)
        }

        override fun onClick(v: View?) {
            if (layer?.dismissOnClickOutside == true) {
                dismiss()
            }
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            container.clear()
            for (index in 0 until childCount) {
                val child = getChildAt(index)
                val item = child.getTag(GuideItem.TAG_ID) as? GuideItem ?: continue
                val rect = getRectFromItem(item)
                container[rect] = item
                resetChildLayoutParams(item, rect, child)
            }
        }

        private fun resetChildLayoutParams(item: GuideItem, rect:RectF, child:View) {
            // 根据不同的gravity获取指定的child顶点(X * Y)
            val point:Point = when(item.getGravity()) {
                Gravity.TOP -> {Point(rect.left.toInt(), (rect.top - child.height).toInt())}
                Gravity.BOTTOM -> {Point(rect.left.toInt(), rect.bottom.toInt())}
                Gravity.LEFT -> {Point((rect.left - child.width).toInt(), rect.top.toInt())}
                Gravity.RIGHT -> {Point(rect.right.toInt(), rect.top.toInt())}
                else -> {Point(rect.left.toInt(), rect.top.toInt())}
            }

            item.getOffsetProvider()?.invoke(point, rect, child)
            val params = child.layoutParams as LayoutParams
            params.leftMargin = point.x
            params.topMargin = point.y
            child.layoutParams = params
        }

        private fun getRectFromItem(item: GuideItem): RectF {
            if (item.rect != null) return item.rect
            if (item.view == null) return RectF(0f, 0f, 0f, 0f)

            val viewRect = Rect()
            val guideRect = Rect()
            item.view.getGlobalVisibleRect(viewRect)
            getGlobalVisibleRect(guideRect)

            if (guideRect.contains(viewRect).not()) return RectF(0f, 0f, 0f, 0f)
            return RectF((viewRect.left - guideRect.left - item.padding).toFloat(),
                    (viewRect.top - guideRect.top - item.padding).toFloat(),
                    (viewRect.left - guideRect.left + viewRect.width() + item.padding).toFloat(),
                    (viewRect.top - guideRect.top + viewRect.height() + item.padding).toFloat())
        }

        private fun createItemView(item:GuideItem):View {
            if (item.getLayout() != 0) {
                val inflater = LayoutInflater.from(context)
                return inflater.inflate(item.getLayout(), this, false)
            } else if (item.getDrawable() != null) {
                val image = ImageView(context)
                image.layoutParams = FrameLayout.LayoutParams(-2, -2)
                image.adjustViewBounds = true
                image.setImageDrawable(item.getDrawable())
                return image
            }
            return EmptyItem(context)
        }

        override fun onDraw(canvas: Canvas) {
            container.forEach {
                if (it.value.getOnDrawHighLightListener()?.invoke(canvas, it.key, paint) == true) return@forEach

                when(it.value.getShapeType()) {
                    GuideItem.SHAPE_RECT -> canvas.drawRect(it.key, paint)
                    GuideItem.SHAPE_OVAL -> canvas.drawOval(it.key, paint)
                }
            }
        }

        private var downPoint:PointF? = null

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
            when(event.action) {
                MotionEvent.ACTION_DOWN -> { downPoint = PointF(event.x, event.y) }
                MotionEvent.ACTION_MOVE -> {
                    val down = downPoint?:return super.onTouchEvent(event)
                    if (event.x - down.x > touchSlop || event.y - down.y > touchSlop) downPoint = null
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    val down = downPoint?:return super.onTouchEvent(event)
                    container.forEach {
                        if (it.key.contains(down.x, down.y) &&
                                it.value.getShapeType() != GuideItem.SHAPE_NONE &&
                                it.value.getOnHighLightClickListenter() != null) {
                            it.value.getOnHighLightClickListenter()?.invoke(this)
                            return true
                        }
                    }
                }
            }
            return super.onTouchEvent(event)
        }
    }

    private class EmptyItem(context: Context?) : View(context) {
        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            setMeasuredDimension(0, 0)
        }
    }

    interface Controller {
        fun getGuideLayer():EasyGuideLayer
        fun dismiss()
    }
}

/** 一个GuideItem实例表示蒙层上的一块引导层。一个引导层包括有：
 *
 * 1. 一个指定的区域用于进行View位置定位以及高亮效果展示：由[rect], [view], [padding]提供定位区域, [shapeType]提供高亮展示效果
 * 2. 引导View，在蒙层上展示给用户查看的一些额外提示性View。由[setLayout]或者[setDrawable]进行提供。
 * */
class GuideItem private constructor(val rect: RectF? = null, val view: View? = null, val padding: Int = 0){
    private var shapeType = SHAPE_NONE
    private var onViewCreated:OnGuideViewCreatedListener? = null
    private var onDrawHighLight:OnDrawHighLightCallback? = null
    private var onHighLightClickListener:OnHighLightClickListener? = null
    private var offsetProvider:OnGuideViewOffsetProvider? = null
    private var drawable:Drawable? = null
    private var layout:Int = 0
    private var gravity = Gravity.NO_GRAVITY

    fun getShapeType() = shapeType
    fun getOnViewCreatedListener() = onViewCreated
    fun getOnDrawHighLightListener() = onDrawHighLight
    fun getOnHighLightClickListenter() = onHighLightClickListener
    fun getDrawable() = drawable
    fun getLayout() = layout
    fun getGravity()   = gravity
    fun getOffsetProvider() = offsetProvider

    /** 设置此接口用于对引导层的位置进行微调。回调参数分别为：
     *  1. Point: 根据[setGravity]配置计算出的引导层顶点坐标，
     *  2. RectF: 引导层的定位区域
     *  3. View: 顶层的引导View。可直接在回调用获取到View的Width以及Height。便于动态计算进行位置展示
     */
    fun setOffsetProvider(offsetProvider:OnGuideViewOffsetProvider?): GuideItem {
        this.offsetProvider = offsetProvider
        return this
    }

    /** 设置高亮区域的显示效果。默认为[GuideItem.SHAPE_NONE]: 不展示高亮效果。*/
    fun setHighLightShape(@ShapeType shapeType:Int): GuideItem {
        this.shapeType = shapeType
        return this
    }

    /**
     * 设置引导View被创建时的回调监听器。可实现此监听器对引导View进行操作。比如添加点击监听等，回调参数分别为：
     * 1. View:被创建的引导View。
     * 2. Controller: 提供给上层使用的控制器。可用于进行蒙层关闭或者蒙层引导更新等操作。
     */
    fun setOnViewCreatedListener(onViewCreatedListener: OnGuideViewCreatedListener?): GuideItem {
        this.onViewCreated = onViewCreatedListener
        return this
    }

    /**
     * 设置自定义高亮绘制回调。用于进行高亮区域的自定义绘制逻辑(因为默认值提供了Rect与Oval模式绘制).回调参数分别为：
     * 1. Canvas: 用于进行绘制的画布
     * 2. RectF: 需要进行高亮绘制的位置区域。
     * 3. Paint: 提供的画笔。默认Xfermode为PorterDuff.Mode.CLEAR
     */
    fun setOnDrawHighLightCallback(callback: OnDrawHighLightCallback?): GuideItem {
        this.onDrawHighLight = callback
        return this
    }

    /**
     * 设置高亮点击监听器，当点击到对应高亮区域(高亮模式必须为非SHAPE_NONE模式)时触发。回调参数为：
     * 1. Controller: 提供给上层使用的控制器。可用于进行蒙层关闭或者蒙层引导更新等操作。
     */
    fun setOnHighLightClickListener(onHighLightClickListener: OnHighLightClickListener?): GuideItem {
        this.onHighLightClickListener = onHighLightClickListener
        return this
    }

    /**
     * 设置引导层View的布局id。与[setDrawable]互斥。
     */
    fun setLayout(layout:Int): GuideItem {
        this.layout = layout
        this.drawable = null
        return this
    }

    /**
     * 根据提供的drawable创建一个模式为adjustViewBounds的ImageView。作为引导层View使用。与[setLayout]互斥
     */
    fun setDrawable(drawable: Drawable): GuideItem {
        this.drawable = drawable
        this.layout = 0
        return this
    }

    /**
     * 根据提供的drawable创建一个模式为adjustViewBounds的ImageView。作为引导层View使用。与[setLayout]互斥
     */
    fun setDrawable(@DrawableRes drawableRes: Int): GuideItem {
        @Suppress("DEPRECATION")
        this.drawable = EasyAndroid.getApplicationContext().resources.getDrawable(drawableRes)
        this.layout = 0
        return this
    }

    /**
     * 设置引导层相对于高亮区域的相对位置，目前仅支持[Gravity.LEFT], [Gravity.TOP], [Gravity.BOTTOM], [Gravity.RIGHT], [Gravity.NO_GRAVITY]
     */
    fun setGravity(@LimitGravity gravity: Int): GuideItem {
        this.gravity = gravity
        return this
    }

    companion object {
        /** 不进行高亮绘制*/
        const val SHAPE_NONE = -1
        /** 绘制成矩形的高亮区域*/
        const val SHAPE_RECT = 0
        /** 绘制成椭圆的高亮区域*/
        const val SHAPE_OVAL = 1

        internal const val TAG_ID = 0x0F000000
        fun newInstance() = GuideItem()
        fun newInstance(view: View, padding: Int = 0) = GuideItem(view = view, padding = padding)
        fun newInstance(rect: RectF) = GuideItem(rect = rect)
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(GuideItem.SHAPE_NONE, GuideItem.SHAPE_RECT, GuideItem.SHAPE_OVAL)
annotation class ShapeType

@Retention(AnnotationRetention.SOURCE)
@IntDef(Gravity.LEFT, Gravity.TOP, Gravity.BOTTOM, Gravity.RIGHT, Gravity.NO_GRAVITY)
annotation class LimitGravity