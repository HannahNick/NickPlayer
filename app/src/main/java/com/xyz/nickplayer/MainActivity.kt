package com.xyz.nickplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ServiceUtils
import com.nick.base.BaseUrl
import com.nick.music.databinding.ActivityMusicMainBinding
import com.nick.music.entity.MusicVo
import com.nick.music.server.MusicServer
import com.nick.music.server.binder.MusicBinder
import com.nick.music.ui.dialog.MusicDialog
import com.xyz.nickplayer.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private val mBinding by lazy { ActivityMusicMainBinding.inflate(layoutInflater) }
    private val mMusicDialog by lazy { MusicDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initListener()
    }

    private fun initListener(){
        mBinding.apply {
            btnShowDialog.setOnClickListener {
                mMusicDialog.show(supportFragmentManager,"MusicDialog")
            }
        }
    }
}