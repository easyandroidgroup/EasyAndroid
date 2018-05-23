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

    @OnClick(R.id.createTypes)
    fun createTypes() {
        println("第一种：只通过对应的class进行创建。")
        println(EasyReflect.create(Test::class.java))
        println("第二种：通过具体的实例进行创建。")
        println(EasyReflect.create(Test()))
        println("第三种：通过完整类名进行创建")
        println(EasyReflect.create("com.haoge.sample.easyandroid.activities.Test"))
        println("第四种：先通过对应class创建后再进行实例化")
        println(EasyReflect.create(Test::class.java).instance("构造函数第一个参数"))
    }

    @OnClick(R.id.fieldReflect)
    fun fieldReflect() {
        val reflect = EasyReflect.create(Test())

        println("直接使用EasyReflect进行数据操作")
        println(reflect.setField("name", "直接通过EasyReflect进行重置"))

        println("直接使用EasyReflect获取变量数据")
        println("获取的变量数据为：${reflect.getFieldValue<String>("name")}")

        val field = reflect.getField("name")
        println("name字段类型是：${field.field.type}")
        println("name字段值是：${field.getValue<String>()}")
        field.setValue("重置名字")
        println("重置后的字段值是：${field.getValue<String>()}")
    }

    @OnClick(R.id.methodReflect)
    fun methodReflect(){
        val reflect = EasyReflect.create(Test())
        println("使用EasyReflect执行方法：")
        reflect.call("toString")
        println("使用EasyReflect执行方法并获取返回数据")
        println(reflect.callWithReturn("toString"))
        val method = reflect.getMethod("toString")
        println("使用MethodReflect执行方法")
        method.call()
        println("使用MethodReflect执行方法并获取返回数据")
        println(method.callWithReturn())
    }
}

class Test(val name:String){
    constructor():this("默认名字")

    override fun toString(): String {
        println("toString方法被调用")
        return "Test(name='$name')"
    }

}