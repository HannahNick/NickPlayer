package com.nick.music.player

import com.nick.base.vo.MusicVo
import com.nick.music.entity.PlayInfo


interface PlayInfoCallBack {
    /**
     * 当前播放位置
     */
    fun playPosition(position: Long){}

    /**
     * 播放器将要播放
     */
    fun willStart(playInfo: PlayInfo) {}

    /**
     * 播放器准备完毕
     */
    fun prepareStart(playInfo: PlayInfo){}

    /**
     * 开始播放
     */
    fun startPlay(position: Long){}

    /**
     * 异常播放url
     */
    fun errorUrl(playInfo: PlayInfo) {}

    /**
     * 下一首歌信息
     */
    fun nextInfo(playInfo: PlayInfo) {}

    /**
     * 播放结束
     */
    fun playEnd(playIndex: Int){}

    /**
     * 刷新或加载数据回调
     */
    fun updatePlayList(playList: List<MusicVo>, refresh: Boolean){}

    /**
     * 缓冲
     */
    fun loading(show: Boolean){}

    /**
     * 播放状态改变
     */
    fun playingStatusChanged(isPlaying: Boolean) {}
}