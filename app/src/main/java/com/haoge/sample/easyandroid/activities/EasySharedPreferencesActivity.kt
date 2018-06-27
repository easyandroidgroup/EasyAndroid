package com.haoge.sample.easyandroid.activities

import android.content.Context
import butterknife.OnClick
import com.haoge.easyandroid.easy.*
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R

/**
 * @author haoge on 2018/6/26
 */
class EasySharedPreferencesActivity:BaseActivity() {
    override fun getLayoutId(): Int = R.layout.activity_shared_preferences

    @OnClick(R.id.createBySystem)
    fun createBySystem() {
        getSharedPreferences("example_shared_data", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("mBool", true)
                .putFloat("mFloat", 4f)
                .putInt("mInt", 100)
                .putLong("mLong", 24L)
                .putString("mStr", "Hello")
                .putStringSet("mStrSet", setOf("Hello", "World", "123"))
                .apply()
    }

    @OnClick(R.id.createByEasy)
    fun createByEasy() {
        val entity = EasySharedPreferences.load(Entity::class.java)
        EasyLog.DEFAULT.immediate(true).e("读取的entity实例为：\n %s", entity)
        entity.mStr = "modify by loaded entity"
        entity.apply()
        EasyLog.DEFAULT.e("操作后的entity实例为：\n %s", entity)
    }
}

@PreferenceAnnotation("example_shared_data")
class Entity:PreferenceSupport() {
    var mBool:Boolean = true
    var mFloat:Float = 0f
    var mInt:Int = 0
    var mLong:Long = 0L
    @PreferenceIgnore
    var mStr:String = ""
}