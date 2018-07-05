package com.haoge.easyandroid.easy

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @author haoge on 2018/7/5
 */
@RunWith(JUnit4::class)
class EasyReflectWithKt {

    @Test
    fun samples() {
        val reflect = EasyReflect.create(Example())
        val proxy = reflect.proxy(Proxy::class.java)
        proxy.defaultFunc()
    }
}

class Example {
    fun defaultFunc() {
        print("Example.defaultFunc")
    }
}

interface Proxy {
    fun defaultFunc() {
        print("默认实现")
    }
}