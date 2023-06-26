package com.nick.music.server.binder

import android.view.SurfaceHolder
import com.nick.base.vo.MusicVo
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.server.PlayMode

interface MusicBinder {
    /**
     * 继续播放
     */
    fun play(index:Int = 0)

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * seek指定位置
     */
    fun seek(num: Int)

    /**
     * 设置key
     */
    fun setKey(key: Float)

    /**
     * 下一首
     */
    fun playNext()

    /**
     * 上一首
     */
    fun playLast()

    /**
     * 播放指定音乐
     */
    fun playSource(musicVo: MusicVo)

    /**
     * 重唱
     */
    fun replay()

    /**
     * 设置播放列表
     */
    fun setPlayList(data: List<MusicVo>)

    /**
     * 获取当前播放器信息
     */
    fun getPlayInfo(): PlayInfo

    /**
     * 释放资源
     */
    fun release()

    /**
     * 注册播放位置回调
     */
    fun registerCallBack(callBack: PlayInfoCallBack)

    /**
     * 移除播放位置回调
     */
    fun removeCallBack(callBack: PlayInfoCallBack)

    /**
     * 设置播放模式
     */
    fun setPlayMode(playMode: PlayMode)

    /**
     * 获取随机播放列表数据
     */
    fun getRandomMusicList(): List<MusicVo>

    /**
     * 播放器和surfaceHolder绑定
     */
    fun attachSurfaceHolder(holder: SurfaceHolder)
}