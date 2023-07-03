package com.nick.vod.ui

import android.Manifest
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ScreenUtils
import com.nick.music.ui.dialog.MusicDialog
import com.nick.vod.R
import com.nick.vod.databinding.ActivityVodBinding
import com.nick.vod.ui.dialog.TwoPlayerDialog
import com.nick.vod.ui.dialog.VodDialog
import com.nick.vod.view.LiveGestureControlLayer
import com.nick.vod.wiget.GestureMessageCenter

class VodActivity : AppCompatActivity(),LiveGestureControlLayer.GestureCallBack {

    private val mBinding by lazy { ActivityVodBinding.inflate(layoutInflater) }
//    private val mVodDialog by lazy { VodDialog() }
    private val mVodDialog by lazy { TwoPlayerDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initListener()
        requestPermission()
    }

    private fun initListener(){
        mBinding.apply {
            btnShowDialog.setOnClickListener {
                mVodDialog.show(supportFragmentManager,"VodDialog")
            }
        }
        GestureMessageCenter.registerCallBack(this)
    }

    private fun requestPermission(){
        if (!PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)){
            PermissionUtils.permission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                .callback(object : PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                    }

                    override fun onDenied() {
                        finish()
                    }

                })
                .request()
            return
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (ScreenUtils.isLandscape()){
            BarUtils.setStatusBarVisibility(this,false)
            mVodDialog.showStatus(false)
        }else{
            BarUtils.setStatusBarVisibility(this,true)
            mVodDialog.showStatus(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GestureMessageCenter.removeCallBack(this)
    }

    override fun fullScreen() {
        if (ScreenUtils.isLandscape()){
            ScreenUtils.setPortrait(this)
        }else{
            ScreenUtils.setLandscape(this)
        }
    }

}