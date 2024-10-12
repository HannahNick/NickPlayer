package com.nick.music.player

import android.view.SurfaceHolder
import com.nick.base.vo.MusicVo
import com.nick.music.entity.AudioTrackType
import com.nick.music.entity.PlayInfo
import com.nick.music.server.PlayMode
import com.nick.music.server.TrackType
import com.nick.music.util.MusicPlayNode

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
     * 准备
     */
    fun prepare(index: Int = 0){}

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
     * 设置升降key,只能从0到24之间变换,用于seekBar的使用，12是中间值
     */
    fun setKey(key: Int){}

    /**
     * 设置升降key,exoPlayer可接受的类型,1是不变调值
     */
    fun setKey(key: Float = 1f){}

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

    fun removeData(index: Int): MusicVo?{return null}

    /**
     * 获取播放列表
     */
    fun getPlayerList(): List<MusicVo>

    /**
     * 获取当前播放信息和状态
     */
    fun getCurrentPlayInfo(): PlayInfo

    /**
     * 下一首歌信息
     */
    fun getNextPlayInfo(): PlayInfo

    /**
     * 释放资源
     */
    fun release(){}

    /**
     * 注册播放位置回调
     */
    fun registerPlayInfoCallBack(callBack: PlayInfoCallBack){}

    /**
     * 移除播放位置回调
     */
    fun removePlayInfoCallCallBack(callBack: PlayInfoCallBack){}

    fun registerPositionCallBack(callBack: PlayPositionCallBack){}

    fun removePositionCallBack(callBack: PlayPositionCallBack){}

    /**
     * 设置播放模式 null下一个模式
     */
    fun setPlayMode(playMode: PlayMode? = null){}

    fun setPlayModeList(playNodeList: MusicPlayNode<PlayMode>){}

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

    fun changeTrack(trackIndex: Int){}

    fun getCurrentAudioTrack(): AudioTrackType

    fun toggle()

    fun resumePlay(){}

    fun getSessionId():Int

    fun dispatchSongIsRemove(needCallUI: Boolean){}
}