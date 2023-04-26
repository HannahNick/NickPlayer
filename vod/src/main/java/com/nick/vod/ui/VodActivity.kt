package com.nick.vod.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import com.blankj.utilcode.util.PermissionUtils
import com.nick.music.ui.dialog.MusicDialog
import com.nick.vod.R
import com.nick.vod.databinding.ActivityVodBinding
import com.nick.vod.ui.dialog.VodDialog

class VodActivity : AppCompatActivity() {

    private val mBinding by lazy { ActivityVodBinding.inflate(layoutInflater) }
    private val mVodDialog by lazy { VodDialog() }

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
}