package com.xyz.nickplayer

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.PermissionUtils
import com.nick.music.databinding.ActivityMusicMainBinding
import com.nick.music.ui.dialog.MusicDialogInstance

class MainActivity : AppCompatActivity() {

    private val mBinding by lazy { ActivityMusicMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initListener()
        getAudioPermission()
    }

    private fun getAudioPermission(){
        if (!PermissionUtils.isGranted(Manifest.permission.RECORD_AUDIO)){
            PermissionUtils.permission(
                Manifest.permission.RECORD_AUDIO)
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

    private fun initListener(){
        mBinding.apply {
            btnShowDialog.setOnClickListener {
                MusicDialogInstance.show(supportFragmentManager)
            }
        }
    }
}