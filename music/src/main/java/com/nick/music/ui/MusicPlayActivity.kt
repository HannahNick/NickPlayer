package com.nick.music.ui

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nick.music.databinding.ActivityMusicMainBinding
import com.nick.music.receiver.HomeReceiver
import com.nick.music.ui.dialog.MusicDialog

class MusicPlayActivity : AppCompatActivity() {
    private val mBinding by lazy { ActivityMusicMainBinding.inflate(layoutInflater) }
    private val mMusicDialog by lazy { MusicDialog() }
    //Home键监听
    private val mHomeReceiver = HomeReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initListener()
        registerReceiver(mHomeReceiver, IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mHomeReceiver)
    }

    private fun initListener(){
        mBinding.apply {
            btnShowDialog.setOnClickListener {
                mMusicDialog.show(supportFragmentManager,"MusicDialog")
            }
        }
    }


}