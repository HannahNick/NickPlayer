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
}