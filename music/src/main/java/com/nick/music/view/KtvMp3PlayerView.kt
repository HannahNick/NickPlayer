package com.nick.music.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.SurfaceView
import android.widget.FrameLayout
import com.blankj.utilcode.util.FileUtils
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
import com.nick.music.R
import com.nick.music.krc.KrcLyricsFileReader
import com.nick.music.player.PlayerControl
import com.nick.music.player.impl.NickExoPlayer
import com.nick.music.server.PlayMode
import java.io.File

class KtvMp3PlayerView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0,private val playerControl:  PlayerControl = NickExoPlayer(context))
    : FrameLayout(context, attributeSet, defStyleAttr),PlayerControl by playerControl{

    private val mSurfaceView: SurfaceView
    private val mKTVLyricsView: KTVLyricsView
    private val mKrcReader = KrcLyricsFileReader()

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_mp3_player,this,false)
        mSurfaceView = view.findViewById(R.id.sv_video)
        mKTVLyricsView = view.findViewById(R.id.ktv_lyric)
        addView(view)
        playerControl.attachSurfaceHolder(mSurfaceView.holder)
        playerControl.setPlayMode(PlayMode.SINGLE)
    }

    fun setKrc(krcFile: File){
        if (!FileUtils.isFileExists(krcFile)){
            // TODO: 加日志
            return
        }
        val krcInfo = mKrcReader.readFile(krcFile)
        if (krcInfo!=null){
            mKTVLyricsView.release()
            mKTVLyricsView.setData(krcInfo)
        }else{
            // TODO: 加日志
        }
    }

    fun setLoopFile(loopFile: File){
        if (!FileUtils.isFileExists(loopFile)){
            // TODO: 加日志
            return
        }
        playerControl.setPlayList(listOf(MusicVo(path = loopFile.absolutePath, pathType = UrlType.DEFAULT, liveName = "")))
    }

    fun setPlayerPosition(position: Long){
        mKTVLyricsView.setCurrentPosition(position)
    }

    override fun release() {
        playerControl.release()
        mKTVLyricsView.release()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.layout(0, 0, child.measuredWidth, child.measuredHeight)
        }
    }

}