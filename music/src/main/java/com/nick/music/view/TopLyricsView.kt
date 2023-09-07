package com.nick.music.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

class TopLyricsView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0):KrcLineView(context, attributeSet, defStyleAttr) {


    override fun doDraw(canvas: Canvas) {
        drawSingLyrics(canvas)
    }

    override fun drawPreView(canvas: Canvas) {
        canvas.drawText(mOriginLineLyrics, mPaintStartPosition, mOriginStartPositionY, mOriginWordsPaint)
        canvas.drawText(mSubsidiaryLineLyrics, mPaintStartPosition, mSubsidiaryStartPositionY, mSubsidiaryWordsPaint)
    }

    override fun drawSingFinish(canvas: Canvas) {
        canvas.drawText(mOriginLineLyrics, mPaintStartPosition, mOriginStartPositionY, mWordsSingPaint)
        canvas.drawText(mSubsidiaryLineLyrics, mPaintStartPosition, mSubsidiaryStartPositionY, mSubsidiaryWordsPaint)
    }


    override fun isTopLyrics(): Boolean {
        return true
    }

    /**
     * 画当前歌词和已唱
     */
    private fun drawSingLyrics(canvas: Canvas){
        measureHaveSingRect()
        // 绘制歌词
        canvas.drawText(mOriginLineLyrics, mPaintStartPosition, mOriginStartPositionY, mOriginWordsPaint)
        canvas.drawText(mSubsidiaryLineLyrics, mPaintStartPosition, mSubsidiaryStartPositionY, mSubsidiaryWordsPaint)
        canvas.save()
        canvas.clipRect(mWordsSingRect)
        canvas.drawText(mOriginLineLyrics,mPaintStartPosition,mOriginStartPositionY,mWordsSingPaint)
        canvas.restore()
    }

    private fun measureHaveSingRect(){
        if (mOriginLineLyrics.isEmpty()){
            return
        }
        // 获取绘制文本的宽度和高度
        mOriginWordsPaint.getTextBounds(mOriginLineLyrics, 0, mOriginLineLyrics.length, mMeasureRect)

        mWordsSingRect.left = mPaintStartPosition
        mWordsSingRect.top = 0f
        mWordsSingRect.bottom = height.toFloat()

        //已唱的宽度
        val haveSingTextWidth = if (mCurrentWordIndex==0){
            0f
        }else{
            mOriginWordsPaint.measureText(mOriginLineLyrics.substring(0 until getHaveSingWordsLength()))
        }
        //测量当前在唱的字的宽度
        val currentWordsWidth = mOriginWordsPaint.measureText(mOriginLineLyrics.substring(0 until getWillSingWordsLength())) - haveSingTextWidth
        //绘制已唱部分核心
        mWordsSingRect.right = ((mCurrentPlayPosition - mCurrentWordStartTime)*currentWordsWidth/mCurrentWordDuration) + haveSingTextWidth +mPaintStartPosition
    }

}