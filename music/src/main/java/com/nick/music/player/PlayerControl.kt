package com.nick.music.player

import android.view.SurfaceHolder
import com.nick.base.vo.MusicVo
import com.nick.music.entity.AudioTrackType
import com.nick.music.entity.PlayInfo
import com.nick.music.server.PlayMode
import com.nick.music.server.TrackType

interface PlayerControl {
    /**
     * 继续播放
     */
    fun play(index: Int = 0){}

    /**
     * 强化播放
     */
    fun forcePlay(index: Int){}

    /**
     * 当播放器准备好后播放
     */
    fun setPlayWhenReady(ready: Boolean){}

    /**
     * 随机播放下一首
     */
    fun playNextRandom(){}

    /**
     * 随机播放上一首
     */
    fun playLastRandom(){}

    /**
     * 暂停播放
     */
    fun pause(){}

    fun stop()

    /**
     * seek指定位置
     */
    fun seek(position: Long){}

    /**
     * 设置升降key,只能从0到24之间变换
     */
    fun setKey(key: Int){}

    /**
     * 下一首
     */
    fun next(){}

    /**
     * 上一首
     */
    fun last(){}

    /**
     * 播放指定音乐
     */
    fun playSource(musicVo: MusicVo){}

    /**
     * 重唱
     */
    fun replay(){}

    /**
     * 设置播放列表
     */
    fun setPlayList(data: List<MusicVo>,playIndex: Int = 0){}

    /**
     * 添加数据
     */
    fun appendPlayList(data: List<MusicVo>){}

    /**
     * 获取播放列表
     */
    fun getPlayerList(): List<MusicVo>

    /**
     * 获取当前播放信息和状态
     */
    fun getCurrentInfo(): PlayInfo

    /**
     * 下一首歌信息
     */
    fun getNextCurrentInfo(): PlayInfo

    /**
     * 释放资源
     */
    fun release(){}

    /**
     * 注册播放位置回调
     */
    fun registerCallBack(callBack: PlayInfoCallBack){}

    /**
     * 移除播放位置回调
     */
    fun removeCallBack(callBack: PlayInfoCallBack){}

    /**
     * 设置播放模式 null下一个模式
     */
    fun setPlayMode(playMode: PlayMode? = null){}

    /**
     * 获取随机播放列表数据
     */
    fun getRandomMusicList(): List<MusicVo>

    /**
     * 绑定SurfaceHolder
     */
    fun attachSurfaceHolder(holder: SurfaceHolder){}

    /**
     * 清除SurfaceHolder
     */
    fun clearSurfaceHolder(holder: SurfaceHolder){}

    /**
     * 静音
     */
    fun mute(){}

    /**
     * 取消定时回调
     */
    fun cancelCallBack(){}

    /**
     * MP3播放器是否播放
     */
    fun isPlay(): Boolean

    /**
     * 循环视频和MP3播放器一起播放
     */
    fun playAll(){}

    /**
     * 是否绑定了SurfaceHolder
     */
    fun hasAttachSurfaceHolder(): Boolean

    /**
     * 切换音轨
     */
    fun changeTrack(audioTrackType: AudioTrackType)

    fun getCurrentAudioTrack(): AudioTrackType

    fun toggle()
}