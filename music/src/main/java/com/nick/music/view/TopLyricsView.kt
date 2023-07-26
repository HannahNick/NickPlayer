package com.nick.music.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

class TopLyricsView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0):KrcLineView(context, attributeSet, defStyleAttr) {


    override fun doDraw(canvas: Canvas) {
        drawSingLyrics(canvas)
    }

    override fun drawPreView(canvas: Canvas) {
        canvas.drawText(mLineLyrics, mStartPosition, mWordsPaint.textSize, mWordsPaint)
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
        canvas.drawText(mLineLyrics, mStartPosition, mWordsPaint.textSize, mWordsPaint)
        canvas.save()
        canvas.clipRect(mWordsSingRect)
        canvas.drawText(mLineLyrics,mStartPosition,mWordsSingPaint.textSize,mWordsSingPaint)
        canvas.restore()
    }



}