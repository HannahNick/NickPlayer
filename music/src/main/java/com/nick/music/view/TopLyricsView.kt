package com.nick.music.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

class TopLyricsView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0):KrcLineView(context, attributeSet, defStyleAttr) {

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        measureHaveSingRect()
        // 绘制歌词
        canvas.drawText(mLineLyrics, mStartPosition, mWordsPaint.textSize, mWordsPaint)
        canvas.save()
        canvas.clipRect(mWordsSingRect)
        canvas.drawText(mLineLyrics,mStartPosition,mWordsSingPaint.textSize,mWordsSingPaint)
        canvas.restore()
    }
}