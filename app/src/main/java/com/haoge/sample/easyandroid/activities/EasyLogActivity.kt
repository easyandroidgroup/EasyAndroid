package com.haoge.sample.easyandroid.activities

import android.os.Bundle
import butterknife.OnClick
import com.alibaba.fastjson.JSON
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.easyandroid.easy.EasyLog
import com.haoge.sample.easyandroid.R

/**
 * @author haoge on 2018/5/11
 */
class EasyLogActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
    }

    @OnClick(R.id.printSingleLine)
    fun printSingleLine() {
        EasyLog.DEFAULT.d("这里是单行数据")
    }

    @OnClick(R.id.printMultipleLine)
    fun printMultipleLine() {
        EasyLog.DEFAULT.d("""
                >"第一行数据",
                >"第二行数据",
                >"第三行数据".
        """.trimMargin(">"))
    }

    @OnClick(R.id.printShortList)
    fun printShortList() {
        EasyLog.DEFAULT.d(listOf(1,2,3,4,5,6,7))
    }

    @OnClick(R.id.printLongList)
    fun printLongList() {
        // 默认输出的列表最长长度为10，所以这里数量超出限制时。将会进行平铺展示。
        EasyLog.DEFAULT.d(listOf(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15))
    }

    @OnClick(R.id.printJSONObject)
    fun printJSONObject() {
        // 默认输出的列表最长长度为10，所以这里数量超出限制时。将会进行平铺展示。
        val array = arrayOf("Hello", "World", "Kotlin", "EasyLog")
        EasyLog.DEFAULT.d(JSON.toJSONString(array))
    }

    @OnClick(R.id.printJSONArray)
    fun printJSONArray() {
        // 默认输出的列表最长长度为10，所以这里数量超出限制时。将会进行平铺展示。
        val any = mapOf(Pair("key1", "Hello"), Pair("key2", "World"), Pair("key3", "Kotlin"), Pair("key4", "EasyLog"))
        EasyLog.DEFAULT.d(JSON.toJSONString(any))
    }

    @OnClick(R.id.printWithCustomTag)
    fun printWithCustomTag() {
        EasyLog.DEFAULT.tag("Custom").d("This is custom tag log")
    }
}