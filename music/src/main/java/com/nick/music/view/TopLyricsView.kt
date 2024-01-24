package com.nick.music.view

import android.content.Context
import android.graphics.Canvas
import android.text.TextUtils
import android.util.AttributeSet
import com.blankj.utilcode.util.LogUtils

/**
 * 顶部歌词、居左显示
 */
class TopLyricsView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0):KrcLineView(context, attributeSet, defStyleAttr) {


    override fun doDraw(canvas: Canvas) {
        if (mLineLyricsList.isNotEmpty()&&mLineLyricsList[0].hasTransliteration){
            drawSingLyrics2(canvas)
        }else{
            drawSingLyrics(canvas)
        }
    }

    override fun drawPreView(canvas: Canvas) {
        canvas.drawText(mOriginLineLyrics, mPaintStartPosition, mOriginStartPositionY, mOriginWordsStrokePaint)
        canvas.drawText(mOriginLineLyrics, mPaintStartPosition, mOriginStartPositionY, mOriginWordsPaint)
        if (mLineLyricsList.isNotEmpty()&&mLineLyricsList[0].hasTransliteration){
            drawPreViewSubsidiaryWords(canvas)
        }else{
            canvas.drawText(mSubsidiaryLineLyrics, mPaintStartPosition, mSubsidiaryStartPositionY, mSubsidiaryWordsStrokePaint)
            canvas.drawText(mSubsidiaryLineLyrics, mPaintStartPosition, mSubsidiaryStartPositionY, mSubsidiaryWordsPaint)
        }

    }

    override fun drawSingFinish(canvas: Canvas) {
        canvas.drawText(mOriginLineLyrics, mPaintStartPosition, mOriginStartPositionY, mWordsSingStrokePaint)
        canvas.drawText(mOriginLineLyrics, mPaintStartPosition, mOriginStartPositionY, mWordsSingPaint)
        if (mLineLyricsList.isNotEmpty()&&mLineLyricsList[0].hasTransliteration){
            drawSubsidiaryLyrics(canvas)
            drawHaveSingSubsidiaryLyrics(canvas)
        }else{
            canvas.drawText(mSubsidiaryLineLyrics, mPaintStartPosition, mSubsidiaryStartPositionY, mSubsidiaryWordsStrokePaint)
            canvas.drawText(mSubsidiaryLineLyrics, mPaintStartPosition, mSubsidiaryStartPositionY, mSubsidiaryWordsPaint)
        }

    }


    override fun isTopLyrics(): Boolean {
        return true
    }

    /**
     * 画主歌词和翻译歌词
     */
    private fun drawSingLyrics(canvas: Canvas){
        measureHaveSingRect()
        // 绘制原始歌词
        canvas.drawText(mOriginLineLyrics, mPaintStartPosition, mOriginStartPositionY, mOriginWordsStrokePaint)
        canvas.drawText(mOriginLineLyrics, mPaintStartPosition, mOriginStartPositionY, mOriginWordsPaint)
        // 绘制副歌词
        canvas.drawText(mSubsidiaryLineLyrics, mPaintStartPosition, mSubsidiaryStartPositionY, mSubsidiaryWordsStrokePaint)
        canvas.drawText(mSubsidiaryLineLyrics, mPaintStartPosition, mSubsidiaryStartPositionY, mSubsidiaryWordsPaint)
        canvas.save()
        canvas.clipRect(mWordsSingRect)
        // 绘制已唱歌词
        canvas.drawText(mOriginLineLyrics,mPaintStartPosition,mOriginStartPositionY,mWordsSingStrokePaint)
        canvas.drawText(mOriginLineLyrics,mPaintStartPosition,mOriginStartPositionY,mWordsSingPaint)
        canvas.restore()
    }

    /**
     * 画主歌词和音译歌词
     */
    private fun drawSingLyrics2(canvas: Canvas){
        measureHaveSingRect()
        // 绘制原音歌词
        canvas.drawText(mOriginLineLyrics,mPaintStartPosition,mOriginStartPositionY,mOriginWordsStrokePaint)
        canvas.drawText(mOriginLineLyrics,mPaintStartPosition,mOriginStartPositionY,mOriginWordsPaint)
        // 逐个绘制副歌词
        drawSubsidiaryLyrics(canvas)
        canvas.save()
        canvas.clipRect(mWordsSingRect)
        // 绘制已唱歌词
        canvas.drawText(mOriginLineLyrics,mPaintStartPosition,mOriginStartPositionY,mWordsSingStrokePaint)
        canvas.drawText(mOriginLineLyrics,mPaintStartPosition,mOriginStartPositionY,mWordsSingPaint)
        //绘制已唱副歌词
        drawHaveSingSubsidiaryLyrics(canvas)
        canvas.restore()
    }

