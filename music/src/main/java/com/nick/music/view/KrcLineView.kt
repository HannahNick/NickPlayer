package com.nick.music.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R
import com.nick.music.model.LyricsInfo

class KrcLineView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0): View(context,attributeSet,defStyleAttr) {

    private val mWordsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mWordsSingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMeasureRect = Rect()
    private val mWordsSingRect = RectF()

    /**
     * 歌词数据
     */
    private val mRhythmList = ArrayList<RhythmView.Rhythm>()
    private var mCurrentPlayDataIndex: Int = 0

    private var mCurrentWord: String = ""
    private var mLineLyrics: String = ""
    private var mCurrentWordIndex: Int = 0
    private var mCurrentPlayPosition: Long = 0
    private var mCurrentWordStartTime: Long = 0
    private var mCurrentWordDuration: Long = 0

    //显示位置
    private var mStartPosition: Float = 0f

    private val mHandler = Handler(Looper.getMainLooper())

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWordsPaint.apply {
            textSize = 20f
            color = context.resources.getColor(R.color.black,null)
            clipBounds
        }
        mWordsSingPaint.apply {
            textSize = 20f
            color = context.resources.getColor(R.color.sing_rhythm,null)
        }
        mRectPaint.apply {
            color = context.resources.getColor(R.color.sing_rhythm,null)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        mStartPosition = (right/3).toFloat()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制文本
        canvas.drawText(mLineLyrics, mStartPosition, mWordsPaint.textSize, mWordsPaint)
        measureHaveSingRect()
        canvas.save()
        canvas.clipRect(mWordsSingRect)
        canvas.drawText(mLineLyrics,mStartPosition,mWordsSingPaint.textSize,mWordsSingPaint)
        canvas.restore()
    }

    private fun measureHaveSingRect(){
        if (mLineLyrics.isEmpty()){
            return
        }
        // 获取绘制文本的宽度和高度
        mWordsPaint.getTextBounds(mLineLyrics, 0, mLineLyrics.length, mMeasureRect)

        mWordsSingRect.left = mStartPosition
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

    private fun getWillSingWordsLength():Int {
        var wordsLength = 0

        var currentIndex = mCurrentWordIndex
        while (currentIndex>=0){
            val rhythm = mRhythmList[mCurrentPlayDataIndex-currentIndex]
            if (rhythm.word.isNotEmpty()){
                wordsLength+=rhythm.word.length
                currentIndex--
            }
        }
        return wordsLength
    }

    private fun getHaveSingWordsLength():Int{
        var wordsLength = 0

        //取前一个字的下标
        var currentIndex = mCurrentWordIndex
        if (currentIndex == 0){
            return 0
        }
        while (currentIndex>=1){
            val rhythm = mRhythmList[mCurrentPlayDataIndex-currentIndex]
            if (rhythm.word.isNotEmpty()){
                wordsLength+=rhythm.word.length
//                LogUtils.i("已唱:${rhythm.word},长度:${rhythm.word.length},wordsLength:${wordsLength}")
                currentIndex--
            }
        }
//        LogUtils.i("测量字符串:$mLineLyrics,长度:${mLineLyrics.length},当前字:${mCurrentWord},下标${mCurrentWordIndex},已唱长度${wordsLength}")
        return wordsLength
    }


    fun setCurrentPosition(position: Long){
        if (mRhythmList.isEmpty()){
            LogUtils.i("lyricsInfo is empty")
            return
        }
        val rhythm = findCurrentLyrics(position)
        if (rhythm==null){
            mHandler.postDelayed({
                mCurrentWord = ""
                mLineLyrics = ""
                mCurrentWordIndex = 0
                mCurrentWordStartTime = 0
                mCurrentWordDuration = 0
                mCurrentPlayPosition = 0
                invalidate()
            },2000)

            return
        }
        mHandler.removeCallbacksAndMessages(null)
        mCurrentWord = rhythm.word
        mLineLyrics = rhythm.lineLyrics
        mCurrentWordIndex = rhythm.wordInLineIndex
        mCurrentWordStartTime = rhythm.startTime
        mCurrentWordDuration = rhythm.duration
        mCurrentPlayPosition = position
        invalidate()
    }

    private fun findCurrentLyrics(currentTime: Long): RhythmView.Rhythm? {
        var left = 0
        var right = mRhythmList.size - 1

        while (left <= right) {
            val mid = left + (right - left) / 2
            val data = mRhythmList[mid]

            if (currentTime >= data.startTime && currentTime < data.startTime + data.duration) {
                // 当前时间在这个歌词的时间范围内
                mCurrentPlayDataIndex = mid
                return data
            } else if (currentTime < data.startTime) {
                // 当前时间在这个歌词之前
                right = mid - 1
            } else {
                // 当前时间在这个歌词之后
                left = mid + 1
            }
        }
        return null
    }

    fun setData(lyricsInfo: LyricsInfo){
        val lineInfoMap = lyricsInfo.lyricsLineInfoTreeMap

        val size = lineInfoMap.size
        if (size==0){
            LogUtils.e("lyricsInfo is empty")
            return
        }
        release()
        lineInfoMap.forEach {
            val duration = it.value.wordsDisInterval
            val startTime = it.value.wordsStartTime
            val wordsIndex = it.value.wordsIndex
            val wordsList = it.value.lyricsWords
            val lineLyrics = it.value.lineLyrics
            duration.forEachIndexed { index, l ->
                val lastWord = duration.size == index+1
                mRhythmList.add(RhythmView.Rhythm(
                        l,
                        startTime[index],
                        wordsIndex[index],
                        wordsList[index],
                        lineLyrics,
                        lastWord,
                        index
                    )
                )
            }
        }
    }

    private fun release(){
        mRhythmList.clear()
    }
}