package com.nick.music.server.binder.impl

import android.os.Binder
import com.nick.music.entity.MusicVo
import com.nick.music.player.PlayerControl
import com.nick.music.server.binder.MusicBinder

class MusicServerBinder(val playerControl: PlayerControl) :Binder(),MusicBinder{

    override fun play() {
        playerControl.play()
    }

    override fun pause() {
        playerControl.pause()
    }

    override fun seek(num: Int) {
        playerControl.seek(num)
    }

    override fun next() {
        playerControl.next()
    }

    override fun last() {
        playerControl.last()
    }

    override fun playSource(musicVo: MusicVo) {
        playerControl.playSource(musicVo)
    }

    override fun replay() {
        playerControl.replay()
    }

    override fun setPlayList(data: List<MusicVo>) {
        playerControl.setPlayList(data)
    }

}