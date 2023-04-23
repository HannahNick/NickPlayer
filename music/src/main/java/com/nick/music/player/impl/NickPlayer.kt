package com.nick.music.player.impl

import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.LogUtils
import com.nick.music.entity.MusicVo
import com.nick.music.entity.PlayInfo
import com.nick.music.kt.play
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

    init {
        mMediaPlayer.apply {
            setOnCompletionListener {
                LogUtils.i("播放完毕")
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
                val timestamp = it.timestamp
                LogUtils.i("准备播放回调 anchorMediaTime:${timestamp?.anchorMediaTimeUs},nanoTime:${timestamp?.anchorSystemNanoTime},mediaClockRate:${timestamp?.mediaClockRate}")
                it.start()
                mDuration = it.duration
                mPlayStatus = PlayStatus.PLAY
            }
        }
        val task = object : TimerTask(){
            override fun run() {
                mCurrentPosition = mMediaPlayer.currentPosition
                LogUtils.i("position:$mCurrentPosition")
            }
        }
        mTimer.schedule(task,500)
    }


    override fun play(index: Int) {
        if (mMusicData.isEmpty()|| mMediaPlayer.isPlaying){
            return
        }
        mIndex = index
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
        mPlayStatus = PlayStatus.PAUSE
        mMediaPlayer.pause()
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
        mMediaPlayer.prepare()
        mMediaPlayer.start()
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
            albumName = musicVo.albumName
            mainActor = musicVo.mainActors
        }
    }
}