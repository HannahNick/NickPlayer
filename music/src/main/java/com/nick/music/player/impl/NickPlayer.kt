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
import java.util.Timer
import java.util.TimerTask

@RequiresApi(Build.VERSION_CODES.Q)
class NickPlayer: PlayerControl{
    private val mMediaPlayer = MediaPlayer()
    private val mMusicData = ArrayList<MusicVo>()
    private val mTimer = Timer()
    private var mIndex: Int = -1
    private var mDuration: Int = -1
    private var mCurrentPosition: Int = -1
    private var mPlayMode = PlayMode.PLAY_CYCLE
    private var mPlayStatus = PlayStatus.PAUSE
    private var mInitSourceFlag = false
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

    init {
        mMediaPlayer.apply {
            setOnCompletionListener {
                LogUtils.i("播放完毕")
                next()
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
    }


    override fun play(index: Int) {
        if (mMusicData.isEmpty()|| mMediaPlayer.isPlaying){
            return
        }
        mIndex = index
        mPlayNow = true
        if (mMediaPlayerHasPrepare){
            mMediaPlayer.start()
        }else{
            mMediaPlayer.setDataSource(mMusicData[index].url)
            mMediaPlayerHasPrepare = false
            mMediaPlayer.prepareAsync()
        }
        mInitSourceFlag = true
    }

    override fun pause() {
        mMediaPlayer.pause()
        mPlayStatus = PlayStatus.PAUSE
    }

    override fun seek(num: Int) {
        mMediaPlayer.seekTo(num)
    }

    override fun next() {
        if (mIndex+1>=mMusicData.size){
            LogUtils.i("已经最后一首了")
            return
        }
        mIndex++
        val musicVo = mMusicData[mIndex]
        mMediaPlayer.play(musicVo.url)
    }

    override fun last() {
        if (mIndex-1<0){
            LogUtils.i("已经是第一首了")
            return
        }
        mIndex--
        val musicVo = mMusicData[mIndex]
        mMediaPlayer.play(musicVo.url)
    }

    override fun playSource(musicVo: MusicVo) {
        mMusicData.add(mIndex,musicVo)
        mMediaPlayer.play(musicVo.url)
        mInitSourceFlag = true
    }

    override fun replay() {
        mMediaPlayer.stop()
        mMediaPlayer.prepareAsync()
    }

    override fun setPlayList(data: List<MusicVo>) {
        mMusicData.clear()
        mMusicData.addAll(data)
        mIndex = 0
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


}