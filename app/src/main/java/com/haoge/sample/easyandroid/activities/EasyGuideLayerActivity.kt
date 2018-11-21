package com.haoge.sample.easyandroid.activities

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.widget.TextView
import butterknife.OnClick
import com.haoge.easyandroid.easy.EasyGuideLayer
import com.haoge.easyandroid.easy.EasyToast
import com.haoge.easyandroid.easy.GuideItem
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R

/**
 * @author haoge on 2018/10/31
 */
class EasyGuideLayerActivity: BaseActivity() {
    private val anchor by lazy { findViewById<View>(R.id.layer_layout) }
    private val guideShownCallback = { shown:Boolean ->
        EasyToast.DEFAULT.show("蒙层已${if (shown) "展示" else "消失"}")
    }

    override fun getLayoutId(): Int = R.layout.activity_guide_layer

    @OnClick(R.id.show_simple_guide)
    fun showSimpleGuide() {
        createDefaultGuide().show()
    }

    @SuppressLint("RtlHardcoded")
    private val gravities = listOf(
            Gravity.LEFT or Gravity.TOP to "左上",
            Gravity.TOP to "顶部",
            Gravity.RIGHT or Gravity.TOP to "右上",
            Gravity.LEFT to "左边",
            Gravity.RIGHT to "右边",
            Gravity.LEFT or Gravity.BOTTOM to "左下",
            Gravity.BOTTOM to "底部",
            Gravity.BOTTOM or Gravity.RIGHT to "右下"
    )

    @OnClick(R.id.show_with_activity)
    fun showWithActivity() {
        val item = GuideItem.newInstance(findViewById<View>(R.id.layer_layout))
                .setGravity(Gravity.BOTTOM)
                .setLayout(R.layout.guide_text_layout)
                .setHighLightShape(GuideItem.SHAPE_OVAL)

        item.setOnViewAttachedListener { view, controller ->
            (view as TextView).text = "此处展示下方的各种蒙层展示效果"
        }
        EasyGuideLayer.with(this).addItem(item)
                .setDismissOnClickOutside(true)
                .show()
    }

    @OnClick(R.id.show_with_gravities)
    fun showWithGravity() {
        var index = 0

        val item = GuideItem.newInstance(findViewById<View>(R.id.anchor_center))
        item.setGravity(gravities[index].first).setLayout(R.layout.guide_text_layout)

        item.setOnViewAttachedListener { view, controller ->
            (view as TextView).text = gravities[index].second
            view.setOnClickListener {
                // 点击后重置gravity信息并重新展示
                try {
                    index++
                    item.setGravity(gravities[index].first)
                    controller.getGuideLayer().show()
                } catch (e:IndexOutOfBoundsException) {
                    controller.dismiss()
                }
            }
        }

        createDefaultGuide()
                .addItem(item)
                .show()
    }

    @OnClick(R.id.show_with_multiple_highlight)
    fun showWithHighlights() {
        val layer = createDefaultGuide()
        layer.setBackgroundColor(0x66000000) // 背景色调深点便于查看高亮块
        val topItem = GuideItem.newInstance(findViewById<View>(R.id.anchor_top))
                .setHighLightShape(GuideItem.SHAPE_OVAL)
                .setOnHighLightClickListener { EasyToast.DEFAULT.show("顶部高亮区域被点击") }

        val centerItem = GuideItem.newInstance(findViewById<View>(R.id.anchor_center))
                .setHighLightShape(GuideItem.SHAPE_RECT)
                .setOnHighLightClickListener { EasyToast.DEFAULT.show("中央高亮区域被点击") }

        val bottomItem = GuideItem.newInstance(findViewById<View>(R.id.anchor_bottom))
                .setOnDrawHighLightCallback { canvas, rect, paint ->
                    canvas.drawRoundRect(rect, 30f, 30f, paint)
                }.setOnHighLightClickListener { EasyToast.DEFAULT.show("底部高亮区域被点击") }

        layer.addItem(topItem).addItem(centerItem).addItem(bottomItem).show()
    }

    @OnClick(R.id.show_with_offset)
    fun showWithOffset() {
        val item = GuideItem.newInstance(findViewById<View>(R.id.anchor_center))
                .setLayout(R.layout.guide_text_layout)
                .setOnViewAttachedListener { view, controller ->
                    (view as TextView).text = "中央展示hehehehe "
                }.setOffsetProvider { point, rectF, view ->
                    // 在此根据具体尺寸计算出中央位置
                    point.offset(((rectF.width() - view.width) / 2).toInt(), 0)
                }.setGravity(Gravity.TOP)

        createDefaultGuide().addItem(item).show()
    }

    private fun createDefaultGuide() = EasyGuideLayer.with(anchor)
            .setBackgroundColor(0x33000000)
            .setOnGuideShownListener(guideShownCallback)
            .setDismissOnClickOutside(true)
}

