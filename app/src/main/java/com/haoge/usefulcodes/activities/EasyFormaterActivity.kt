package com.haoge.usefulcodes.activities

import android.os.Bundle
import android.widget.TextView
import butterknife.OnClick
import com.alibaba.fastjson.JSON
import com.haoge.usefulcodes.BaseActivity
import com.haoge.usefulcodes.R
import com.haoge.usefulcodes.utils.easy.EasyFormatter
import com.haoge.usefulcodes.utils.easy.format

/**
 * @author haoge on 2018/5/11
 */
class EasyFormaterActivity:BaseActivity() {

    val mResult by lazy { findViewById<TextView>(R.id.result) }
    val mDefaultFormatter:EasyFormatter = EasyFormatter.DEFAULT
    val mCustomformatter by lazy {
        val builder = EasyFormatter.newBuilder()
        builder.maxLines = 10 // 指定格式化后最大行数为10.
        builder.maxArraySize = 4 // 指定允许的最大数组型数据长度为4.若超出此限制。则不进行换行处理
        builder.maxMapSize = 4 // 指定允许的最大对象型数据长度为4，若超出此限制。则不进行换行处理
        return@lazy builder.build()
    }

    var mUsedFormatter:EasyFormatter = mDefaultFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formater)
    }

    @OnClick(R.id.usedCustomFormatter)
    fun usedCustomFormatter() {
        mUsedFormatter = mCustomformatter
    }

    @OnClick(R.id.usedDefaultFormatter)
    fun usedDefaultFormatter() {
        mUsedFormatter = mDefaultFormatter
    }

    @OnClick(R.id.formatSimpleList)
    fun formatSimpleList() {
        mResult.text = listOf("Hello", "kotlin", "new", "world").format()
    }

    @OnClick(R.id.formatSimpleMap)
    fun formatSimpleMap() {
        mResult.text = mUsedFormatter.format(mapOf(
                Pair(1, "1"),
                Pair(2, "2"),
                Pair(3, "3"),
                Pair(4, "4"),
                Pair(5, "5")
        ))
    }

    @OnClick(R.id.formatJSONArray)
    fun formatJSONArray() {
        val json = JSON.toJSONString(
                listOf("Hello", "kotlin", "new", "world", "JSON", "Array")
        )

        mResult.text = mUsedFormatter.format(json)
    }

    @OnClick(R.id.formatJSONObject)
    fun formatJSONObject() {
        val json = JSON.toJSONString(
                mapOf(
                        Pair("1", 1),
                        Pair("2", 2),
                        Pair("3", 3),
                        Pair("4", 4)
                )
        )


        mResult.text = mUsedFormatter.format(json)
    }

    @OnClick(R.id.formatBean)
    fun formatBean() {
        mResult.text = mUsedFormatter.format(Age(100))
    }

    @OnClick(R.id.formatException)
    fun formatException() {
        mResult.text = mUsedFormatter.format(Exception())
    }

    @OnClick(R.id.formatComplex)
    fun formatComplex() {

        val message = mutableListOf<Any>()
        message.add(1)
        message.add("Haoge")
        message.add(listOf(1,2,3,4))
        message.add(mapOf(Pair("1", 1), Pair("2", 2)))
        message.add(JSON.toJSONString(message))
        message.add(User("King", Address("Earth", Age(100))))

        val result = mUsedFormatter.format(message)
        mResult.text = result
    }

    fun escape(source:String):String {
        val chars = source.toCharArray()
        for (value in chars) {
            println(value)
        }
        return ""
    }

    data class User(var name:String, var address:Address)

    data class Address(var address:String, var age:Age)

    data class Age(var age:Int)
}