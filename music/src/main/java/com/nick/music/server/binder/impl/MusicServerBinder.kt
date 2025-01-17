package com.nick.music.server.binder.impl

import android.os.Binder
import android.view.SurfaceHolder
import com.nick.base.vo.MusicVo
import com.nick.music.entity.AudioTrackType
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayerControl
import com.nick.music.server.PlayMode
import com.nick.music.server.TrackType
import com.nick.music.server.binder.MusicBinder

class MusicServerBinder(private val playerControl: PlayerControl) :Binder(),MusicBinder{

    override fun play(index:Int) {
        playerControl.play(index)
    }

    override fun pause() {
        playerControl.pause()
    }

    override fun seek(num: Int) {
        playerControl.seek(num.toLong())
    }

    override fun setKey(key: Float) {
        playerControl.setKey(key.toInt())
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
        return playerControl.getCurrentPlayInfo()
    }

    override fun release() {
        playerControl.release()
    }

    override fun registerCallBack(callBack: PlayInfoCallBack) {
        playerControl.registerPlayInfoCallBack(callBack)
    }

    override fun removeCallBack(callBack: PlayInfoCallBack) {
        playerControl.removePlayInfoCallCallBack(callBack)
    }

    override fun setPlayMode(playMode: PlayMode) {
        playerControl.setPlayMode(playMode)
    }

    override fun getRandomMusicList(): List<MusicVo> {
        return playerControl.getRandomMusicList()
    }

    override fun attachSurfaceHolder(holder: SurfaceHolder) {
        playerControl.attachSurfaceHolder(holder)
    }

    override fun changeTrack(trackType: AudioTrackType) {
        playerControl.changeTrack(trackType)
    }

}