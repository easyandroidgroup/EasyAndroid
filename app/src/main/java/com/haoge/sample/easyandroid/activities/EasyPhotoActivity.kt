package com.haoge.sample.easyandroid.activities

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.TextView
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.haoge.easyandroid.easy.EasyLog
import com.haoge.easyandroid.easy.EasyPermissions
import com.haoge.easyandroid.easy.EasyMedia
import com.haoge.easyandroid.easy.EasyToast
import com.haoge.sample.easyandroid.BaseActivity
import com.haoge.sample.easyandroid.R
import java.io.File


/**
 * 创建日期：2018/8/22 上午 10:42
 * @author：Vincent
 * 备注：
 */
class EasyPhotoActivity : BaseActivity() {

    private val switcher by lazy { findViewById<TextView>(R.id.indicate_img_path) }
    private var indicatePath:String? = null
    private val photo = EasyMedia().setCallback {
        showImg(it)
    }

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

    @OnClick(R.id.indicate_img_path)
    fun indicateImgPath() {
        indicatePath = if (indicatePath == null) {
            switcher.text = "指定图片缓存地址"
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "EasyPhotoTest.jpg").absolutePath
        } else {
            switcher.text = "使用默认缓存地址"
            null
        }

        photo.setFilePath(indicatePath)
    }

    @OnClick(R.id.takePhoto)
    fun takePhoto() {
        photo.setCrop(false).takePhoto(this)
    }

    @OnClick(R.id.selectPhoto)
    fun selectPhoto() {
        photo.setCrop(false).selectPhoto(this)
    }




    @OnClick(R.id.takePhoto_zoom)
    fun takePhotoZoom() {
        photo.setCrop(true).takePhoto(this)
    }

    @OnClick(R.id.selectPhoto_zoom)
    fun selectPhotoZoom() {
        photo.setCrop(true).selectPhoto(this)
    }
    @OnClick(R.id.getPhoto)
    fun getPhoto(){
        photo.getImage(this)
    }

    @OnClick(R.id.takeAudio)
    fun takeAudio(){
        photo.takeAudio(this)
    }

    @OnClick(R.id.selectAudio)
    fun selectAudio() {
        photo.selectAudio(this)
    }

    @OnClick(R.id.getAudio)
    fun getAudio(){
        photo.getAudio(this)
    }
    @OnClick(R.id.takeVideo)
    fun takeVideo(){
        photo.takeVideo(this)
    }

    @OnClick(R.id.selectVideo)
    fun selectVideo() {
        photo.selectVideo(this)
    }

    @OnClick(R.id.getVideo)
    fun getVideo(){
        photo.getVideo(this)
    }

    @OnClick(R.id.selectNormalFile)
    fun selectNormalFile() {
        photo.selectFile(this)
    }

    /**
     * 加载图片
     */
    private fun showImg(outputFile: File) {
        val showImg:ImageView = findViewById(R.id.showImg)
        EasyToast.DEFAULT.show("得到的文件名为：${outputFile.absolutePath}")
        EasyLog.DEFAULT.e("得到的文件名为：${outputFile.absolutePath}")
        //加载图片
        Glide.with(showImg).load(outputFile).into(showImg)
    }



}
