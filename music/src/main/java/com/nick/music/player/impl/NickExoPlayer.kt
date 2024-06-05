package com.nick.music.player.impl

import android.content.Context
import android.text.TextUtils
import android.view.SurfaceHolder
import androidx.media3.common.*
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.util.EventLogger
import com.google.common.collect.ImmutableList
import com.nick.base.vo.*
import com.nick.base.vo.enum.UrlType
import com.nick.music.entity.AudioTrackType
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus
import com.xyz.base.utils.L


class NickExoPlayer(context: Context, val tag: String): AbsPlayer() {
    private val mDataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
    private val mPlayer = ExoPlayer.Builder(context).build()
    private var mDefaultTrack = AudioTrackType.ACC
    private val MID_KEY = 12
    private val MAX_ERROR_TIMES = 10

    init {
        mPlayer.addAnalyticsListener(EventLogger(tag))
        mPlayer.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                L.i("$tag onPlaybackStateChanged: $playbackState")
                when (playbackState) {
                    Player.STATE_READY -> {
                        L.i("$tag duration: ${mPlayer.duration}")
                        mDuration = mPlayer.duration
                        changeTrack(mDefaultTrack)
                        mPositionCallBackList.dispatch {
                            L.i("$tag 回调准备开始")
                            prepareStart(getCurrentInfo())
                        }

                        if (mPlayNow) {
                            mPlayer.play()
                            L.i("$tag 播放器已播放")
                            mPositionCallBackList.dispatch {
                                startPlay(mPlayer.currentPosition)
                            }

                            L.i("$tag 已回调开始播放")
                            mErrorTimes = 0
                        }
                    }

                    Player.STATE_ENDED -> {
                        L.i("$tag 播放结束")
                        mPositionCallBackList.dispatch {
                            playEnd(mIndex)
                        }
                        when(mPlayMode){
                            PlayMode.RANDOM ->{
                                playNextRandom()
                            }
                            PlayMode.SINGLE ->{
                                seek(mClipStartPosition)
                            }
                            PlayMode.CYCLE ->{
                                next()
                            }
                            else->{
                                if (mIndex == mMusicData.size-1){
                                    mPlayNow = false
                                    seek(mClipStartPosition)
                                    pause()
                                }else{
                                    next()
                                }
                            }
                        }

                    }

                    Player.STATE_BUFFERING -> {
                        L.i("$tag Player.STATE_BUFFERING")
                    }

                    Player.STATE_IDLE -> {
                        L.i("$tag mPlayNow: $mPlayNow")
                        if (mPlayNow) {
                            mPlayer.prepare()
                        }
                    }
                }

            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                L.e("$tag onPlayerError: $error")
                error.printStackTrace()
                if (mErrorTimes == MAX_ERROR_TIMES) {
                    L.e(" $tag 播放重试失败")
                    return
                }
                mErrorTimes++
                mPlayerHasPrepare = false
                mPlayer.stop()
                play(mIndex)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {//当设置单曲循环的时候，会走这个回调函数而不会走 上面的 playbackState == Player.STATE_ENDED
                    L.i("$tag onPositionDiscontinuity")
                }

            }

