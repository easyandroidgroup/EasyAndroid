package com.haoge.sample.easyandroid.activities

import android.Manifest
import android.net.Uri
import android.os.Bundle
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.haoge.easyandroid.easy.EasyPermissions
import com.haoge.easyandroid.easy.EasyPhoto
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R
import kotlinx.android.synthetic.main.activity_easy_photo.*
import java.io.File


/**
 * 创建日期：2018/8/22 上午 10:42
 * @author：Vincent
 * 备注：
 */
class EasyPhotoActivity : BaseActivity() {


    override fun getLayoutId(): Int {
        return R.layout.activity_easy_photo
    }

    override fun initPage(savedInstanceState: Bundle?) {
        //拒绝权限将无法使用
       EasyPermissions.create(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA).callback { grant ->
           //拒绝直接关闭
           if(!grant){
               finish()
           } }.request(this)

    }

    @OnClick(R.id.takePhoto)
    fun takePhoto() {
        EasyPhoto(false).setCallback { outputFile: File?, outputUri: Uri? ->
            showImg(outputFile, outputUri)
        }.takePhoto(this)

    }

    @OnClick(R.id.selectPhoto)
    fun selectPhoto() {
        EasyPhoto(false).setCallback { outputFile: File?, outputUri: Uri? ->
            showImg(outputFile, outputUri)
        }.selectPhoto(this)

    }

    @OnClick(R.id.takePhoto_zoom)
    fun takePhotoZoom() {
        EasyPhoto(true).setCallback { outputFile: File?, outputUri: Uri? ->
            showImg(outputFile, outputUri)
        }.setDimens(800, 400, 2, 1)
                .takePhoto(this)

    }

    @OnClick(R.id.selectPhoto_zoom)
    fun selectPhotoZoom() {
        EasyPhoto(true).setCallback { outputFile: File?, outputUri: Uri? ->
            showImg(outputFile, outputUri)
        }.selectPhoto(this)
    }

    /**
     * 加载图片
     */

    private fun showImg(outputFile: File?, outputUri: Uri?) {
        //加载图片
        Glide.with(showImg).load(outputFile ?: outputUri!!).into(showImg)


    }



}
