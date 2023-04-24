package com.nick.music.server.binder.impl

import android.os.Binder
import com.nick.music.entity.MusicVo
import com.nick.music.entity.PlayInfo
import com.nick.music.player.CurrentPositionCallBack
import com.nick.music.player.PlayerControl
import com.nick.music.server.binder.MusicBinder

class MusicServerBinder(private val playerControl: PlayerControl) :Binder(),MusicBinder{

    override fun play(index:Int) {
        playerControl.play(index)
    }

    override fun pause() {
        playerControl.pause()
    }

    override fun seek(num: Int) {
        playerControl.seek(num)
    }

    override fun playNext() {
        playerControl.next()
    }

    override fun playLast() {
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

    override fun getPlayInfo(): PlayInfo {
        return playerControl.getCurrentInfo()
    }

    override fun release() {
        playerControl.release()
    }

    override fun registerCallBack(callBack: CurrentPositionCallBack) {
        playerControl.registerCallBack(callBack)
    }

    override fun removeCallBack(callBack: CurrentPositionCallBack) {
        playerControl.removeCallBack(callBack)
    }

}