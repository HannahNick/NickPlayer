package com.nick.music.player.impl

import android.content.Context
import android.view.SurfaceHolder
import androidx.media3.common.*
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.common.collect.ImmutableList
import com.nick.base.vo.enum.UrlType
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus
import com.nick.music.server.TrackType
import com.xyz.base.utils.L


class NickExoClipPlayer(context: Context): AbsPlayer() {
    private val mDataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
    private val mPlayer = ExoPlayer.Builder(context).build()
    private val mLoadingToken = "LOADING"
    private var mEndPosition = Long.MAX_VALUE
    private var mNeedSeek = false

    private val mPlayerListener = object : Player.Listener{
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            LogUtils.i("playbackState:$playbackState")
            when(playbackState){
                Player.STATE_READY-> {
                    LogUtils.i("duration: ${mPlayer.duration}")
                    mDuration = mPlayer.duration.toInt()
                    mPositionCallBackList.forEach { callback ->
                        LogUtils.i("回调准备开始")
                        showLoading(false)
                        callback.prepareStart(getPlayInfo())
                    }
                    if (mPlayNow){
                        if (mNeedSeek){
                            seek(mClipStartPosition)
                            return
                        }
                        mPlayer.play()
                        LogUtils.i("播放器已播放")
                        mPositionCallBackList.forEach { callback->
                            showLoading(false)
                            callback.startPlay(mPlayer.currentPosition)
                        }
                        LogUtils.i("已回调开始播放")
                        mPlayStatus = PlayStatus.PLAY
                        mErrorTimes = 0
                    }
                }
                Player.STATE_ENDED-> {
                    LogUtils.i("mPlayMode:${mPlayMode.name}")
                    mPositionCallBackList.forEach { callback ->
                        LogUtils.i("回调播放结束")
                        callback.playEnd(mIndex)
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
                        }else->{
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
                else->{
                    showLoading(true)
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
            mPlayer.stop()
            play(mIndex)
        }

        override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
            L.i("onPositionDiscontinuity")
        }

        override fun onTracksChanged(tracks: Tracks) {
            super.onTracksChanged(tracks)
            //当音轨切换成功后，更新UI
            trackLog(tracks.groups)
        }
    }
    init {
        mPlayer.addListener(mPlayerListener)
        super.init()
    }

    private fun showLoading(show: Boolean){
        mHandler.removeCallbacksAndMessages(mLoadingToken)
        if (show){
            mHandler.postDelayed({
                mPositionCallBackList.forEach { callback->
                    callback.loading(true)
                }
            },mLoadingToken,2000)
        }else{
            mPositionCallBackList.forEach { callback->
                callback.loading(false)
            }
        }
    }

    override fun setPlayMode(playMode: PlayMode) {
        super.setPlayMode(playMode)
        when (playMode) {
            PlayMode.SINGLE -> {
                mPlayer.repeatMode = Player.REPEAT_MODE_ONE
            }
            PlayMode.CYCLE -> {
                mPlayer.repeatMode = Player.REPEAT_MODE_ALL
            }
            else -> {
                mPlayer.repeatMode = Player.REPEAT_MODE_OFF
            }
        }
    }

    override fun startPlay() {
        mPlayer.play()
    }

    override fun playUrl(url: String,urlType: UrlType) {
        if (urlType == UrlType.M3U8){
            val hlsMediaSource = HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(MediaItem.fromUri(url))
            mPlayer.setMediaSource(hlsMediaSource)
        }else{
            LogUtils.i("playUrl: $url")
            mPlayer.setMediaItem(MediaItem.fromUri(url))
        }
        mPlayer.prepare()
        mPlayerHasPrepare = true

    }

    override fun prepareUrl(url: String,urlType: UrlType) {
        if (urlType == UrlType.M3U8){
            val hlsMediaSource = HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(MediaItem.fromUri(url))
            mPlayer.setMediaSource(hlsMediaSource)
        }else{
            mPlayer.setMediaItem(MediaItem.fromUri(url))
        }
        mPlayer.prepare()
    }

    override fun prepareUrlByClipping(url: String, urlType: UrlType, start: Long, end: Long) {
        L.i("prepareUrlByClipping start:$start end:$end")
        mIsClip = true
        mClipStartPosition = start
        mPlayNow = true
        if (urlType == UrlType.M3U8){
            mEndPosition = end
            if (start>0){
                mNeedSeek = true
            }
            L.i("prepare m3u8")
            val hlsMediaSource = HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(MediaItem.fromUri(url))
            mPlayer.setMediaSource(hlsMediaSource)
            mPlayer.prepare()
        }else{
            // 创建 MediaSource
            val mediaItem: MediaItem = MediaItem.Builder()
                .setUri(url)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(start)
                        .setEndPositionMs(end)
                        .build()
                )
                .build()
            mPlayer.setMediaItem(mediaItem)
            mPlayer.prepare()
        }

    }

    override fun playerPause() {
        mPlayer.pause()
    }

    override fun getPlayPosition(): Int {
        return mPlayer.currentPosition.toInt()
    }

    override fun callBackPosition(position: Int) {
        L.i("callBackPosition:$position")
        if (position >= mEndPosition) {
            LogUtils.i("position:$position >= mEndPosition: $mEndPosition")
            mPlayer.pause() // 停止播放
            mPlayerListener.onPlaybackStateChanged(Player.STATE_ENDED)
        }
    }

    override fun setPlayWhenReady(ready: Boolean) {
        mPlayer.playWhenReady = ready
    }

    override fun seek(num: Int) {
        mNeedSeek = false
        LogUtils.i("seek: ${num.toLong()}")
        mPlayer.seekTo(num.toLong())
    }

    fun seek(num: Long){
        mNeedSeek = false
        LogUtils.i("seek: $num")
        mPlayer.seekTo(num)
    }

    override fun setKey(key: Float) {
        mPlayer.playbackParameters = PlaybackParameters(1f,key)
    }

    override fun replay() {
        mPlayer.seekTo(0)
        if (!mPlayer.isPlaying){
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

    override fun changeTrack(trackType: TrackType) {
        val groups = mPlayer.currentTracks.groups
        var tempTrackGroup: TrackGroup = groups.first().mediaTrackGroup
        if (TrackType.ACC == trackType){
            tempTrackGroup = groups.last().mediaTrackGroup
        }
        mPlayer.trackSelectionParameters = mPlayer.trackSelectionParameters
                .buildUpon()
                .setOverrideForType(
                    TrackSelectionOverride(tempTrackGroup, 0)
                )
                .build()
    }
    private fun trackLog(groups: ImmutableList<Tracks.Group>){
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
                LogUtils.i("isSupported: $isSupported, isSelected: $isSelected ,trackFormat: $trackFormat")
            }
            LogUtils.i("trackType: $trackType,trackInGroupIsSelected: $trackInGroupIsSelected \n trackInGroupIsSupported: $trackInGroupIsSupported")
        }
    }

    override fun release() {
        super.release()
        mEndPosition = Long.MAX_VALUE
        mNeedSeek = false
        mPlayer.release()
    }

}