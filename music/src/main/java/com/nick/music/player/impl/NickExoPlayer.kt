package com.nick.music.player.impl

import android.content.Context
import android.view.SurfaceHolder
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.blankj.utilcode.util.LogUtils
import com.nick.music.server.PlayStatus

class NickExoPlayer(private val context: Context): AbsPlayer() {

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
                    }
                    mPositionCallBackList.forEach { callback->
                        callback.prepareStart(getCurrentInfo())
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                LogUtils.e("onPlayerError: $error")

            }

        })
        super.init()
    }

    override fun startPlay() {
        player.play()
    }

    override fun playUrl(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        mMediaPlayerHasPrepare = true

    }

    override fun prepareUrl(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
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

    }

    override fun release() {
        super.release()
        player.release()
    }

}