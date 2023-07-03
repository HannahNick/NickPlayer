package com.nick.music.server.binder

import android.view.SurfaceHolder
import com.nick.base.vo.MusicVo
import com.nick.music.server.PlayMode

interface TwoPlayerBinder {
    /**
     * 继续播放
     */
    fun play(index:Int = 0)

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * 设置播放模式
     */
    fun setPlayMode(playMode: PlayMode)

    /**
     * 播放器和surfaceHolder绑定
     */
    fun attachSurfaceHolder(holder: SurfaceHolder)

    /**
     * 设置音乐播放列表
     */
    fun setMusicPlayList(data: List<MusicVo>)

    /**
     * 设置视频播放列表
     */
    fun setVodPlayerList(data: List<MusicVo>)
}