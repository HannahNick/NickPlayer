package com.nick.music.player

import android.view.SurfaceHolder
import com.nick.base.vo.MusicVo
import com.nick.music.entity.PlayInfo
import com.nick.music.server.PlayMode

interface PlayerControl {
    /**
     * 继续播放
     */
    fun play(index: Int = 0)

    /**
     * 随机播放下一首
     */
    fun playNextRandom()

    /**
     * 随机播放上一首
     */
    fun playLastRandom()

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

    /**
     * 获取当前播放信息和状态
     */
    fun getCurrentInfo(): PlayInfo

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
     * 绑定SurfaceHolder
     */
    fun attachSurfaceHolder(holder: SurfaceHolder)
}