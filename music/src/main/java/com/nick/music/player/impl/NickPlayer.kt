package com.nick.music.player.impl

import android.media.MediaPlayer
import com.blankj.utilcode.util.LogUtils
import com.nick.music.entity.MusicVo
import com.nick.music.player.PlayerControl

class NickPlayer: PlayerControl{
    private val mediaPlayer = MediaPlayer()
    private val musicData = ArrayList<MusicVo>()
    private var index: Int = -1
    private var initSourceFlag = false



    override fun play() {
        if (initSourceFlag && !mediaPlayer.isPlaying){
            mediaPlayer.start()
        }else{
            mediaPlayer.setDataSource(musicData[0].url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                LogUtils.i("播放器准备完成，开始播放")
                mediaPlayer.start()
            }
        }
        initSourceFlag = true
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun seek(num: Int) {
        mediaPlayer.seekTo(num)
    }

    override fun next() {
        if (index+1>=musicData.size){
            return
        }
        val musicVo = musicData[index]
        mediaPlayer.setDataSource(musicVo.url)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    override fun last() {
        if (index-1<0){
            return
        }
        val musicVo = musicData[index]
        mediaPlayer.setDataSource(musicVo.url)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    override fun playSource(musicVo: MusicVo) {
        musicData.add(0,musicVo)
        mediaPlayer.setDataSource(musicVo.url)
        mediaPlayer.prepare()
        mediaPlayer.start()
        initSourceFlag = true
    }

    override fun replay() {
        mediaPlayer.stop()
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    override fun setPlayList(data: List<MusicVo>) {
        musicData.clear()
        musicData.addAll(data)
    }

    override fun getCurrentInfo() {
    }
}