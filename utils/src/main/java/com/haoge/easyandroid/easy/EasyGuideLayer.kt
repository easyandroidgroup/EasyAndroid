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
private typealias OnDrawHighLightListener = (canvas:Canvas, rect:RectF, paint:Paint) -> Boolean
private typealias OnHighLightClickListener = (EasyGuideLayer.Controller) -> Unit
private typealias OnGuideViewOffsetProvider = (Point, RectF, View) -> Unit
class EasyGuideLayer private constructor(private val anchor: View){

    private val items = mutableListOf<GuideItem>()
    private var onGuideShowListener:OnGuideShowListener? = null
    private var backgroundColor:Int = 0x33000000
    private var dismissOnClickOutside = false
    private var dismissIfNoItems = false

    /** 设置蒙层的背景色 */
    fun setBackgroundColor(color:Int): EasyGuideLayer {
        this.backgroundColor = color
        return this
    }

    /** 设置蒙层的展示/消失事件监听器 */
    fun setOnGuideShownListener(listener: OnGuideShowListener?): EasyGuideLayer {
        this.onGuideShowListener = listener
        return this
    }

    /** 设置当点击到非预期区域(即非内部view点击内部消费事件)的时候，是否自动关闭蒙层*/
    fun setDismissOnClickOutside(dismiss: Boolean): EasyGuideLayer {
        this.dismissOnClickOutside = dismiss
        return this
    }

    /** 设置是否当此layer不存在绑定的GuideItem时。自动消失*/
    fun setDismissIfNoItems(dismiss: Boolean): EasyGuideLayer {
        this.dismissIfNoItems = dismiss
        return this
    }

    fun addItem(item:GuideItem): EasyGuideLayer {
        if (items.contains(item).not()) items.add(item)
        return this
    }

    fun removeItem(item: GuideItem): EasyGuideLayer {
        if (items.contains(item)) items.remove(item)
        return this
    }

    /** 清理所有的已添加的引导条目*/
    fun clearItems(): EasyGuideLayer {
        items.clear()
        return this
    }

    fun show() {
        val activity = getActivity(anchor)?:throw RuntimeException("")
        val parent = (anchor.parent as ViewGroup?)?:throw RuntimeException("")
        // 从指定布局节点处寻找或创建guideLayout容器。进行绑定展示
        val guideLayout = if (anchor is FrameLayout && getLast(anchor) is GuideLayout) {
            getLast(anchor) as GuideLayout
        } else if (parent is FrameLayout && getLast(parent) is GuideLayout) {
            getLast(parent) as GuideLayout
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

    private fun getLast(view:ViewGroup):View? =
            if (view.childCount > 0) view.getChildAt(view.childCount - 1) else null

    companion object {
        fun with(anchor:View) = EasyGuideLayer(anchor)
        fun with(activity: Activity) = EasyGuideLayer.with(activity.findViewById<View>(android.R.id.content))

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
            layer = null
            container.clear()
            val parent = parent as ViewGroup?
            parent?.removeView(this)
        }

        fun bindLayer(layer:EasyGuideLayer) {
            removeAllViews()
            this.layer = layer
            setBackgroundColor(layer.backgroundColor)
            layer.items.forEach { item ->
                val child = createItemView(item)
                child.setTag(GuideItem.TAG_ID, item)
                item.getOnViewCreatedListener()?.invoke(child, this)
                addView(child)
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
                                it.value.getOnHighLightClickLisenter() != null) {
                            it.value.getOnHighLightClickLisenter()?.invoke(this)
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

class GuideItem private constructor(val rect: RectF? = null, val view: View? = null, val padding: Int = 0){
    private var shapeType = SHAPE_NONE
    private var onViewCreated:OnGuideViewCreatedListener? = null
    private var onDrawHighLight:OnDrawHighLightListener? = null
    private var onHighLightClickListener:OnHighLightClickListener? = null
    private var offsetProvider:OnGuideViewOffsetProvider? = null
    private var drawable:Drawable? = null
    private var layout:Int = 0
    private var gravity = Gravity.NO_GRAVITY

    fun getShapeType() = shapeType
    fun getOnViewCreatedListener() = onViewCreated
    fun getOnDrawHighLightListener() = onDrawHighLight
    fun getOnHighLightClickLisenter() = onHighLightClickListener
    fun getDrawable() = drawable
    fun getLayout() = layout
    fun getGravity()   = gravity
    fun getOffsetProvider() = offsetProvider

    fun setOffsetProvider(offsetProvider:OnGuideViewOffsetProvider?): GuideItem {
        this.offsetProvider = offsetProvider
        return this
    }

    fun setHighLightShape(@ShapeType shapeType:Int): GuideItem {
        this.shapeType = shapeType
        return this
    }

    fun setOnViewCreatedListener(onViewCreatedListener: OnGuideViewCreatedListener?): GuideItem {
        this.onViewCreated = onViewCreatedListener
        return this
    }

    fun setOnDrawHighLightListener(onDrawHighLightListener: OnDrawHighLightListener?): GuideItem {
        this.onDrawHighLight = onDrawHighLightListener
        return this
    }

    fun setOnHighLightClickListener(onHighLightClickListener: OnHighLightClickListener?): GuideItem {
        this.onHighLightClickListener = onHighLightClickListener
        return this
    }

    fun setLayout(layout:Int): GuideItem {
        this.layout = layout
        this.drawable = null
        return this
    }

    fun setDrawable(drawable: Drawable): GuideItem {
        this.drawable = drawable
        this.layout = 0
        return this
    }

    fun setDrawable(@DrawableRes drawableRes: Int): GuideItem {
        @Suppress("DEPRECATION")
        this.drawable = EasyAndroid.getApplicationContext().resources.getDrawable(drawableRes)
        this.layout = 0
        return this
    }

    fun setGravity(@LimitGravity gravity: Int): GuideItem {
        this.gravity = gravity
        return this
    }

    companion object {
        const val SHAPE_NONE = -1
        const val SHAPE_RECT = 0
        const val SHAPE_OVAL = 1

        const val TAG_ID = 0x0F000000
        fun newInstance() = GuideItem()
        fun newInstance(view: View, padding: Int = 0) = GuideItem(view = view, padding = padding)
        fun newInstance(rect: RectF) = GuideItem(rect = rect)
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(GuideItem.SHAPE_NONE, GuideItem.SHAPE_RECT, GuideItem.SHAPE_OVAL)
annotation class ShapeType

@Retention(AnnotationRetention.SOURCE)
@IntDef(Gravity.LEFT, Gravity.TOP, Gravity.BOTTOM, Gravity.RIGHT)
annotation class LimitGravity
