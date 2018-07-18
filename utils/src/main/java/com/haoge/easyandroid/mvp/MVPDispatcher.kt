package com.haoge.easyandroid.mvp

import android.content.Intent
import android.os.Bundle

/**
 * MVP - 用于对页面绑定的Presenter进行生命周期的派发
 * @author haoge on 2018/5/29
 */
class MVPDispatcher{

    private val presenters:MutableList<MVPPresenter<*>> = mutableListOf()

    // ==== 添加与移除Presenter ========
    /**
     * 添加新的Presenter到容器中。只允许添加已绑定过View后的。否则将毫无意义！
     */
    fun <V:MVPView> addPresenter(presenter:MVPPresenter<V>) {
        if (presenter.isViewAttached() && !presenters.contains(presenter)) {
            presenters.add(presenter)
        }
    }

    /**
     * 移除制定的Presenter实例。并将其与View解绑。
     */
    internal fun <V:MVPView> removePresenter(presenter:MVPPresenter<V>) {
        if (presenters.contains(presenter)) {
            presenters.remove(presenter)
            if (presenter.isViewAttached()) {
                presenter.detach()
            }
        }
    }

    fun dispatchOnCreate(bundle:Bundle?) {
        presenters.forEach {
            if (it.isViewAttached()) {
                it.onCreate(bundle)
            }
        }
    }

    fun dispatchOnStart() {
        presenters.forEach {
            if (it.isViewAttached()) {
                it.onStart()
            }
        }
    }

    fun dispatchOnResume() {
        presenters.forEach {
            if (it.isViewAttached()) {
                it.onResume()
            }
        }
    }

    fun dispatchOnPause() {
        presenters.forEach {
            if (it.isViewAttached()) {
                it.onPause()
            }
        }
    }

    fun dispatchOnStop() {
        presenters.forEach {
            if (it.isViewAttached()) {
                it.onStop()
            }
        }
    }

    fun dispatchOnRestart() {
        presenters.forEach {
            if (it.isViewAttached()) {
                it.onRestart()
            }
        }
    }

    fun dispatchOnDestroy() {
        presenters.forEach {
            if (it.isViewAttached()) {
                it.onDestroy()
            }
            removePresenter(it)
        }
    }

    fun dispatchOnActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        presenters.forEach {
            if (it.isViewAttached()) {
                it.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    fun dispatchOnSaveInstanceState(outState: Bundle?) {
        presenters.forEach {
            if (it.isViewAttached()) {
                it.onSaveInstanceState(outState)
            }
        }
    }

    fun dispatchOnRestoreInstanceState(savedInstanceState: Bundle?) {
        presenters.forEach {
            if (it.isViewAttached()) {
                it.onRestoreInstanceState(savedInstanceState)
            }
        }
    }
}