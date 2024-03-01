package com.nick.music.player.impl

import android.media.MediaPlayer
import android.os.Build
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.LogUtils
import com.nick.base.vo.enum.UrlType
import com.nick.music.kt.play
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus
import com.nick.music.server.TrackType

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
                mPlayerHasPrepare = true
                mDuration = it.duration
                if (mPlayNow){
                    it.start()
                    mPlayStatus = PlayStatus.PLAY
                }

                mPositionCallBackList.forEach { callback->
                    callback.prepareStart(getPlayInfo())
                }
            }
            setOnBufferingUpdateListener { mp,precent ->
                LogUtils.i("已缓存:${precent}%")
            }
        }
        super.init()
        LogUtils.i("初始化完成")
    }

    override fun seek(num: Int) {
        mMediaPlayer.seekTo(num)
    }

    override fun setKey(key: Float) {
        TODO("Not yet implemented")
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
        mPositionCallBackList.clear()
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

    override fun prepareUrlByClipping(url: String, urlType: UrlType, start: Long, end: Long) {

    }

    override fun playerPause() {
        mMediaPlayer.pause()
    }

    override fun getPlayPosition(): Int {
        return mMediaPlayer.currentPosition
    }

    override fun callBackPosition(position: Int) {

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

    override fun changeTrack(trackType: TrackType) {

    }

}