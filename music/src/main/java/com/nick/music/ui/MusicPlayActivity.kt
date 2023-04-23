package com.nick.music.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nick.music.databinding.ActivityMusicMainBinding
import com.nick.music.ui.dialog.MusicDialog

class MusicPlayActivity : AppCompatActivity() {
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