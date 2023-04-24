package com.nick.music.player.impl

import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.LogUtils
import com.nick.music.entity.MusicVo
import com.nick.music.entity.PlayInfo
import com.nick.music.kt.play
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayerControl
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus
import com.nick.music.util.MusicPlayNode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@RequiresApi(Build.VERSION_CODES.Q)
class NickPlayer: PlayerControl{
    private val mMediaPlayer = MediaPlayer()
    private val mMusicData = ArrayList<MusicVo>()
    private val mTimer = Timer()
    private var mIndex: Int = 0
    private var mDuration: Int = -1
    private var mCurrentPosition: Int = -1
    private var mPlayMode = PlayMode.CYCLE
    private var mPlayStatus = PlayStatus.PAUSE
    private var mMediaPlayerHasPrepare = false
    private var mPositionCallBackList = HashSet<PlayInfoCallBack>()
    private var mPlayNow = false
    private val mTask = object : TimerTask(){
        override fun run() {
            mCurrentPosition = mMediaPlayer.currentPosition
            mPositionCallBackList.forEach {
                it.playPosition(mCurrentPosition)
            }
        }
    }
    private lateinit var mHasRandomPlayData: MusicPlayNode<MusicVo>

    init {
        mMediaPlayer.apply {
            setOnCompletionListener {
                when(mPlayMode){
                    PlayMode.SINGLE-> replay()
                    PlayMode.CYCLE-> next()
                    else-> playRandom()
                }
            }
            setOnErrorListener { mp, what, extra ->
                LogUtils.e("播放异常error, what:$what,extra:$extra")
                return@setOnErrorListener true
            }
            setOnSeekCompleteListener {
                LogUtils.i("seek完成")
            }
            setOnTimedMetaDataAvailableListener { mp, data ->
                LogUtils.i("TimedMetaDataAvailableListener:${data.timestamp}")
            }
            setOnPreparedListener {
                mMediaPlayerHasPrepare = true
                mDuration = it.duration
                if (mPlayNow){
                    mPlayStatus = PlayStatus.PLAY
                    it.start()
                }

                val playInfo = getCurrentInfo()
                mPositionCallBackList.forEach { callback->
                    callback.prepareStart(playInfo)
                }
            }
        }
        mTimer.schedule(mTask,0,1000)
        LogUtils.i("初始化完成")
    }

    /**
     * 获取随机播放index
     */
    private fun addHasPlayRandomMusicAndGetPlayNextIndex(): Int{
        var randomIndex: Int
        if (mHasRandomPlayData.size == mMusicData.size){
            return mMusicData.indexOf(mHasRandomPlayData.nextData())
        }
        val random = Random()
        var musicVo: MusicVo
        do {
            randomIndex = random.nextInt(mMusicData.size)
            musicVo = mMusicData[randomIndex]
        }while (mHasRandomPlayData.contains(musicVo))
        mHasRandomPlayData.addAndSkipLast(musicVo)
        return randomIndex
    }

    override fun play(index: Int) {
        if (mMusicData.isEmpty()){
            return
        }
        if (mIndex == index && mMediaPlayerHasPrepare){
            if (mPlayStatus==PlayStatus.PAUSE){
                mMediaPlayer.start()
                mPlayStatus = PlayStatus.PLAY
            }
            return
        }
        mIndex = index
        mPlayNow = true
        mMediaPlayerHasPrepare = false
        mMediaPlayer.play(mMusicData[index].url)
    }

    override fun playRandom() {
        val randomIndex = addHasPlayRandomMusicAndGetPlayNextIndex()
        play(randomIndex)
    }

    override fun pause() {
        mMediaPlayer.pause()
        mPlayStatus = PlayStatus.PAUSE
    }

    override fun seek(num: Int) {
        mMediaPlayer.seekTo(num)
    }

    override fun next() {
        if (mPlayMode == PlayMode.RANDOM){
            playRandom()
            return
        }
        if (mIndex+1>=mMusicData.size){
            mIndex = -1
        }
        mIndex++
        val musicVo = mMusicData[mIndex]
        mMediaPlayer.play(musicVo.url)
    }

    override fun last() {
        if (mPlayMode == PlayMode.RANDOM){

        }

        if (mIndex-1<0){
            mIndex = mMusicData.size
        }
        mIndex--
        val musicVo = mMusicData[mIndex]
        mMediaPlayer.play(musicVo.url)
    }

    override fun playSource(musicVo: MusicVo) {
        mMusicData.add(mIndex,musicVo)
        mMediaPlayer.play(musicVo.url)
    }

    override fun replay() {
        mMediaPlayer.seekTo(0)
        if (!mMediaPlayer.isPlaying){
            mMediaPlayer.start()
        }
    }

    override fun setPlayList(data: List<MusicVo>) {
        if (data.isEmpty()){
            LogUtils.w("setPlayList data is empty")
            return
        }
        mMusicData.clear()
        mMusicData.addAll(data)
        mIndex = 0
        // TODO: 设置随机播放的数据列表
        mHasRandomPlayData.reset(mMusicData[mIndex])
    }

    override fun getCurrentInfo():PlayInfo {
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

    override fun release() {
        mTimer.cancel()
        mMediaPlayer.release()
        mMediaPlayer.clearOnMediaTimeDiscontinuityListener()
        mMediaPlayer.clearOnSubtitleDataListener()
        mPositionCallBackList.clear()
    }

    override fun registerCallBack(callBack: PlayInfoCallBack) {
        mPositionCallBackList.add(callBack)
    }

    override fun removeCallBack(callBack: PlayInfoCallBack) {
        if (mPositionCallBackList.contains(callBack)){
            mPositionCallBackList.remove(callBack)
        }
    }

    override fun setPlayMode(playMode: PlayMode) {
        if (playMode==PlayMode.RANDOM){
            mHasRandomPlayData.reset(mMusicData[mIndex])
        }
        mPlayMode = playMode
    }

}