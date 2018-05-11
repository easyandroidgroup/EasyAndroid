package com.haoge.usefulcodes.activities

import android.os.Bundle
import android.widget.TextView
import butterknife.OnClick
import com.alibaba.fastjson.JSON
import com.haoge.usefulcodes.BaseActivity
import com.haoge.usefulcodes.R
import com.haoge.usefulcodes.utils.tools.format

/**
 * @author haoge on 2018/5/11
 */
class EasyFormaterActivity:BaseActivity() {

    val mResult by lazy { findViewById<TextView>(R.id.result) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formater)
    }

    @OnClick(R.id.formatSimpleList)
    fun formatSimpleList() {
        mResult.text = listOf("Hello", "kotlin", "new", "world").format()
    }

    @OnClick(R.id.formatSimpleMap)
    fun formatSimpleMap() {
        mResult.text = mapOf(
                Pair(1, "1"),
                Pair(2, "2"),
                Pair(3, "3"),
                Pair(4, "4")
        ).format()
    }

    @OnClick(R.id.formatJSONArray)
    fun formatJSONArray() {
        val json = JSON.toJSONString(
                listOf("Hello", "kotlin", "new", "world")
        )

        mResult.text = json.format()
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

        mResult.text = json.format()
    }

    @OnClick(R.id.formatBean)
    fun formatBean() {
        mResult.text = Age(100).format()
    }

    @OnClick(R.id.formatComplex)
    fun formatComplex() {
        val message = mutableListOf<Any>()
        message.add(1)
        message.add("Haoge")
        message.add(listOf(1,2,3,4))
        message.add(mapOf(Pair("1", 1), Pair("2", 2)))
        message.add(JSON.toJSONString(message))
        mResult.text = message.format()
    }

    data class User(var name:String, var address:Address)

    data class Address(var address:String, var age:Age)

    data class Age(var age:Int)
}