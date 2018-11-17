package com.haoge.sample.easyandroid.extensions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.haoge.easyandroid.easy.EasyActivityResult
import com.haoge.easyandroid.easy.EasyBundle

// EasyBundle extensions:
fun Bundle.put(key:String, value:Any?) = EasyBundle.create(this).put(key, value)
fun Bundle.put(vararg items:Pair<String, Any?>) = EasyBundle.create(this).put(*items)
fun Bundle.put(map:Map<String, Any?>) = EasyBundle.create(this).put(map)

inline fun <reified T> Bundle.get(key:String) = EasyBundle.create(this).get<T>(key)
inline fun <reified T> Bundle.get(key: String, defValue:T) = EasyBundle.create(this).get(key, defValue)

// EasyActivityResult:
fun Activity.startActivity(intent:Intent, result:(resultCode:Int, data:Intent?) -> Unit) =
        EasyActivityResult.startActivity(this, intent, result)
