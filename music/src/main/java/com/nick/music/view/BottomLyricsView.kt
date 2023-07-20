package com.nick.music.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

class BottomLyricsView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0):KrcLineView(context, attributeSet, defStyleAttr) {

    private var mViewWith = 0f

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mViewWith = right.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        measureLyrics()
        // 绘制歌词
        canvas.drawText(mLineLyrics, mViewWith - mMeasureRect.right, mWordsPaint.textSize, mWordsPaint)
        canvas.save()
        canvas.clipRect(mWordsSingRect)
        canvas.drawText(mLineLyrics,mViewWith - mMeasureRect.right,mWordsSingPaint.textSize,mWordsSingPaint)
        canvas.restore()
    }

    /**
     * 测量要显示的歌词
     */
    private fun measureLyrics(){
        mWordsPaint.getTextBounds(mLineLyrics, 0, mLineLyrics.length, mMeasureRect)
        //居右显示
        mWordsSingRect.left = mViewWith-mMeasureRect.right
        mWordsSingRect.top = 0f
        mWordsSingRect.bottom = (mMeasureRect.height()+20).toFloat()

        val haveSingTextWidth = if (mCurrentWordIndex==0){
            0f
        }else{
            mWordsPaint.measureText(mLineLyrics.substring(0 until getHaveSingWordsLength()))
        }
        val currentWordsWidth = mWordsPaint.measureText(mLineLyrics.substring(0 until getWillSingWordsLength())) - haveSingTextWidth
        //描绘已唱部分核心
        mWordsSingRect.right = ((mCurrentPlayPosition - mCurrentWordStartTime)*currentWordsWidth/mCurrentWordDuration) + haveSingTextWidth +mStartPosition
    }
}