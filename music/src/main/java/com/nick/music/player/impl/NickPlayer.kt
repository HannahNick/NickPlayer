package com.nick.music.player.impl

import android.media.MediaPlayer
import android.os.Build
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.LogUtils
import com.nick.base.BaseUrl
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
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

                mPositionCallBackList.forEach { callback->
                    callback.prepareStart(getCurrentInfo())
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

    override fun playerPause() {
        mMediaPlayer.pause()
    }

    override fun getPlayPosition(): Int {
        return mMediaPlayer.currentPosition
    }

    override fun attachSurfaceHolder(holder: SurfaceHolder) {
        mMediaPlayer.setDisplay(holder)
    }

}