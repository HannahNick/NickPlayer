package com.nick.music.player.impl

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.text.TextUtils
import android.view.SurfaceHolder
import androidx.annotation.OptIn
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
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
import com.xyz.base.utils.NetworkUtils


@UnstableApi
class NickExoPlayer(private val context: Context,
                    val tag: String) : AbsPlayer() {
    private val mDataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
    @SuppressLint("UnsafeOptInUsageError")
    private val mPlayer = ExoPlayer.Builder(
        /* context = */ context,
        /* renderersFactory = */ DefaultRenderersFactory(context)
            .apply {
                setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            }
    ).build()
    //组合控制音轨切换(对于K歌是有默认选音轨的,音乐则交给播放器自己选默认音轨)
    private var mDefaultTrack = AudioTrackType.ACC
    //默认音轨下标
    private var mDefaultTrackIndex = 1

    //key值分成24个等级，0~24，12为中间值也就是不升也不降，Int类型是因为由SeekBar控制
    private val MID_KEY = 12
    //当前key
    private var mCurrentKey = 1f
    //最大错误次数
    private val MAX_ERROR_TIMES = 30
    //是否需要seek到指定位置播放
    private var mNeedResumeSeek = false
    //网络错误记录的关键位置
    private var mNeedSeekPosition = 0L

    private val mReceiver: NetworkChangeReceiver
    init {
        mPlayer.addAnalyticsListener(EventLogger(tag))
        mPlayer.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                L.i("$tag onPlaybackStateChanged: $playbackState")
                mPlayInfoCallBackList.dispatch {
                    onPlaybackStateChanged(playbackState)
                }
                when (playbackState) {
                    Player.STATE_READY -> {
                        L.i("$tag duration: ${mPlayer.duration}")
                        mNeedResumeSeek = false
                        mNeedSeekPosition = 0L
                        mPlayInfoCallBackList.dispatch {
                            showPlayLoading(false)
                        }
                        mDuration = mPlayer.duration
                        changeTrack(mDefaultTrackIndex)
                        setKey(mCurrentKey)
                        mPlayInfoCallBackList.dispatch {
                            L.i("$tag 回调准备开始")
                            prepareStart(getCurrentPlayInfo())
                        }

                        if (mPlayNow) {
//                            BackdoorWindow.send("$tag mPlayNow currentPosition:${getPlayPosition()}")
                            mPlayer.play()
                            L.i("$tag 播放器已播放")
                            mPlayInfoCallBackList.dispatch {
                                startPlay(getPlayPosition())
                            }

                            L.i("$tag 已回调开始播放")
                        }
                        mErrorTimes = 0
                    }

                    Player.STATE_ENDED -> {
                        L.i("$tag 播放结束 playEnd mode:${mPlayMode.name} mIndex:$mIndex dataSize: ${mMusicData.size}")
                        mPlayInfoCallBackList.dispatch {
                            playEnd()
                        }
                        next()
                    }

                    Player.STATE_BUFFERING -> {
                        L.i("$tag Player.STATE_BUFFERING")
                        if (!NetworkUtils.isNetworkAvailable(context)){
                            L.i("$tag Network error buffer mNeedSeekPosition:$mNeedSeekPosition")
                        }

                        mPlayInfoCallBackList.dispatch {
                            showPlayLoading(true)
                        }
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
                L.e("$tag errorCode:${error.errorCode} pausePosition:${mCurrentPosition} onPlayerError: $error, ")
                error.printStackTrace()
                mPlayInfoCallBackList.dispatch {
                    playError(error)
                }
                mPlayerHasPrepare = false
                //异常重试、在错误的位置进行重试
                saveErrorPosition()
                if (mPlayNow){
                    play(mIndex)
                }else{
                    prepare(mIndex)
                }

//                mErrorTimes++
//                if (mErrorTimes>=MAX_ERROR_TIMES){
//                    saveErrorPosition()
//                    play(mIndex)
//                }

            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int,
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
                mPlayStatusEx = if (isPlaying) PlayStatus.PLAY else PlayStatus.PAUSE
                mPlayInfoCallBackList.dispatch {
                    playingStatusChanged(isPlaying)
                }
            }

        })
        mReceiver = NetworkChangeReceiver()
        registerReceiver()
        super.init(tag)
    }



    private fun registerReceiver(){
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        mReceiver.netWorkChangeListener = object : NetWorkChangeListener {
            override fun netWorkChange(netWorkCanUse: Boolean) {
                //网络恢复后如果没有在播,而且需要恢复到断网前的位置
                if(netWorkCanUse && !isPlay() && mNeedResumeSeek){
                    L.i("$tag getNetworkResume seek to play")
                    play(mIndex)
                }
            }
        }
        context.registerReceiver(mReceiver,intentFilter)
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
        //默认音轨控制
//        if (!mNeedLoadDefaultTracks){
//            L.i("$tag changeTrack option is off")
//            return
//        }
//        L.i("$tag changeTrack: ${audioTrackType.name}")
//        mDefaultTrack = audioTrackType
//        if (!mPlayer.playWhenReady) {
//            L.i("$tag 播放器还未准备好，无法切换音轨")
//            return
//        }
//        val groups = mPlayer.currentTracks.groups
//        if (groups.isEmpty()) {
//            L.e("$tag trackGroup is empty")
//            return
//        }
//
//        val tempTrackGroup = if (AudioTrackType.ACC == audioTrackType) {
//            groups.last().mediaTrackGroup
//        } else {
//            groups.first().mediaTrackGroup
//        }
//
//        mPlayer.trackSelectionParameters = mPlayer.trackSelectionParameters
//            .buildUpon()
//            .setOverrideForType(
//                TrackSelectionOverride(tempTrackGroup, 0)
//            )
//            .build()
    }

    override fun changeTrack(trackIndex: Int) {
        mDefaultTrackIndex = trackIndex
        L.i("$tag changeTrack: $trackIndex")
        if (!mPlayer.playWhenReady){
            L.i("$tag Player is not prepare, can not change track")
            return
        }
        try {
            //默认音轨排序可能回相反，需要根据id来排序

            val mediaTrackGroup = mPlayer.currentTracks.groups
                .sortedBy {
                    it.mediaTrackGroup.getFormat(0).id?.toInt()
                }
//                .apply {
//                    this.forEach {
//                        L.i("$tag sort: ${it.mediaTrackGroup.getFormat(0)}")
//                    }
//                }
                .get(trackIndex).mediaTrackGroup
            L.i("$tag id: ${mediaTrackGroup.id}")
            mPlayer.trackSelectionParameters = mPlayer.trackSelectionParameters
                .buildUpon()
                .setOverrideForType(
                    TrackSelectionOverride(mediaTrackGroup, 0)
                )
                .build()
        }catch (e: Exception){
            L.w("$tag Player change track error")
//            e.printStackTrace()
        }

    }

    override fun setPlayList(data: List<MusicVo>, playIndex: Int) {
        super.setPlayList(data, playIndex)
//        mPositionCallBackList.dispatch {
//            L.i("$tag playUrl: willStart")
//            this.willStart(getCurrentInfo())
//        }
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
        setPlayWhenReady(true)
        mPlayer.play()
    }

    override fun playUrl(url: String, urlType: UrlType) {
        if (TextUtils.isEmpty(url)){
            L.i("$tag playUrl is empty send error")
            mPlayInfoCallBackList.dispatch {
                errorUrl(getCurrentPlayInfo())
            }
            return
        }
        mPlayStatusEx = PlayStatus.PREPARE
        mPlayInfoCallBackList.dispatch {
            L.i("$tag playUrl: willStart")
            this.willStart(getCurrentPlayInfo())
        }
        if (urlType == UrlType.M3U8) {
            val hlsMediaSource =
                HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(MediaItem.fromUri(url))
            mPlayer.setMediaSource(hlsMediaSource)
        } else {
            mPlayer.setMediaItem(MediaItem.fromUri(url))
        }
        if (mNeedResumeSeek){
            L.i("$tag needStartSeek :${mNeedSeekPosition}")
            seek(mNeedSeekPosition)
        }

        mPlayer.prepare()
        mPlayerHasPrepare = true

    }

    override fun prepareUrl(url: String, urlType: UrlType) {
//        L.i(" $tag prepareUrl: $url urlType: ${urlType.name}")
        if (TextUtils.isEmpty(url)){
            mPlayInfoCallBackList.dispatch {
                errorUrl(getCurrentPlayInfo())
            }
            return
        }
        mPlayStatusEx = PlayStatus.PREPARE
        mPlayInfoCallBackList.dispatch {
            L.i("$tag prepareUrl: willStart")
            this.willStart(getCurrentPlayInfo())
        }
        mPlayer.playWhenReady = false
//        L.i("$tag prepareUrl: $url")
        if (urlType == UrlType.M3U8) {
            val hlsMediaSource =
                HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(MediaItem.fromUri(url))
            mPlayer.setMediaSource(hlsMediaSource)
        } else {
            mPlayer.setMediaItem(MediaItem.fromUri(url))
        }
        if (mNeedResumeSeek){
            L.i("$tag needStartSeek :${mNeedSeekPosition}")
            seek(mNeedSeekPosition)
        }
        mPlayer.prepare()
//        L.i("$tag prepareUrl is invoke")
        mPlayerHasPrepare = true
    }

    override fun playerPause() {
        mPlayer.pause()
    }

    override fun stop() {
        mPlayNow = false
        pause()
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
        L.i("$tag isSeekable: ${mPlayer.isCurrentMediaItemSeekable}")
        mPlayer.seekTo(position)
    }

    override fun setKey(key: Int) {
        mCurrentKey = 1f + (key - MID_KEY) * 0.05f
        if (!mPlayer.playWhenReady) {
            L.i("$tag mPlayer is not ready can not set key")
            return
        }
        mPlayer.playbackParameters = PlaybackParameters(1f, mCurrentKey)
    }

    override fun setKey(key: Float) {
        mCurrentKey = key
        if (!mPlayer.playWhenReady) {
            L.i("$tag mPlayer is not ready can not set key")
            return
        }
        mPlayer.playbackParameters = PlaybackParameters(1f, mCurrentKey)
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
        mReceiver.netWorkChangeListener = null
        context.unregisterReceiver(mReceiver)
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
        L.i("mPlayer.isPlaying ${mPlayer.isPlaying} mPlayNow:$mPlayNow")
        if (mPlayNow){
            pause()
        }else{
            resumePlay()
        }
    }

    override fun resumePlay() {
        L.i("$tag resumePlay")
        mPlayerHasPrepare = true
        play(mIndex)
        //这种情况是异常暂停后没恢复而且播放状态未进入准备时，需要重新开始播
        if (!mPlayer.isPlaying && mPlayStatus != PlayStatus.PREPARE){
            L.i("$tag restart play")
            saveErrorPosition()
            mPlayerHasPrepare = false
            play(mIndex)
        }
    }

    private fun saveErrorPosition(){
        mNeedResumeSeek = true
        mNeedSeekPosition = mCurrentPosition
    }

    override fun pause() {
        setPlayWhenReady(false)
        super.pause()
    }

    @OptIn(UnstableApi::class)
    override fun getSessionId(): Int {
        return mPlayer.audioSessionId
    }

    override fun dispatchSongIsRemove(needCallUI: Boolean) {
        mPlayInfoCallBackList.dispatch {
            songIsRemove(getCurrentPlayInfo(),needCallUI)
        }
    }


    class NetworkChangeReceiver : BroadcastReceiver() {
        var netWorkChangeListener: NetWorkChangeListener? = null
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.apply {
                netWorkChangeListener?.netWorkChange(NetworkUtils.isNetworkAvailable(this))
            }
        }
    }

    interface NetWorkChangeListener{
        fun netWorkChange(netWorkCanUse: Boolean)
    }
}