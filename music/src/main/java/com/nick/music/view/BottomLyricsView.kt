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

    override fun doDraw(canvas: Canvas) {
        drawSingLyrics(canvas)
    }

    override fun drawPreView(canvas: Canvas) {
        measurePreViewLyrics()
        canvas.drawText(mOriginLineLyrics, mViewWith - mMeasureRect.right,mOriginStartPositionY, mOriginWordsPaint)
        canvas.drawText(mSubsidiaryLineLyrics, mViewWith - mMeasureRect.right, mSubsidiaryStartPositionY, mSubsidiaryWordsPaint)
    }

    override fun drawSingFinish(canvas: Canvas) {
        measurePreViewLyrics()
        canvas.drawText(mOriginLineLyrics,mViewWith - mMeasureRect.right,mOriginStartPositionY,mWordsSingPaint)
        canvas.drawText(mSubsidiaryLineLyrics, mViewWith - mMeasureRect.right, mSubsidiaryStartPositionY, mSubsidiaryWordsPaint)
    }

    override fun isTopLyrics(): Boolean {
        return false
    }

    private fun drawSingLyrics(canvas: Canvas){
        measureLyrics()
        // 绘制歌词
        canvas.drawText(mOriginLineLyrics, mViewWith - mMeasureRect.right, mOriginStartPositionY, mOriginWordsPaint)
        canvas.drawText(mSubsidiaryLineLyrics, mViewWith - mMeasureRect.right, mSubsidiaryStartPositionY, mSubsidiaryWordsPaint)
        canvas.save()
        canvas.clipRect(mWordsSingRect)
        canvas.drawText(mOriginLineLyrics,mViewWith - mMeasureRect.right,mOriginStartPositionY,mWordsSingPaint)
        canvas.restore()
    }

    /**
     * 测量要显示的歌词
     */
    private fun measureLyrics(){
        if (mOriginLineLyrics.isEmpty()){
            return
        }
        mOriginWordsPaint.getTextBounds(mOriginLineLyrics, 0, mOriginLineLyrics.length, mMeasureRect)
        //居右显示
        mWordsSingRect.left = mViewWith-mMeasureRect.right
        mWordsSingRect.top = 0f
        mWordsSingRect.bottom = height.toFloat()

        val haveSingTextWidth = if (mCurrentWordIndex==0){
            0f
        }else{
            mOriginWordsPaint.measureText(mOriginLineLyrics.substring(0 until getHaveSingWordsLength()))
        }
        val currentWordsWidth = mOriginWordsPaint.measureText(mOriginLineLyrics.substring(0 until getWillSingWordsLength())) - haveSingTextWidth
        //描绘已唱部分核心
        mWordsSingRect.right = ((mCurrentPlayPosition - mCurrentWordStartTime)*currentWordsWidth/mCurrentWordDuration) + haveSingTextWidth + mWordsSingRect.left
    }

    private fun measurePreViewLyrics(){
        //测量文本边界
        mOriginWordsPaint.getTextBounds(mOriginLineLyrics, 0, mOriginLineLyrics.length, mMeasureRect)
    }
}