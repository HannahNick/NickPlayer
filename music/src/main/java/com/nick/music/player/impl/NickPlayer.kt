package com.nick.music.player.impl

import android.media.MediaPlayer
import com.nick.music.entity.MusicVo
import com.nick.music.player.PlayerControl

class NickPlayer: PlayerControl{
    private val mediaPlayer = MediaPlayer()
    private val musicData = ArrayList<MusicVo>()
    private var index: Int = -1

    override fun play() {
        mediaPlayer.prepare()
        mediaPlayer.start()
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