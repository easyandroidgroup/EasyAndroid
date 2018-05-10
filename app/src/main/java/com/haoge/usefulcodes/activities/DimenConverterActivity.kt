package com.haoge.usefulcodes.activities

import android.os.Bundle
import android.util.TypedValue
import android.widget.*
import butterknife.OnClick
import com.haoge.usefulcodes.BaseActivity
import com.haoge.usefulcodes.R
import com.haoge.usefulcodes.utils.components.EasyToast
import com.haoge.usefulcodes.utils.tools.DimenConverter

/**
 * @author haoge on 2018/5/10
 */
class DimenConverterActivity:BaseActivity() {

    val mInput:EditText by lazy { findViewById<EditText>(R.id.number_input) }
    val mShown:TextView by lazy { findViewById<TextView>(R.id.shown_tv) }
    val mSelector:RadioGroup by lazy { findViewById<RadioGroup>(R.id.selector) }

    var mDimenConverter:DimenConverter? = null
    var value:Float = 0.0f
    var unitName:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dimen_converter)
    }

    @OnClick(R.id.create)
    fun create() {
        value = mInput.text.toString().safeToInt().toFloat()
        val (unit:Int, name:String) = when(mSelector.checkedRadioButtonId) {
            R.id.select_PX -> Pair(TypedValue.COMPLEX_UNIT_PX, "PX")
            R.id.select_DIP -> Pair(TypedValue.COMPLEX_UNIT_DIP, "DIP")
            R.id.select_SP -> Pair(TypedValue.COMPLEX_UNIT_SP, "SP")
            R.id.select_PT -> Pair(TypedValue.COMPLEX_UNIT_PT, "PT")
            R.id.select_IN -> Pair(TypedValue.COMPLEX_UNIT_IN, "IN")
            R.id.select_MM -> Pair(TypedValue.COMPLEX_UNIT_MM, "MM")
            else -> Pair(TypedValue.COMPLEX_UNIT_PX, "PX")
        }
        unitName = name
        mDimenConverter = DimenConverter.create(value, unit)
        mShown.text = "转换器创建成功，数值为${value}单位为$unitName"
    }

    @OnClick(R.id.toPX)
    fun toPX() {
        if (checkConverterCreated()) {
            val result = """
                >原始数值为${value}单位为$unitName
                >转换后数值为${mDimenConverter?.toPX()}单位为PX
            """.trimMargin(">")
            mShown.text = result
        }
    }

    @OnClick(R.id.toSP)
    fun toSP() {
        if (checkConverterCreated()) {
            val result = """
                >原始数值为${value}单位为$unitName
                >转换后数值为${mDimenConverter?.toSP()}单位为SP
            """.trimMargin(">")
            mShown.text = result
        }
    }

    @OnClick(R.id.toDIP)
    fun toDIP() {
        if (checkConverterCreated()) {
            val result = """
                >原始数值为${value}单位为$unitName
                >转换后数值为${mDimenConverter?.toDIP()}单位为DIP
            """.trimMargin(">")
            mShown.text = result
        }
    }

    @OnClick(R.id.toPT)
    fun toPT() {
        if (checkConverterCreated()) {
            val result = """
                >原始数值为${value}单位为$unitName
                >转换后数值为${mDimenConverter?.toPT()}单位为PT
            """.trimMargin(">")
            mShown.text = result
        }
    }

    @OnClick(R.id.toIN)
    fun toIN() {
        if (checkConverterCreated()) {
            val result = """
                >原始数值为${value}单位为$unitName
                >转换后数值为${mDimenConverter?.toIN()}单位为IN
            """.trimMargin(">")
            mShown.text = result
        }
    }

    @OnClick(R.id.toMM)
    fun toMM() {
        if (checkConverterCreated()) {
            val result = """
                >原始数值为${value}单位为$unitName
                >转换后数值为${mDimenConverter?.toMM()}单位为MM
            """.trimMargin(">")
            mShown.text = result
        }
    }

    fun checkConverterCreated():Boolean {
        return if (mDimenConverter == null) {
            EasyToast.DEFAULT.show("请先创建转换器")
            false
        } else {
            true
        }
    }

}

fun String.safeToInt():Int {
    return try {
        this.toInt()
    } catch (e:Exception) {
        0
    }
}