package com.nick.music.player.impl

import com.nick.base.vo.MusicVo
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayerControl
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus
import com.nick.music.util.MusicPlayNode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * 播放器抽象父类，用于保存一些音乐数据和状态数据
 */
abstract class AbsPlayer: PlayerControl {
    //首次初始化数据,播放数据
    protected val mMusicData = ArrayList<MusicVo>()
    //随机播放列表数据
    protected lateinit var mHasRandomPlayData: MusicPlayNode<MusicVo>
    //每秒回调数据的计时器
    protected val mTimer = Timer()
    //当前播放列表下标
    protected var mIndex: Int = 0
    //当前播放歌曲的总时长
    protected var mDuration: Int = -1
    //当前播放时间位置
    protected var mCurrentPosition: Int = -1
    //播放模式
    protected var mPlayMode = PlayMode.CYCLE
    //播放状态
    protected var mPlayStatus = PlayStatus.PAUSE
    //播放器是否准备完成(加载好资源)
    protected var mMediaPlayerHasPrepare = false
    //播放位置和信息的回调
    protected var mPositionCallBackList = HashSet<PlayInfoCallBack>()
    //是否立即播放
    protected var mPlayNow = false

    private val mTask = object : TimerTask(){
        override fun run() {
            mCurrentPosition = getPlayPosition()
            mPositionCallBackList.forEach {
                it.playPosition(mCurrentPosition)
            }
        }
    }


    protected fun init(){
        mTimer.schedule(mTask,0,1000)
    }


    override fun release() {
        mTimer.cancel()
    }

    abstract fun getPlayPosition(): Int

    protected fun setDataSource(data: List<MusicVo>){
        mMusicData.clear()
        mMusicData.addAll(data)
        mIndex = 0
        //设置
        val randomList = ArrayList(mMusicData).shuffled()
        if (::mHasRandomPlayData.isInitialized){
            mHasRandomPlayData.reset()
        }else{
            mHasRandomPlayData = MusicPlayNode()
        }
        randomList.forEach {
            mHasRandomPlayData.add(it)
        }
        mPlayNow = false
    }

    override fun registerCallBack(callBack: PlayInfoCallBack) {
        mPositionCallBackList.add(callBack)
    }

    override fun removeCallBack(callBack: PlayInfoCallBack) {
        if (mPositionCallBackList.contains(callBack)){
            mPositionCallBackList.remove(callBack)
        }
    }

    override fun getCurrentInfo(): PlayInfo {
        val musicVo = mMusicData[mIndex]
        return PlayInfo().apply {
            dataIndex = mIndex
            playStatus = mPlayStatus
            playMode = mPlayMode
            currentPosition = mCurrentPosition
            duration = mDuration
            albumName = musicVo.albumName
            mainActor = musicVo.mainActors
        }
    }

    override fun setPlayMode(playMode: PlayMode) {
        mPlayMode = playMode
    }

    override fun getRandomMusicList(): List<MusicVo> {
        return mHasRandomPlayData.convertList()
    }
}