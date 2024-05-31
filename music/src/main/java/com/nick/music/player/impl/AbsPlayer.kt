package com.nick.music.player.impl

import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.LogUtils
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayerControl
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus
import com.nick.music.util.MusicPlayNode
import com.xyz.base.utils.Dispatcher
import com.xyz.base.utils.L
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
    protected val mHasRandomPlayData: MusicPlayNode<MusicVo> = MusicPlayNode()

    //当前播放列表下标
    protected var mIndex: Int = 0
    //当前播放歌曲的总时长
    protected var mDuration: Long = -1
    //当前播放时间位置
    protected var mCurrentPosition: Long = -1
    //播放模式
    protected var mPlayMode = PlayMode.CYCLE
    //播放状态
    protected var mPlayStatus = PlayStatus.PAUSE
    //播放器是否准备完成(加载好资源)
    protected var mPlayerHasPrepare = false
    //播放位置和信息的回调
    protected var mPositionCallBackList : Dispatcher<PlayInfoCallBack> = Dispatcher.create()
    //是否立即播放
    protected var mPlayNow = false
    //播放错误次数
    protected var mErrorTimes = 0
    //是否绑定了Surface
    protected var mHasAttachSurfaceHolder = false

    private val mHandler = Handler(Looper.getMainLooper())
    //播放器回调播放位置控制flag
    protected var mCancelPlayerPositionCallBackFlag = false

    protected var mClipStartPosition = 0L


    //播放模式列表
    protected val mPlayerModeList = MusicPlayNode<PlayMode>().apply {
        add(PlayMode.CYCLE)
        add(PlayMode.SINGLE)
        add(PlayMode.RANDOM)
        setCurrentNode(PlayMode.CYCLE)
    }
    private lateinit var mTag: String

    private val mCheckPositionRunnable = object : Runnable{
        override fun run() {
            if (mCancelPlayerPositionCallBackFlag){
                return
            }

            if (mPlayStatus == PlayStatus.PAUSE){
                mHandler.postDelayed(this, 1000)
                return
            }
            mCurrentPosition = getPlayPosition()
            mPositionCallBackList.dispatch {
                playPosition(mCurrentPosition)
            }
            mHandler.postDelayed(this,  35  )
        }
    }

    private val mNextInfoRunnable = object : Runnable{
        override fun run() {
            mPositionCallBackList.dispatch {
                nextInfo(getNextCurrentInfo())
            }
        }
    }

    companion object{
        const val NEXTINFO_RUNNABLE_TOKEN = "nextinfoRunnableToken"
    }

    protected fun init(tag: String){
        mTag = tag
        mHandler.post(mCheckPositionRunnable)
    }



    override fun release() {
        mCancelPlayerPositionCallBackFlag = true
        mHandler.removeCallbacksAndMessages(null)
        mMusicData.clear()
        mHasRandomPlayData.reset()
        mPositionCallBackList.clear()
    }

    private fun setDataSource(data: List<MusicVo>,playIndex: Int){
        mMusicData.clear()
        mMusicData.addAll(data)
        mIndex = playIndex
        //设置
        val randomList = ArrayList(mMusicData).shuffled()
        mHasRandomPlayData.reset()
        randomList.forEach {
            mHasRandomPlayData.add(it)
        }
        mHasRandomPlayData.setCurrentNode(data[playIndex])
        mPlayNow = false
    }

    override fun getPlayerList(): List<MusicVo> {
        return mMusicData
    }

    override fun registerCallBack(callBack: PlayInfoCallBack) {
        L.i("registerCallBack")
        mPositionCallBackList.add(callBack)
    }

    override fun removeCallBack(callBack: PlayInfoCallBack) {
        if (mPositionCallBackList.contain(callBack)){
            L.i("removeCallBack")
            mPositionCallBackList.remove(callBack)
        }
    }

    override fun getCurrentInfo(): PlayInfo {
        if (mMusicData.isEmpty()){
            return PlayInfo()
        }

        val musicVo = mMusicData[mIndex]
        return PlayInfo().apply {
            dataIndex = mIndex
            playStatus = mPlayStatus
            playMode = mPlayMode
            currentPosition = getPlayPosition()
            duration = mDuration
            songName = musicVo.songName
            mainActor = musicVo.mainActors
//            liveName = musicVo.liveName
            lyricPath = musicVo.lyricPath
            url = musicVo.path
            imgPath = musicVo.imgPath
        }
    }

    override fun getNextCurrentInfo(): PlayInfo {
        if (mMusicData.isEmpty()){
            return PlayInfo()
        }
        val musicVo = when(mPlayMode){
            PlayMode.RANDOM ->{
                mHasRandomPlayData.nextData(true)
            }
            PlayMode.SINGLE ->{
                mMusicData[mIndex]
            }
            else ->{
                val nextIndex: Int = if (mIndex+1>=mMusicData.size){
                    0
                }else{
                    mIndex+1
                }
                mMusicData[nextIndex]
            }
        }
        return PlayInfo().apply {
            dataIndex = mMusicData.indexOf(musicVo)
            playStatus = mPlayStatus
            playMode = mPlayMode
            currentPosition = 0
            duration = 0
            songName = musicVo.songName
            mainActor = musicVo.mainActors
//            liveName = musicVo.liveName
            lyricPath = musicVo.lyricPath
            url = musicVo.path
            imgPath = musicVo.imgPath
        }
    }

    override fun setPlayMode(playMode: PlayMode?) {
        mPlayMode = if (playMode==null){
            mPlayerModeList.nextData()
        }else{
            mPlayerModeList.setCurrentNode(playMode)
            playMode
        }
        L.i("setPlayMode: ${mPlayMode.name}")
    }

    override fun getRandomMusicList(): List<MusicVo> {
        return mHasRandomPlayData.convertList()
    }

    override fun play(index: Int) {
        if (mMusicData.isEmpty()){
            return
        }
        if (mIndex == index && mPlayerHasPrepare){
            if (mPlayStatus== PlayStatus.PAUSE){
                mPlayNow = true
                startPlay()
            }
            return
        }
        forcePlay(index)
    }

    override fun forcePlay(index: Int) {
        mHandler.removeCallbacksAndMessages(NEXTINFO_RUNNABLE_TOKEN)
        mHandler.postDelayed(mNextInfoRunnable,NEXTINFO_RUNNABLE_TOKEN,30000)
        mPlayNow = true
        mIndex = index
        mPlayerHasPrepare = false
        mCancelPlayerPositionCallBackFlag = false
        val musicVo = mMusicData[index]
        L.i("playData: $musicVo")
        playUrl(musicVo.path,musicVo.pathType)
        mHasRandomPlayData.setCurrentNode(musicVo)
    }

    override fun playNextRandom() {
        val musicVo = mHasRandomPlayData.nextData()
        val index = mMusicData.indexOf(musicVo)
        if (index==mIndex){
            seek(0)
        }else{
            play(index)
        }
    }

    override fun playLastRandom() {
        val musicVo = mHasRandomPlayData.lastData()
        val index = mMusicData.indexOf(musicVo)
        if (index==mIndex){
            seek(0)
        }else{
            play(index)
        }
    }

    override fun pause() {
        mPlayNow = false
        playerPause()
    }

    override fun next() {
        L.i("next playMode: $mPlayMode")
        if (mPlayMode == PlayMode.RANDOM){
            playNextRandom()
            return
        }
        if (mPlayMode == PlayMode.SINGLE){
            if (isPlay()){
                seek(0)
            }
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
        if (mPlayMode == PlayMode.SINGLE){
            if (isPlay()){
                seek(0)
            }
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

    override fun setPlayList(data: List<MusicVo>,playIndex: Int) {
        L.i("$mTag setPlayList: $data")
        if (data.isEmpty()){
            L.w("$mTag setPlayList data is empty")
            return
        }
        setDataSource(data,playIndex)
        val musicVo = mMusicData[mIndex]
        L.i("$mTag musicVo: $musicVo")
        mHandler.removeCallbacksAndMessages(NEXTINFO_RUNNABLE_TOKEN)
        mHandler.postDelayed(mNextInfoRunnable,NEXTINFO_RUNNABLE_TOKEN,30000)
        prepareUrl(musicVo.path,musicVo.pathType)
        mPositionCallBackList.dispatch {
            this.updatePlayList(getPlayerList(),true)
        }
    }

    override fun appendPlayList(data: List<MusicVo>) {
        L.i("$mTag appendPlayList: $data")
        //添加到普通列表
        mMusicData.addAll(data)
        //添加到随机列表
        val tempRandomList = ArrayList(data).shuffled()
        tempRandomList.forEach {
            mHasRandomPlayData.add(it)
        }
        mPositionCallBackList.dispatch {
            this.updatePlayList(data,false)
        }
    }

    override fun hasAttachSurfaceHolder(): Boolean {
        return mHasAttachSurfaceHolder
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

    abstract override fun stop()

    /**
     * 获取当前播放位置
     */
    abstract fun getPlayPosition(): Long

}