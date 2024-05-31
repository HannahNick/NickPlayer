package com.nick.vod.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import com.nick.music.player.PlayerControl
import com.nick.music.server.PlayStatus
import com.nick.vod.R
import com.nick.vod.wiget.GestureMessageCenter

class VodGestureControlLayer @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr){

    private lateinit var mPlayerControl: PlayerControl
    private val mHandler = Handler(Looper.getMainLooper())
    private val mGestureDetectorCompat = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            onToggleVisibility()
            return super.onDown(e)
        }

    })

    /**
     * ui
     */
    private lateinit var mIvPlay : AppCompatImageView
//    private lateinit var mIvCenterPlay : AppCompatImageView
    private lateinit var mIvFullScreen: AppCompatImageView
    private lateinit var mIvBack: AppCompatImageView
    private lateinit var mTvName: AppCompatTextView
    private lateinit var mSbSeek: AppCompatSeekBar

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        children.forEach {
            when (it.id){
                R.id.iv_play -> mIvPlay = it as AppCompatImageView
//                R.id.iv_center_play -> mIvCenterPlay = it as AppCompatImageView
                R.id.iv_full_screen -> mIvFullScreen = it as AppCompatImageView
                R.id.iv_back -> mIvBack = it as AppCompatImageView
                R.id.tv_live_name -> mTvName = it as AppCompatTextView
                R.id.sb_seek -> mSbSeek = it as AppCompatSeekBar
            }
        }
        mIvPlay.setOnClickListener {
            playOrPause()
        }
//        mIvCenterPlay.setOnClickListener {
//            playOrPause()
//        }
        mIvFullScreen.setOnClickListener {
//            GestureMessageCenter.sendFullScreen()
        }
        mIvBack.setOnClickListener {
            GestureMessageCenter.sendBack()
        }
        mSbSeek.setOnSeekBarChangeListener(object :OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                GestureMessageCenter.seek(seekBar.progress)
            }

        })
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mGestureDetectorCompat.onTouchEvent(event)
    }


    private fun playOrPause(){
        val playInfo = mPlayerControl.getCurrentInfo()
        if (playInfo.playStatus == PlayStatus.PLAY){
            mIvPlay.setImageResource(R.drawable.ic_play)
//            mIvCenterPlay.setImageResource(R.drawable.ic_play)
            mPlayerControl.pause()
        }else{
            mIvPlay.setImageResource(R.drawable.ic_live_pause)
//            mIvCenterPlay.setImageResource(R.drawable.ic_live_pause)
            mPlayerControl.play()
        }
        onToggleVisibility(true)
    }

    fun flushPlayStatus(){
        val playInfo = mPlayerControl.getCurrentInfo()
        if (playInfo.playStatus == PlayStatus.PLAY){
            mIvPlay.setImageResource(R.drawable.ic_live_pause)
//            mIvCenterPlay.setImageResource(R.drawable.ic_live_pause)
        }else{
            mIvPlay.setImageResource(R.drawable.ic_play)
//            mIvCenterPlay.setImageResource(R.drawable.ic_play)
        }
    }

    private fun onToggleVisibility(statusChange: Boolean = false) {
        mHandler.removeCallbacksAndMessages(null)
        children.forEach { v ->
            if (v.getTag()=="invisible"){
                return@forEach
            }
            if (statusChange){
                mHandler.postDelayed({
                    v.visibility = View.GONE
                },3000)
                return@forEach
            }
            if (v.isVisible){
                v.visibility = View.GONE
            } else{
                v.visibility = View.VISIBLE
                mHandler.postDelayed({
                    v.visibility = View.GONE
                },3000)
            }
        }
    }

    fun initMusicBinder(playerControl: PlayerControl){
        mPlayerControl = playerControl
    }

    fun setVodName(name: String){
//        mTvName.text = name
    }

    fun setPlayStart(){
        mIvPlay.setImageResource(R.drawable.ic_live_pause)
        onToggleVisibility(true)
    }


    interface GestureCallBack{
        fun fullScreen(){}
        fun back(){}
    }
}