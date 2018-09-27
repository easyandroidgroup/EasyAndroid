package com.haoge.sample.easyandroid.activities

import android.os.Bundle
import android.widget.TextView
import com.haoge.easyandroid.easy.EasyImageGetter
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R

/**
 * @author haoge on 2018/9/26
 */
class EasyImageGetterActivity: BaseActivity() {

    private val htmlLoader = EasyImageGetter.create()
            .setPlaceHolder(R.drawable.placeholder)
            .setError(R.drawable.error)
    private val result by lazy { findViewById<TextView>(R.id.result) }
    private val html = """
<h5>asset图片加载示例</h5>
<img src="file:///android_asset/imagegetter/cat.png">
<h5>http图片加载示例</h5>
<img src="http://www.w3school.com.cn/i/eg_tulip.jpg">
    """.trimIndent()

    override fun initPage(savedInstanceState: Bundle?) {
        htmlLoader.loadHtml(html, result)
    }

    override fun getLayoutId() = R.layout.activity_image_getter
}