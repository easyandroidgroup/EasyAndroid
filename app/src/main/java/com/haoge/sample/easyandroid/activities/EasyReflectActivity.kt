package com.haoge.sample.easyandroid.activities

import android.os.Bundle
import butterknife.OnClick
import com.haoge.easyandroid.easy.EasyReflect
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R

/**
 * @author haoge on 2018/5/22
 */
class EasyReflectActivity:BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reflect)
    }

    @OnClick(R.id.simple)
    fun simple() {
        var reflect = EasyReflect.create("com.haoge.sample.easyandroid.activities.Data").instance("Haoge")
        println(reflect)
        println(reflect.field("name"))
        println(reflect.call("setName", "Kotlin"))
        println(reflect.method("getName"))
    }
}

data class Data(var name:String)