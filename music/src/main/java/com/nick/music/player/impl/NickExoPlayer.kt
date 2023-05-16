package com.nick.music.player.impl

import android.content.Context
import android.view.SurfaceHolder
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.nick.base.vo.enum.UrlType
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
                    mDuration = player.contentDuration.toInt()
                    if (mPlayNow){
                        player.play()
                        mPlayStatus = PlayStatus.PLAY
                        mErrorTimes = 0
                    }
                    mPositionCallBackList.forEach { callback->
                        callback.prepareStart(getCurrentInfo())
                        if (mPlayNow){
                            callback.startPlay()
                        }
                    }
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
                play(mCurrentPosition)
            }

        })
        super.init()
    }

    override fun startPlay() {
        player.play()
    }

    override fun playUrl(url: String,urlType: UrlType) {
        if (urlType == UrlType.M3U8){
            val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(url))
            player.setMediaSource(hlsMediaSource)
        }else{
            player.setMediaItem(MediaItem.fromUri(url))
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

    override fun seek(num: Int) {
        player.seekTo(num.toLong())
    }

    override fun replay() {
        player.seekTo(0)
        if (!player.isPlaying){
            player.play()
        }
    }

    override fun attachSurfaceHolder(holder: SurfaceHolder) {
        player.setVideoSurfaceHolder(holder)
    }

    override fun release() {
        super.release()
        player.release()
    }

}