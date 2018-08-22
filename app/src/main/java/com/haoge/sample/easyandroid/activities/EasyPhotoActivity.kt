package com.haoge.sample.easyandroid.activities

import android.Manifest
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import butterknife.BindView
import butterknife.OnClick
import com.haoge.easyandroid.easy.EasyPhoto
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R
import kotlinx.android.synthetic.main.activity_easy_photo.*
import java.io.File
import android.graphics.Bitmap
import android.os.Build
import android.support.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.haoge.easyandroid.easy.EasyPermissions
import com.haoge.easyandroid.easy.EasyToast


/**
 * 创建日期：2018/8/22 上午 10:42
 * @author：Vincent
 * 备注：
 */
class EasyPhotoActivity : BaseActivity() {


    override fun getLayoutId(): Int {
        return R.layout.activity_easy_photo
    }



    @OnClick(R.id.takePhoto)
    fun takePhoto() {
        //申请权限，否则打开相机
        EasyPermissions.create(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .callback { grant ->
                    if(grant){
                        //拍照
                        EasyPhoto.create(false).callback { outputFile: File?, outputUri: Uri? ->
                            showImg(outputFile, outputUri)
                        }.takePhoto(this)
                    }else{
                        EasyToast.DEFAULT.show("权限申请失败")
                    }
                }
                .request(this)

    }

    @OnClick(R.id.selectPhoto)
    fun selectPhoto() {
        EasyPhoto.create(false).callback { outputFile: File?, outputUri: Uri? ->
            showImg(outputFile, outputUri)
        }.selectPhoto(this)

    }

    @OnClick(R.id.takePhoto_zoom)
    fun takePhotoZoom() {
        //申请权限，否则打开相机
        EasyPermissions.create(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .callback { grant ->
                    if(grant){
                        //拍照且自定义裁剪尺寸
                        EasyPhoto.create(true).callback { outputFile: File?, outputUri: Uri? ->
                            showImg(outputFile, outputUri)
                        }.demisn(800, 400, 2, 1)
                                .takePhoto(this)
                    }else{
                        EasyToast.DEFAULT.show("权限申请失败")
                    }
                }
                .request(this)

    }

    @OnClick(R.id.selectPhoto_zoom)
    fun selecePhotoZoom() {
        EasyPhoto.create(true).callback { outputFile: File?, outputUri: Uri? ->
            showImg(outputFile, outputUri)
        }.selectPhoto(this)
    }

    /**
     * 加载图片
     */

    private fun showImg(outputFile: File?, outputUri: Uri?) {
        //申请权限，否则无法加载相册图片
        EasyPermissions.create(Manifest.permission.READ_EXTERNAL_STORAGE)
                .callback { grant ->
                    if(grant){
                        //加载图片
                        Glide.with(showImg).load(outputFile ?: outputUri!!).into(showImg)
                    }else{
                        EasyToast.DEFAULT.show("权限申请失败")
                    }
                }
                .request(this)


    }



}
