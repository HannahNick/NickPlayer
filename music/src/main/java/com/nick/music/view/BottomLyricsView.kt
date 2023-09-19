package com.nick.music.view

import android.content.Context
import android.graphics.Canvas
import android.text.TextUtils
import android.util.AttributeSet

class BottomLyricsView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0):KrcLineView(context, attributeSet, defStyleAttr) {

    private var mViewWith = 0f

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mViewWith = right.toFloat()
    }

    override fun doDraw(canvas: Canvas) {
        drawSingLyrics2(canvas)
    }

    override fun drawPreView(canvas: Canvas) {
        measurePreViewLyrics()
        canvas.drawText(mOriginLineLyrics, mViewWith - mMeasureRect.right,mOriginStartPositionY, mOriginWordsPaint)
        drawPreViewSubsidiaryWords(canvas)
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

    private fun drawSingLyrics2(canvas: Canvas){
        measureLyrics()
        // 绘制歌词
        canvas.drawText(mOriginLineLyrics, mViewWith - mMeasureRect.right, mOriginStartPositionY, mOriginWordsPaint)
        drawSubsidiaryLyrics(canvas)
        canvas.save()
        canvas.clipRect(mWordsSingRect)
        canvas.drawText(mOriginLineLyrics,mViewWith - mMeasureRect.right,mOriginStartPositionY,mWordsSingPaint)
        canvas.restore()
    }

    private fun drawSubsidiaryLyrics(canvas: Canvas){
        if (mLineLyricsList.isEmpty()){
            return
        }
        if (TextUtils.isEmpty(mOriginLineLyrics)){
            return
        }

        var currentX = mViewWith - mMeasureRect.right
        val krcLineWord = mLineLyricsList[mCurrentLineIndex]
        krcLineWord.originArray.forEachIndexed { index, originChar ->
            val transliteration = krcLineWord.transliterationArray[index]
            // 计算注音的宽度
            val transliterationWidth = mSubsidiaryWordsPaint.measureText(transliteration)
            // 绘制拼音，居中显示在汉字的正上方
            canvas.drawText(
                transliteration,
                currentX + (mOriginWordsPaint.measureText(originChar) - transliterationWidth) / 2,
                mSubsidiaryStartPositionY,
                mSubsidiaryWordsPaint
            )
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
        var currentX = mViewWith - mMeasureRect.right
        val krcLineWord = mLineLyricsList[mPreViewLineIndex]
//        LogUtils.i("顶部画预览歌词>>>${krcLineWord.origin}")
        krcLineWord.originArray.forEachIndexed { index, originChar ->
            val transliteration = krcLineWord.transliterationArray[index]
            // 计算注音的宽度
            val transliterationWidth = mSubsidiaryWordsPaint.measureText(transliteration)
            // 绘制拼音，居中显示在汉字的正上方
            canvas.drawText(
                transliteration,
                currentX + (mOriginWordsPaint.measureText(originChar) - transliterationWidth) / 2,
                mSubsidiaryStartPositionY,
                mSubsidiaryWordsPaint
            )
            // 移动 X 坐标，为下一个汉字和拼音腾出空间
            currentX += mOriginWordsPaint.measureText(originChar)
        }
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