            override fun onTracksChanged(tracks: Tracks) {
                super.onTracksChanged(tracks)
                //当音轨切换成功后，更新UI
                trackLog(tracks.groups)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                L.i("onIsPlayingChanged: ${getCurrentInfo()}")
                mPlayStatus = if (isPlaying) PlayStatus.PLAY else PlayStatus.PAUSE
                mPositionCallBackList.dispatch {
                    playingStatusChanged(isPlaying)
                }
            }
        })
        super.init(tag)
    }

    private fun trackLog(groups: ImmutableList<Tracks.Group>) {
        for (trackGroup in groups) {
            // Group level information.
            val trackType = trackGroup.type
            val trackInGroupIsSelected = trackGroup.isSelected
            val trackInGroupIsSupported = trackGroup.isSupported
            for (i in 0 until trackGroup.length) {
                // Individual track information.
                val isSupported = trackGroup.isTrackSupported(i)
                val isSelected = trackGroup.isTrackSelected(i)
                val trackFormat = trackGroup.getTrackFormat(i)
                L.i(
                    "$tag isSupported: $isSupported, isSelected: $isSelected ,trackFormat: $trackFormat"
                )
            }
            L.i(
                "$tag trackType: $trackType,trackInGroupIsSelected: $trackInGroupIsSelected \n trackInGroupIsSupported: $trackInGroupIsSupported"
            )
        }
    }

    /**
     * 取消定时回调
     * 详情看父类
     */
    override fun cancelCallBack() {
        mCancelPlayerPositionCallBackFlag = false
    }

    override fun isPlay(): Boolean {
        return mPlayer.isPlaying
    }

    override fun changeTrack(audioTrackType: AudioTrackType) {
        L.i("$tag changeTrack: ${audioTrackType.name}")
        mDefaultTrack = audioTrackType
        if (!mPlayer.playWhenReady) {
            L.i("$tag 播放器还未准备好，无法切换音轨")
            return
        }
        val groups = mPlayer.currentTracks.groups
        if (groups.isEmpty()) {
            L.e("$tag trackGroup is empty")
            return
        }

        val tempTrackGroup = if (AudioTrackType.ACC == audioTrackType) {
            groups.last().mediaTrackGroup
        } else {
            groups.first().mediaTrackGroup
        }

        mPlayer.trackSelectionParameters = mPlayer.trackSelectionParameters
            .buildUpon()
            .setOverrideForType(
                TrackSelectionOverride(tempTrackGroup, 0)
            )
            .build()
    }

    override fun setPlayMode(playMode: PlayMode?) {
        super.setPlayMode(playMode)
//        when (mPlayMode) {
//            PlayMode.SINGLE -> {
//                mPlayer.repeatMode = Player.REPEAT_MODE_ONE
//            }
//
//            PlayMode.CYCLE -> {
//                mPlayer.repeatMode = Player.REPEAT_MODE_ALL
//            }
//
//            else -> {
//                mPlayer.repeatMode = Player.REPEAT_MODE_OFF
//            }
//        }
        L.i("mPlayer.repeatMode: ${mPlayer.repeatMode}")
    }

    override fun startPlay() {
        mPlayer.play()
    }

    override fun playUrl(url: String, urlType: UrlType) {
        if (TextUtils.isEmpty(url)){
            L.i("$tag playUrl is empty send error")
            mPositionCallBackList.dispatch {
                errorUrl(getCurrentInfo())
            }
            return
        }
        mPositionCallBackList.dispatch {
            L.i("$tag playUrl: willStart")
            this.willStart(getCurrentInfo())
        }
        if (urlType == UrlType.M3U8) {
            val hlsMediaSource =
                HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(MediaItem.fromUri(url))
            mPlayer.setMediaSource(hlsMediaSource)
        } else {
            mPlayer.setMediaItem(MediaItem.fromUri(url))
        }
        mPlayer.prepare()
        mPlayerHasPrepare = true

    }

    override fun prepareUrl(url: String, urlType: UrlType) {
//        L.i(" $tag prepareUrl: $url urlType: ${urlType.name}")
        if (TextUtils.isEmpty(url)){
            mPositionCallBackList.dispatch {
                errorUrl(getCurrentInfo())
            }
            return
        }
        mPositionCallBackList.dispatch {
            L.i("$tag prepareUrl: willStart")
            this.willStart(getCurrentInfo())
        }
        mPlayer.playWhenReady = false
        if (urlType == UrlType.M3U8) {
            val hlsMediaSource =
                HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(MediaItem.fromUri(url))
            mPlayer.setMediaSource(hlsMediaSource)
        } else {
            mPlayer.setMediaItem(MediaItem.fromUri(url))
        }
        mPlayer.prepare()
    }

    override fun playerPause() {
        mPlayer.pause()
    }

    override fun stop() {
        mPlayNow = false
        mPlayer.stop()
    }

    override fun getPlayPosition(): Long {
        return mPlayer.currentPosition
    }

    override fun setPlayWhenReady(ready: Boolean) {
        L.i("$tag setPlayWhenReady: $ready")
        mPlayNow = ready
        mPlayer.playWhenReady = ready
    }

    override fun seek(position: Long) {
        mPlayer.seekTo(position)
    }

    override fun setKey(key: Int) {
        val keyValue = 1f + (key - MID_KEY) * 0.05f
        mPlayer.playbackParameters = PlaybackParameters(1f, keyValue)
    }

    override fun replay() {
        mPlayer.seekTo(0)
        if (!mPlayer.isPlaying) {
            mPlayer.play()
        }
    }

    override fun attachSurfaceHolder(holder: SurfaceHolder) {
        mPlayer.setVideoSurfaceHolder(holder)
        mHasAttachSurfaceHolder = true
    }

    override fun clearSurfaceHolder(holder: SurfaceHolder) {
        mPlayer.clearVideoSurfaceHolder(holder)
        mHasAttachSurfaceHolder = false
    }

    override fun mute() {
        mPlayer.volume = 0f
    }

    override fun release() {
        super.release()
        L.i("$tag release")
        mPlayer.release()
    }

    override fun getCurrentAudioTrack(): AudioTrackType {
        for (trackGroup in mPlayer.currentTracks.groups) {
            // Group level information.
            val trackType = trackGroup.type
            val trackInGroupIsSelected = trackGroup.isSelected
            val trackInGroupIsSupported = trackGroup.isSupported
            for (i in 0 until trackGroup.length) {
                // Individual track information.
                val isSupported = trackGroup.isTrackSupported(i)
                val isSelected = trackGroup.isTrackSelected(i)
                val trackFormat = trackGroup.getTrackFormat(i)
                L.i(
                    "$tag getCurrentAudioTrack isSupported: $isSupported, isSelected: $isSelected ,trackFormat: $trackFormat"
                )
                if (isSelected){
                    return if (i == 0) {
                        AudioTrackType.ACC
                    } else {
                        AudioTrackType.ORIGIN
                    }
                }
            }
            L.i(
                "$tag trackType: $trackType,trackInGroupIsSelected: $trackInGroupIsSelected \n trackInGroupIsSupported: $trackInGroupIsSupported"
            )

        }
        return AudioTrackType.ACC
    }

    override fun toggle() {
        L.i("mPlayer.isPlaying ${mPlayer.isPlaying}")
        if (mPlayer.isPlaying){
            pause()
        }else{
            play(mIndex)
        }
    }
}