package com.nick.music.server.binder

import android.view.SurfaceHolder
import com.nick.base.vo.MusicVo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.server.PlayMode

interface TwoPlayerBinder {
    /**
     * 继续播放
     */
    fun play(index:Int = 0)


    fun setPlayWhenReady(ready:Boolean)

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
     * 清除surfaceHolder绑定
     */
    fun clearSurfaceHolder(holder: SurfaceHolder)

    /**
     * 设置音乐播放列表
     */
    fun setMusicPlayList(data: List<MusicVo>)

    /**
     * 设置视频播放列表
     */
    fun setVodPlayerList(data: List<MusicVo>)

    /**
     * 视频静音
     */
    fun muteVod()

    /**
     * 注册回调监听
     */
    fun registerCallBack(callBack: PlayInfoCallBack)

    /**
     * 移除监听
     */
    fun removeCallBack(callBack: PlayInfoCallBack)

    /**
     * 释放资源
     */
    fun release()
}