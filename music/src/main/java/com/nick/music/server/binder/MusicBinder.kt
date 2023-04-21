package com.nick.music.server.binder

import com.nick.music.entity.MusicVo

interface MusicBinder {
    /**
     * 继续播放
     */
    fun play()

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
    fun next()

    /**
     * 上一首
     */
    fun last()

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
}