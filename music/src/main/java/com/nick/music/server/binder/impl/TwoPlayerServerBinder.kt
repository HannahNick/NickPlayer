package com.nick.music.server.binder.impl

import android.os.Binder
import android.view.SurfaceHolder
import com.nick.base.vo.MusicVo
import com.nick.music.player.PlayerControl
import com.nick.music.server.PlayMode
import com.nick.music.server.binder.TwoPlayerBinder

class TwoPlayerServerBinder(private val vodControl: PlayerControl, private val musicControl: PlayerControl) : Binder(), TwoPlayerBinder {
    override fun play(index: Int) {
        vodControl.play(index)
        musicControl.play(index)
    }

    override fun pause() {
        vodControl.pause()
        musicControl.pause()
    }


    override fun setPlayMode(playMode: PlayMode) {
        vodControl.setPlayMode(playMode)
        musicControl.setPlayMode(playMode)
    }

    override fun attachSurfaceHolder(holder: SurfaceHolder) {
        vodControl.attachSurfaceHolder(holder)
    }

    override fun setMusicPlayList(data: List<MusicVo>) {
        musicControl.setPlayList(data)
    }

    override fun setVodPlayerList(data: List<MusicVo>) {
        vodControl.setPlayList(data)
    }

}