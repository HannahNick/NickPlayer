package com.nick.music.player.impl

import android.view.SurfaceHolder
import com.nick.base.vo.MusicVo
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayerControl
import com.nick.music.server.PlayMode

class NickExoPlayer: PlayerControl {
    override fun play(index: Int) {
        TODO("Not yet implemented")
    }

    override fun playNextRandom() {
        TODO("Not yet implemented")
    }

    override fun playLastRandom() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun seek(num: Int) {
        TODO("Not yet implemented")
    }

    override fun next() {
        TODO("Not yet implemented")
    }

    override fun last() {
        TODO("Not yet implemented")
    }

    override fun playSource(musicVo: MusicVo) {
        TODO("Not yet implemented")
    }

    override fun replay() {
        TODO("Not yet implemented")
    }

    override fun setPlayList(data: List<MusicVo>) {
        TODO("Not yet implemented")
    }

    override fun getCurrentInfo(): PlayInfo {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override fun registerCallBack(callBack: PlayInfoCallBack) {
        TODO("Not yet implemented")
    }

    override fun removeCallBack(callBack: PlayInfoCallBack) {
        TODO("Not yet implemented")
    }

    override fun setPlayMode(playMode: PlayMode) {
        TODO("Not yet implemented")
    }

    override fun getRandomMusicList(): List<MusicVo> {
        TODO("Not yet implemented")
    }

    override fun attachSurfaceHolder(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }
}