package com.nick.music.server.binder.impl

import android.os.Binder
import android.view.SurfaceHolder
import com.nick.base.vo.MusicVo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayerControl
import com.nick.music.server.PlayMode
import com.nick.music.server.binder.TwoPlayerBinder

class TwoPlayerServerBinder(private val vodControl: PlayerControl, private val musicControl: PlayerControl) : Binder(), TwoPlayerBinder {
    override fun play(index: Int) {
        vodControl.play(index)
        musicControl.play(index)
    }

    override fun setPlayWhenReady(ready: Boolean) {
        vodControl.setPlayWhenReady(ready)
        musicControl.setPlayWhenReady(ready)
    }

    override fun loopHasAttach(): Boolean {
        return vodControl.hasAttachSurfaceHolder()
    }

    override fun pause() {
        vodControl.pause()
        musicControl.pause()
    }

    override fun setPlayMode(playMode: PlayMode) {
        vodControl.setPlayMode(playMode)
    }

    override fun attachLoopVideoHolder(holder: SurfaceHolder) {
        vodControl.attachSurfaceHolder(holder)
    }

    override fun attachMusicHolder(holder: SurfaceHolder) {
        musicControl.attachSurfaceHolder(holder)
    }

    override fun clearSurfaceHolder(holder: SurfaceHolder) {
        vodControl.clearSurfaceHolder(holder)
        musicControl.clearSurfaceHolder(holder)
    }

    override fun clearLoopHolder(holder: SurfaceHolder) {
        vodControl.clearSurfaceHolder(holder)
    }

    override fun clearMusicHolder(holder: SurfaceHolder) {
        musicControl.clearSurfaceHolder(holder)
    }

    override fun setMusicPlayList(data: List<MusicVo>) {
        musicControl.setPlayList(data)
    }

    override fun setVodPlayerList(data: List<MusicVo>) {
        vodControl.setPlayList(data)
    }

    override fun muteVod(){
        vodControl.mute()
    }

    override fun registerCallBack(callBack: PlayInfoCallBack) {
        musicControl.registerCallBack(callBack)
    }

    override fun removeCallBack(callBack: PlayInfoCallBack) {
        musicControl.removeCallBack(callBack)
    }

    override fun release() {
        vodControl.release()
    }

}