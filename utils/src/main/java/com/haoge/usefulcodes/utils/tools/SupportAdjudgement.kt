package com.haoge.usefulcodes.utils.tools

/**
 * @author haoge on 2018/5/15
 */
object SupportAdjudgement {
    val FASTJSON:Boolean by lazy { return@lazy isExist("com.alibaba.fastjson.JSON") }

    private fun isExist(name:String):Boolean {
        return try {
            Class.forName(name)
            true
        } catch (e:Exception) {
            false
        }
    }
}