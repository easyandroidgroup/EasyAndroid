package com.haoge.sample.easyandroid.activities

import android.content.Intent
import butterknife.OnClick
import com.haoge.easyandroid.easy.EasyActivityResult
import com.haoge.easyandroid.easy.EasyLog
import com.haoge.easyandroid.easy.EasyToast
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R

/**
 * @author haoge on 2018/6/2
 */
class EasyResultActivity: BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_result_callback
    }

    @OnClick(R.id.normalStart)
    fun normalStart() {
        EasyActivityResult.startActivity(this,
                Intent(this, EasyToastActivity::class.java),
                {_, _ -> EasyToast.DEFAULT.show("正常启动模式接收返回信息") })
    }

    @OnClick(R.id.violentStart)
    fun violentStart() {
        // 暴击启动测试：同时被调用启动多次，应只有第一次启动成功
        EasyActivityResult.startActivity(this,
                Intent(this, EasyToastActivity::class.java),
                {_, _ -> EasyToast.DEFAULT.show("暴击启动测试：第一次启动任务 接收返回信息") })

        EasyActivityResult.startActivity(this,
                Intent(this, EasyToastActivity::class.java),
                {_, _ -> EasyToast.DEFAULT.show("暴击启动测试：第二次启动任务 接收返回信息") })

        EasyActivityResult.startActivity(this,
                Intent(this, EasyToastActivity::class.java),
                {_, _ -> EasyToast.DEFAULT.show("暴击启动测试：第三次启动任务 接收返回信息") })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 进行返回信息过滤派发
        EasyActivityResult.dispatch(this, requestCode, resultCode, data)
    }
}