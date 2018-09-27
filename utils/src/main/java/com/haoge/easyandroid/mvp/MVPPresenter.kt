/*
 * Copyright (C) 2018 Haoge https://github.com/yjfnypeu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haoge.easyandroid.mvp

import android.content.Intent
import android.os.Bundle

/**
 * Presenter层：连接View与Model的中间件桥梁，处理一些Model相关逻辑。比如API访问等
 * @author haoge on 2018/5/29
 */
open class MVPPresenter<T:MVPView>(private var view:T?){

    fun attach(t:T) {
        this.view = t
    }
    fun detach() {
        this.view = null
    }
    fun getView() = view as T
    fun isViewAttached() = view != null
    fun getActivity() = view?.getHostActivity()?:throw RuntimeException("Could not call getActivity if the View is not attached")

    // Lifecycle delegate
    open fun onCreate(bundle: Bundle?) {}
    open fun onStart(){}
    open fun onRestart(){}
    open fun onResume(){}
    open fun onPause(){}
    open fun onStop(){}
    open fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?){}
    open fun onDestroy(){}
    open fun onSaveInstanceState(outState: Bundle?){}
    open fun onRestoreInstanceState(savedInstanceState: Bundle?){}

}
