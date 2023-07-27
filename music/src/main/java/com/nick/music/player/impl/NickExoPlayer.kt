package com.nick.music.player.impl

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.media3.common.*
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.nick.base.vo.enum.UrlType
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus

class NickExoPlayer(context: Context): AbsPlayer() {
    private val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
    private val player = ExoPlayer.Builder(context).build()
    init {
        player.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_READY){
                    LogUtils.i("duration: ${player.duration}")
                    mDuration = player.duration.toInt()

                    mPositionCallBackList.forEach { callback->
                        LogUtils.i("回调准备开始")
                        callback.prepareStart(getCurrentInfo())
                    }
                    if (mPlayNow){
                        player.play()
                        LogUtils.i("播放器已播放")
                        mPositionCallBackList.forEach { callback->
                            callback.startPlay(player.currentPosition)
                        }
                        LogUtils.i("已回调开始播放")
                        mPlayStatus = PlayStatus.PLAY
                        mErrorTimes = 0
                    }

                }
                if (playbackState == Player.STATE_ENDED){
                    next()
                }

            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                LogUtils.e("onPlayerError: $error")
                if (mErrorTimes==3){
                    LogUtils.e("播放重试失败")
                    ToastUtils.showLong("播放重试失败")
                    return
                }
                mErrorTimes++
                mPlayerHasPrepare = false
                player.stop()
                play(mIndex)
            }

            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                if(reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION){//当设置单曲循环的时候，会走这个回调函数而不会走 上面的 playbackState == Player.STATE_ENDED
                    LogUtils.i("onPositionDiscontinuity")
                }

            }
        })
        super.init()
    }

    override fun setPlayMode(playMode: PlayMode) {
        super.setPlayMode(playMode)
        when (playMode) {
            PlayMode.SINGLE -> {
                player.repeatMode = Player.REPEAT_MODE_ONE
            }
            PlayMode.CYCLE -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
            }
            else -> {
                player.repeatMode = Player.REPEAT_MODE_OFF
            }
        }
    }

    override fun startPlay() {
        player.play()
    }

    override fun playUrl(url: String,urlType: UrlType) {
        if (urlType == UrlType.M3U8){
            val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(url))
            player.setMediaSource(hlsMediaSource)
        }else{
            LogUtils.i("playUrl: $url")
            player.setMediaItem(MediaItem.fromUri(url))
        }
        if (url.endsWith(".mp3")){

        }
        player.prepare()
        mPlayerHasPrepare = true

    }

    override fun prepareUrl(url: String,urlType: UrlType) {
        if (urlType == UrlType.M3U8){
            val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(url))
            player.setMediaSource(hlsMediaSource)
        }else{
            player.setMediaItem(MediaItem.fromUri(url))
        }
        player.prepare()
    }

    override fun playerPause() {
        player.pause()
    }

    override fun getPlayPosition(): Int {
        return player.currentPosition.toInt()
    }

    override fun setPlayWhenReady(ready: Boolean) {
        player.playWhenReady = ready
    }

    override fun seek(num: Int) {
        LogUtils.i("seek: ${num.toLong()}")
        player.seekTo(num.toLong())
    }

    override fun setKey(key: Float) {
        player.playbackParameters = PlaybackParameters(1f,key)
    }

    override fun replay() {
        player.seekTo(0)
        if (!player.isPlaying){
            player.play()
        }
    }

    override fun attachSurfaceHolder(holder: SurfaceHolder) {
        player.setVideoSurfaceHolder(holder)
        mHasAttachSurfaceHolder = true
    }

    override fun clearSurfaceHolder(holder: SurfaceHolder) {
        player.clearVideoSurfaceHolder(holder)
        mHasAttachSurfaceHolder = false
    }

    override fun mute() {
        player.volume = 0f
    }

    override fun release() {
        super.release()
        player.release()
    }

}