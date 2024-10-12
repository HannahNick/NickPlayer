package com.nick.music.player

/**
 * 播放位置回调
 */
interface PlayPositionCallBack {
    /**
     * 当前播放位置,刷新速度高
     */
    fun playPosition(position: Long) {}

    /**
     * 当前播放位置,刷新速度低
     */
    fun playPositionSlow(position: Long){}
}