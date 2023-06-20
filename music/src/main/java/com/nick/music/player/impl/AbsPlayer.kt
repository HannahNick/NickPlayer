package com.nick.music.player.impl

import com.blankj.utilcode.util.LogUtils
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayerControl
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus
import com.nick.music.util.MusicPlayNode
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * 播放器抽象父类，用于保存一些音乐、状态数据，还有部分播放的业务逻辑
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
    protected var mPlayerHasPrepare = false
    //播放位置和信息的回调
    protected var mPositionCallBackList = HashSet<PlayInfoCallBack>()
    //是否立即播放
    protected var mPlayNow = false
    //播放错误次数
    protected var mErrorTimes = 0


    private val mTask = object : TimerTask(){
        override fun run() {
            io.reactivex.rxjava3.core.Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _ ->
                    if (mPlayStatus == PlayStatus.PAUSE){
                        return@subscribe
                    }
                    mCurrentPosition = getPlayPosition()
                    mPositionCallBackList.forEach {
                        it.playPosition(mCurrentPosition)
                    }
                }
        }
    }


    protected fun init(){
        mTimer.schedule(mTask,0,1000)
    }


    override fun release() {
        mTimer.cancel()
    }

    private fun setDataSource(data: List<MusicVo>){
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
            currentPosition = getPlayPosition()
            duration = mDuration
            albumName = musicVo.albumName
            mainActor = musicVo.mainActors
            liveName = musicVo.liveName
            lyricPath = musicVo.lyricPath
        }
    }

    override fun setPlayMode(playMode: PlayMode) {
        mPlayMode = playMode
    }

    override fun getRandomMusicList(): List<MusicVo> {
        return mHasRandomPlayData.convertList()
    }

    override fun play(index: Int) {
        if (mMusicData.isEmpty()){
            return
        }
        if (mIndex == index && mPlayerHasPrepare){
            if (mPlayStatus==PlayStatus.PAUSE){
                startPlay()
                mPlayStatus = PlayStatus.PLAY
            }
            return
        }
        mIndex = index
        mPlayNow = true
        mPlayerHasPrepare = false
        val musicVo = mMusicData[index]
        playUrl(musicVo.path,musicVo.pathType)
        mHasRandomPlayData.setCurrentNode(musicVo)
    }

    override fun playNextRandom() {
        val musicVo = mHasRandomPlayData.nextData()
        play(mMusicData.indexOf(musicVo))
    }

    override fun playLastRandom() {
        val musicVo = mHasRandomPlayData.lastData()
        play(mMusicData.indexOf(musicVo))
    }

    override fun pause() {
        playerPause()
        mPlayStatus = PlayStatus.PAUSE
    }

    override fun next() {
        if (mPlayMode == PlayMode.RANDOM){
            playNextRandom()
            return
        }
        if (mIndex+1>=mMusicData.size){
            mIndex = -1
        }
        play(mIndex+1)
    }

    override fun last() {
        if (mPlayMode == PlayMode.RANDOM){
            playLastRandom()
            return
        }

        if (mIndex-1<0){
            mIndex = mMusicData.size
        }
        play(mIndex-1)
    }

    override fun playSource(musicVo: MusicVo) {
        mMusicData.add(mIndex,musicVo)
        play(mIndex)
    }

    override fun setPlayList(data: List<MusicVo>) {
        if (data.isEmpty()){
            LogUtils.w("setPlayList data is empty")
            return
        }
        setDataSource(data)
        val musicVo = mMusicData[mIndex]
        prepareUrl(musicVo.path,musicVo.pathType)

    }

    /**
     * 直接播放无需准备
     */
    abstract fun startPlay()

    /**
     * 播放指定路径，需要准备
     */
    abstract fun playUrl(url: String,urlType: UrlType = UrlType.DEFAULT)

    /**
     * 准备播放路径
     */
    abstract fun prepareUrl(url: String,urlType: UrlType = UrlType.DEFAULT)

    /**
     * 播放暂停
     */
    abstract fun playerPause()

    /**
     * 获取当前播放位置
     */
    abstract fun getPlayPosition(): Int

}