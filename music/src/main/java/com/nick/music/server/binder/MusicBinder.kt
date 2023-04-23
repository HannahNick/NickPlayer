package com.nick.music.server.binder

import com.nick.music.entity.MusicVo
import com.nick.music.entity.PlayInfo

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
}