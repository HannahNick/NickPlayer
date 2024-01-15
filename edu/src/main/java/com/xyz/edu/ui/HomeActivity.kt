package com.xyz.edu.ui

import android.os.Bundle
import com.bumptech.glide.Glide
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayerControl
import com.nick.music.player.impl.NickExoPlayer
import com.xyz.edu.contract.IHomeC
import com.xyz.edu.databinding.ActivityHomeBinding
import com.xyz.edu.model.HomeModel
import com.xyz.edu.presenter.HomePresenter

class HomeActivity : BaseActivity<IHomeC.Presenter>(),IHomeC.View, PlayInfoCallBack {

    private val mBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    private val audioPlayer: PlayerControl by lazy { NickExoPlayer(this) }
    override fun createPresenter(): IHomeC.Presenter {
       return HomePresenter(this,this,HomeModel(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        Glide.with(this)
            .load("https://i2.hdslb.com/bfs/archive/6c92eb1dadedd6d1e54767c97ce5c19e53807ff1.jpg")
            .into(mBinding.ivLessonImg)
        audioPlayer.setPlayList(arrayListOf())
        audioPlayer.setPlayWhenReady(true)

    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }

    override fun playPosition(position: Int) {

    }

    override fun prepareStart(playInfo: PlayInfo) {

    }

    override fun startPlay(position: Long) {

    }

    override fun playEnd(playIndex: Int) {
        // TODO: 切图，换文本
    }
}