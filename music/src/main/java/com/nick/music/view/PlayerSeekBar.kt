package com.nick.music.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.nick.music.R
import com.xyz.base.utils.L

class PlayerSeekBar@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr){

    /**
     * 当前播放进度
     */
    private var mCurrentPosition: Int = 0

    /**
     * 总播放时长
     */
    private var mTotalDuration: Int = 0

    /**
     * 格式化播放时间
     */
    private var mThumbText: String = "00:00/00:00"

    /**
     * 格式化文本框框
     */
    private val mTextBounds = Rect()

    /**
     * 文本背景框
     */
    private val mRectF = RectF()
    private val mPadding = 10f

    /**
     * 播放偏移量
     */
    private var mPlayOffset = 0f

    /**
     * 是否遥控seek
     */
    private var mIsControlSeek = false

    private var mSeekCallBack: SeekCallBack? = null

    private val mTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 20f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val mDurationLinePaint = Paint().apply {
        color = ContextCompat.getColor(context,R.color.black_50) // 框的颜色
        strokeWidth = 3f
    }
    private val mPlayFinishLinePaint = Paint().apply {
        color = ContextCompat.getColor(context,R.color.white_100) // 框的颜色
        strokeWidth = 3f
    }

    private val mRectPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context,R.color.black_30) // 框的颜色
        strokeWidth = 10f // 边界的宽度
        isAntiAlias = true // 抗锯齿
    }

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (gainFocus){
            mRectPaint.color = ContextCompat.getColor(context,R.color.white_50)
        }else{
            mRectPaint.color = ContextCompat.getColor(context,R.color.black_30)
        }
        invalidate()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                mCurrentPosition = if (mCurrentPosition-5000>=0){
                    mCurrentPosition-5000
                }else{
                    0
                }
                mIsControlSeek = true
                updateSeek(mCurrentPosition)
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                mCurrentPosition = if (mCurrentPosition+5000>=mTotalDuration){
                    mTotalDuration
                }else{
                    mCurrentPosition+5000
                }
                mIsControlSeek = true
                updateSeek(mCurrentPosition)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                mSeekCallBack?.playSeek(mCurrentPosition)
                postDelayed({
                    mIsControlSeek = false
                },1000)

                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                mSeekCallBack?.playSeek(mCurrentPosition)
                postDelayed({
                    mIsControlSeek = false
                },1000)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }



    fun setTotalDuration(duration: Int){
        mTotalDuration = duration
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mTextPaint.getTextBounds(mThumbText,0, mThumbText.length, mTextBounds)
        mRectF.set(
            mPlayOffset, // 左边界
            (height-mTextBounds.height())/2f, // 上边界
            mTextBounds.width().toFloat() + 2*mPadding + mPlayOffset, // 右边界
            mTextBounds.height().toFloat() + (height-mTextBounds.height())/2f// 下边界
        )
        canvas.drawLine(0f,height/2f,width.toFloat(),height/2f,mDurationLinePaint)
        canvas.drawLine(0f,height/2f,mPlayOffset,height/2f,mPlayFinishLinePaint)
        canvas.drawRoundRect(mRectF,100f,100f,mRectPaint)
        canvas.drawText(mThumbText,0,mThumbText.length,mTextBounds.width().toFloat()/2 + mPadding + mPlayOffset,height/2f + mTextBounds.height()/2f -1,mTextPaint)
    }

    fun updateThumbText(currentPosition: Int) {
        if (mIsControlSeek){
            return
        }
        mPlayOffset = currentPosition*(width-mRectF.width()).toFloat()/mTotalDuration
        mCurrentPosition = currentPosition
        mThumbText = "${formatTime(currentPosition)}/${formatTime(mTotalDuration)}"
        invalidate()
    }

    private fun updateSeek(currentPosition: Int) {
        mPlayOffset = currentPosition*(width-mRectF.width()).toFloat()/mTotalDuration
        mCurrentPosition = currentPosition
        mThumbText = "${formatTime(currentPosition)}/${formatTime(mTotalDuration)}"
        invalidate()
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun setSeekCallBack(seekCallBack: SeekCallBack){
        mSeekCallBack = seekCallBack
    }
    fun release(){
        mSeekCallBack = null
    }

    interface SeekCallBack{
        fun playSeek(position: Int)
    }
}