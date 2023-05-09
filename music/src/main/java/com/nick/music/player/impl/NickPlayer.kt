package com.nick.music.player.impl

import android.media.MediaPlayer
import android.os.Build
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.LogUtils
import com.nick.base.BaseUrl
import com.nick.base.vo.MusicVo
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
class NickPlayer: AbsPlayer(){
    private val mMediaPlayer = MediaPlayer()

    init {
        mMediaPlayer.apply {
            setOnCompletionListener {
                when(mPlayMode){
                    PlayMode.SINGLE-> replay()
                    PlayMode.CYCLE-> next()
                    else-> playNextRandom()
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
                    it.start()
                    mPlayStatus = PlayStatus.PLAY
                }

                val playInfo = getCurrentInfo()
                mPositionCallBackList.forEach { callback->
                    callback.prepareStart(playInfo)
                }
            }
            setOnBufferingUpdateListener { mp,precent ->
                LogUtils.i("已缓存:${precent}%")
            }
        }
        init()
        LogUtils.i("初始化完成")
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
        val musicVo = mMusicData[index]
        mMediaPlayer.play("${BaseUrl.url}${musicVo.path}")
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
        mMediaPlayer.pause()
        mPlayStatus = PlayStatus.PAUSE
    }

    override fun seek(num: Int) {
        mMediaPlayer.seekTo(num)
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
        setDataSource(data)
        mMediaPlayer.reset()
        mMediaPlayer.setDataSource("${BaseUrl.url}${mMusicData[mIndex].path}")
        mMediaPlayer.prepareAsync()
    }

    override fun release() {
        mTimer.cancel()
        mMediaPlayer.release()
        mMediaPlayer.clearOnMediaTimeDiscontinuityListener()
        mMediaPlayer.clearOnSubtitleDataListener()
        mPositionCallBackList.clear()
    }

    override fun getPlayPosition(): Int {

        return mMediaPlayer.currentPosition
    }

    override fun attachSurfaceHolder(holder: SurfaceHolder) {
        mMediaPlayer.setDisplay(holder)
    }

}