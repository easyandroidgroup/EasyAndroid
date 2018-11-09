package com.haoge.sample.easyandroid.activities

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.haoge.easyandroid.easy.EasyGuideLayer
import com.haoge.easyandroid.easy.EasyToast
import com.haoge.easyandroid.easy.GuideItem
import com.haoge.sample.easyandroid.R

/**
 * @author haoge on 2018/10/31
 */
class EasyGuideLayerActivity: EasyDimensionActivity() {

    // 输入框的引导层
    private val inputItem by lazy {
        GuideItem.newInstance(findViewById<View>(R.id.number_input))
                .setGravity(Gravity.TOP)
                .setTextLayout("此处用于输入具体的待转换数值")
                .setHighLightShape(GuideItem.SHAPE_RECT)
                .setOffsetProvider { point /*当前计算的坐标顶点*/,
                                     rectF /*指定的shape区域*/,
                                     view/*展示的view*/ ->
                    point.offset(100, 0)
                }
    }

    private val transformItem by lazy {
        GuideItem.newInstance(findViewById<View>(R.id.selector), 10)
                .setGravity(Gravity.TOP)
                .setTextLayout("选择不同的尺寸作为待转换单位")
    }

    override fun initPage(savedInstanceState: Bundle?) {
        super.initPage(savedInstanceState)
        EasyGuideLayer.with(this)
                .setBackgroundColor(0x33000000)
                .setOnGuideShownListener { EasyToast.DEFAULT.show("蒙层已${if (it) "展示" else "消失"}") }
                .setDismissIfNoItems(true)
                .setDismissOnClickOutside(false)
                .addItem(inputItem)
                .addItem(transformItem)
                .addHighLightForSelector(this)
                .addHighLightForTransform(this)
                .show()
    }
}

private fun GuideItem.setTextLayout(message:String):GuideItem {
    return this.setLayout(R.layout.guide_text_layout)
            .setOnViewAttachedListener { view, controller ->
                (view as TextView).text = message
                // 点击后。关闭自身的引导层。
                view.setOnClickListener { controller.getGuideLayer().removeItem(this).show() }
            }
}

private fun EasyGuideLayer.addHighLightForSelector(activity: Activity): EasyGuideLayer {
    val view = activity.findViewById<ViewGroup>(R.id.selector)
    for (index in 0 until view.childCount) {
        val item = GuideItem.newInstance(view.getChildAt(index))
        item.setHighLightShape(GuideItem.SHAPE_OVAL)
                .setOnHighLightClickListener { it.getGuideLayer().removeItem(item).show() }
        addItem(item)
    }
    return this
}

private fun EasyGuideLayer.addHighLightForTransform(activity: Activity): EasyGuideLayer {
    val view = activity.findViewById<ViewGroup>(R.id.transform)
    for (index in 0 until view.childCount) {
        val item = GuideItem.newInstance(view.getChildAt(index))
        item.setHighLightShape(GuideItem.SHAPE_OVAL)
                .setOnHighLightClickListener { it.getGuideLayer().removeItem(item).show() }
                .setOnDrawHighLightCallback { canvas, rect, paint ->
                    canvas.drawRoundRect(rect, 20f, 20f, paint)
                }
        addItem(item)
    }
    return this
}