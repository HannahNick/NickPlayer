package com.xyz.nickplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nick.music.databinding.ActivityMusicMainBinding
import com.nick.music.ui.dialog.MusicDialogInstance

class MainActivity : AppCompatActivity() {

    private val mBinding by lazy { ActivityMusicMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initListener()
    }

    private fun initListener(){
        mBinding.apply {
            btnShowDialog.setOnClickListener {
                MusicDialogInstance.show(supportFragmentManager)
            }
        }
    }
}