    /**
     * 画副歌词
     */
    private fun drawSubsidiaryLyrics(canvas: Canvas){
        if (TextUtils.isEmpty(mOriginLineLyrics)){
            return
        }

        var currentX = mPaintStartPosition
        val krcLineWord = mLineLyricsList[mCurrentLineIndex]
        krcLineWord.originArray.forEachIndexed { index, originChar ->
            val transliteration = krcLineWord.transliterationArray[index]
            // 计算注音的宽度
            val transliterationWidth = mSubsidiaryWordsPaint.measureText(transliteration)
            // 绘制拼音，居中显示在汉字的正上方
            canvas.drawText(transliteration, currentX + (mOriginWordsPaint.measureText(originChar) - transliterationWidth) / 2, mSubsidiaryStartPositionY, mSubsidiaryWordsStrokePaint)
            canvas.drawText(transliteration, currentX + (mOriginWordsPaint.measureText(originChar) - transliterationWidth) / 2, mSubsidiaryStartPositionY, mSubsidiaryWordsPaint)
            // 移动 X 坐标，为下一个汉字和拼音腾出空间
            currentX += mOriginWordsPaint.measureText(originChar)
        }
    }

    /**
     * 画已唱副歌词
     */
    private fun drawHaveSingSubsidiaryLyrics(canvas: Canvas){
        if (TextUtils.isEmpty(mOriginLineLyrics)){
            return
        }

        var currentX = mPaintStartPosition
        val krcLineWord = mLineLyricsList[mCurrentLineIndex]
        krcLineWord.originArray.forEachIndexed { index, originChar ->
            val transliteration = krcLineWord.transliterationArray[index]
            // 计算注音的宽度
            val transliterationWidth = mHaveSingSubsidiaryWordsPaint.measureText(transliteration)
            // 绘制拼音，居中显示在汉字的正上方
            canvas.drawText(transliteration, currentX + (mOriginWordsPaint.measureText(originChar) - transliterationWidth) / 2, mSubsidiaryStartPositionY, mHaveSingSubsidiaryWordsStrokePaint)
            canvas.drawText(transliteration, currentX + (mOriginWordsPaint.measureText(originChar) - transliterationWidth) / 2, mSubsidiaryStartPositionY, mHaveSingSubsidiaryWordsPaint)
            // 移动 X 坐标，为下一个汉字和拼音腾出空间
            currentX += mOriginWordsPaint.measureText(originChar)
        }
    }

    private fun drawPreViewSubsidiaryWords(canvas: Canvas){
        if (mLineLyricsList.size<=mPreViewLineIndex){
            return
        }
        if (TextUtils.isEmpty(mOriginLineLyrics)){
            return
        }
        var currentX = mPaintStartPosition
        val krcLineWord = mLineLyricsList[mPreViewLineIndex]
//        LogUtils.i("顶部画预览歌词>>>${krcLineWord.origin}")
        krcLineWord.originArray.forEachIndexed { index, originChar ->
            val transliteration = krcLineWord.transliterationArray[index]
            // 计算注音的宽度
            val transliterationWidth = mSubsidiaryWordsPaint.measureText(transliteration)
            // 绘制拼音，居中显示在汉字的正上方
            canvas.drawText(transliteration, currentX + (mOriginWordsPaint.measureText(originChar) - transliterationWidth) / 2, mSubsidiaryStartPositionY, mSubsidiaryWordsStrokePaint)
            canvas.drawText(transliteration, currentX + (mOriginWordsPaint.measureText(originChar) - transliterationWidth) / 2, mSubsidiaryStartPositionY, mSubsidiaryWordsPaint)
            // 移动 X 坐标，为下一个汉字和拼音腾出空间
            currentX += mOriginWordsPaint.measureText(originChar)
        }
    }


    /**
     * 测量已唱的框框
     */
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