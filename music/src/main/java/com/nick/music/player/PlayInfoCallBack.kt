package com.nick.music.player

import com.nick.music.entity.PlayInfo


interface PlayInfoCallBack {
    /**
     * 当前播放位置
     */
    fun playPosition(position: Int)

    /**
     * 播放器准备完毕
     */
    fun prepareStart(playInfo: PlayInfo)

    /**
     * 开始播放
     */
    fun startPlay(position: Long)

    /**
     * 播放结束
     */
    fun playEnd(playIndex: Int){}

    /**
     * 缓冲
     */
    fun loading(show: Boolean){}
}