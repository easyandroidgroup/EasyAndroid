package com.haoge.sample.easyandroid.activities

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import butterknife.OnClick
import com.google.gson.Gson
import com.haoge.easyandroid.easy.BundleField
import com.haoge.easyandroid.easy.EasyBundle
import com.haoge.easyandroid.easy.EasyLog
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R
import java.io.Serializable

/**
 * @author haoge on 2018/6/14
 */
class EasyBundleActivity:BaseActivity() {
    override fun getLayoutId() = R.layout.activity_bundle

    @OnClick(R.id.savePrimite)
    fun savePrimite() {
        // 基本数据类型存储测试
        val bundle = EasyBundle.create(null).put("byte", 1.toByte())
                .put("short", 2.toShort())
                .put("int", 3)
                .put("long", 4.toLong())
                .put("float", 5.toFloat())
                .put("double", 6.toDouble())
                .put("char", 'c')
                .put("boolean", true)
                .put("String", "String")
                .bundle
        EasyLog.DEFAULT.e("存储基本数据类型后的bundle数据为==>$bundle \n 要求长度为9 实际为${bundle.size()}")
    }

    @OnClick(R.id.savePrimiteArray)
    fun savePrimiteArray() {
        // 基本数据类型数组存储测试
        val bundle = EasyBundle.create(null)
                .put("IntArray", intArrayOf(1,2,3))
                .put("LongArray", longArrayOf(1,2,3))
                .put("FloatArray", floatArrayOf(1f,2f,3f))
                .put("DoubleArray", doubleArrayOf(1.toDouble(),2.toDouble(),3.toDouble()))
                .put("CharArray", charArrayOf('a','b','c'))
                .put("ShortArray", shortArrayOf(1,2,3))
                .put("BooleanArray", booleanArrayOf(true,false,true))
                .put("StringArray", arrayOf("hello", "world"))
                .bundle

        EasyLog.DEFAULT.e("存储基本数据类型数组后的bundle数据为==>$bundle \n 要求长度为8 实际为${bundle.size()}")
    }

    @OnClick(R.id.saveSerializable)
    fun saveSerializable() {
        // 其他序列化数据存储测试
        val bundle = EasyBundle.create(null)
                .put("parcelable", ParcelableSubclass())
                .put("serializable", SerializableSubclass())
                .put("arrayParcelable", arrayOf(ParcelableSubclass(), ParcelableSubclass()))
                .put("bundle", Bundle())
                .bundle
        EasyLog.DEFAULT.e("存储可序列化数据后 ==> $bundle \n 要求长度为4，实际为${bundle.size()}")
    }

    @OnClick(R.id.saveUnSerializable)
    fun saveUnSerializable() {
        val bundle = EasyBundle.create(null)
                .put("info", Info("存储的名字"))
                .bundle
        EasyLog.DEFAULT.e("存储后的info数据为 ==> ${bundle.get("info")}")
    }

    @OnClick(R.id.restorePrimite)
    fun restorePrimite() {
        val easyBundle = EasyBundle.create(null)
        // 使用空bundle进行读取。测试读取基本数据类型时。是否正确返回默认值
        EasyLog.DEFAULT.e("读取空Byte数据：${easyBundle.get("key", Byte::class.java)}")
        EasyLog.DEFAULT.e("读取空char数据：${easyBundle.get("key", Char::class.java)}")
        EasyLog.DEFAULT.e("读取空Short数据：${easyBundle.get("key", Short::class.java)}")
        EasyLog.DEFAULT.e("读取空Int数据：${easyBundle.get("key", Int::class.java)}")
        EasyLog.DEFAULT.e("读取空Long数据：${easyBundle.get("key", Long::class.java)}")
        EasyLog.DEFAULT.e("读取空Float数据：${easyBundle.get("key", Float::class.java)}")
        EasyLog.DEFAULT.e("读取空Double数据：${easyBundle.get("key", Double::class.java)}")
        EasyLog.DEFAULT.e("读取空Boolean数据：${easyBundle.get("key", Boolean::class.java)}")
    }

    @OnClick(R.id.testForConverter)
    fun testForConverter() {
        val easyBundle = EasyBundle.create(null)
                .put("int", 10)// 测试自动基本数据类型转换
                // 测试json自动转换
                .put("jsonOfInfo", Gson().toJson(Info("不可序列化的类, 将转换为json存储")))

        EasyLog.DEFAULT.e("从json反序列化回来后的info数据：${easyBundle.get<Info>("jsonOfInfo")} \n bundle中保存的info原始数据为：${easyBundle.bundle["jsonOfInfo"]}")
        EasyLog.DEFAULT.e("从json反序列化回来后的int数据：${easyBundle.get<Int>("int")}")
    }

    @OnClick(R.id.testForInjector)
    fun testForInjector() {
        val entity = TestInjector("this is name of Injector",
                100,
                ParcelableSubclass("Hello parcelable"),
                SerializableSubclass("Hello Serializable!"),
                Info())

        val bundle = EasyBundle.toBundle(entity, null)
        EasyLog.DEFAULT.e("注入后的bundle数据：\n$bundle")

        // 对bundle数据部分数据进行重置：
        EasyBundle.create(bundle).put("name", "重置的name属性")
                .put("age", 18)

        // 再次将数据反注入回实体类中：
        EasyBundle.toEntity(entity, bundle)
        EasyLog.DEFAULT.e("反注入回后的entity实例：\n$entity")
    }


}

data class ParcelableSubclass(val name: String = "this is a subclass of  Parcelable"):Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableSubclass> {
        override fun createFromParcel(parcel: Parcel): ParcelableSubclass {
            return ParcelableSubclass(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableSubclass?> {
            return arrayOfNulls(size)
        }
    }
}

data class SerializableSubclass(val name: String = "this is a subclass of Serializable"):Serializable

class Info(val name:String?) {
    constructor():this("默认名字")// JSON反序列化时需要空构造

    override fun toString(): String {
        return "Info(name=$name)"
    }

}

class TestInjector(@BundleField var name:String,
                   @BundleField var age:Int,
                   @BundleField var parcelable:ParcelableSubclass,
                   @BundleField var serializable: SerializableSubclass,
                   @BundleField var unserial:Info) {
    override fun toString(): String {
        return "TestInjector(name='$name', age=$age, parcelable=$parcelable, serializable=$serializable, unserial=$unserial)"
    }
}