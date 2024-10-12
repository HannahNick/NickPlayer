package com.nick.music.player.impl

import android.media.MediaPlayer
import android.os.Build
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.LogUtils
import com.nick.base.vo.enum.UrlType
import com.nick.music.entity.AudioTrackType
import com.nick.music.kt.play
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus
import com.nick.music.server.TrackType

@RequiresApi(Build.VERSION_CODES.Q)
class NickPlayer(val tag: String): AbsPlayer(){
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
                mPlayerHasPrepare = true
                mDuration = it.duration.toLong()
                if (mPlayNow){
                    it.start()
                    mPlayStatus = PlayStatus.PLAY
                }

                mPlayInfoCallBackList.dispatch {
                    prepareStart(getCurrentPlayInfo())
                }
            }
            setOnBufferingUpdateListener { mp,precent ->
                LogUtils.i("已缓存:${precent}%")
            }
        }
        super.init(tag)
        LogUtils.i("初始化完成")
    }

    override fun seek(num: Long) {
        mMediaPlayer.seekTo(num.toInt())
    }

    override fun replay() {
        mMediaPlayer.seekTo(0)
        if (!mMediaPlayer.isPlaying){
            mMediaPlayer.start()
        }
    }


    override fun release() {
        super.release()
        mMediaPlayer.release()
        mMediaPlayer.clearOnMediaTimeDiscontinuityListener()
        mMediaPlayer.clearOnSubtitleDataListener()
        mPlayInfoCallBackList.clear()
        mPlayPositionCallBackList.clear()
    }

    override fun startPlay() {
        mMediaPlayer.start()
    }

    override fun playUrl(url: String,urlType: UrlType) {
        mMediaPlayer.play(url)
    }

    override fun prepareUrl(url: String,urlType: UrlType) {
        mMediaPlayer.reset()
        mMediaPlayer.setDataSource(url)
        mMediaPlayer.prepareAsync()
    }

    override fun playerPause() {
        mMediaPlayer.pause()
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun getPlayPosition(): Long {
        return mMediaPlayer.currentPosition.toLong()
    }

    override fun setPlayWhenReady(ready: Boolean) {
    }

    override fun attachSurfaceHolder(holder: SurfaceHolder) {
        mMediaPlayer.setDisplay(holder)
        mHasAttachSurfaceHolder = true
    }

    override fun clearSurfaceHolder(holder: SurfaceHolder) {
        mMediaPlayer.setDisplay(null)
        mHasAttachSurfaceHolder = false
    }

    override fun mute() {
    }

    override fun isPlay(): Boolean {
        return mMediaPlayer.isPlaying
    }

    override fun changeTrack(audioTrackType: AudioTrackType) {
//        mMediaPlayer.selectTrack()
    }

    override fun getCurrentAudioTrack(): AudioTrackType {
        return AudioTrackType.ACC
    }

    override fun toggle() {
        if (mMediaPlayer.isPlaying){
            pause()
        }else{
            play(mIndex)
        }
    }

    override fun getSessionId(): Int {
        return mMediaPlayer.audioSessionId
    }

}