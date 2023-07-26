package com.nick.music.view

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R
import com.nick.music.callback.PositionInitFinishListener
import com.nick.music.krc.KrcLyricsFileReader
import com.nick.music.model.LyricsLineInfo
import java.util.*

abstract class KrcLineView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0): View(context,attributeSet,defStyleAttr) {
    /**
     * 歌词画笔
     */
    protected val mWordsPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 已唱歌词画笔
     */
    protected val mWordsSingPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 歌词长度测量框框
     */
    protected val mMeasureRect = Rect()

    /**
     * 已唱歌词测量框框
     */
    protected val mWordsSingRect = RectF()

    /**
     * 歌词数据
     */
    protected val mRhythmList = ArrayList<RhythmView.Rhythm>()

    /**
     * 行歌词数据
     */
    protected val mLineLyricsList = ArrayList<String>()

    /**
     * 行歌词颜色
     */
    private val mLineLyricsColorList = ArrayList<Int>()

    /**
     * 当前唱的字在rhythmList的下标
     */
    protected var mCurrentPlayDataIndex: Int = 0

    /**
     * 当前唱的字
     */
    protected var mCurrentWord: String = ""

    /**
     * 当前行歌词
     */
    protected var mLineLyrics: String = ""

    /**
     * 当前字在行的下标
     */
    protected var mCurrentWordIndex: Int = 0

    /**
     * 播放器的播放位置
     */
    protected var mCurrentPlayPosition: Long = 0

    /**
     * 当前字的开始时间
     */
    protected var mCurrentWordStartTime: Long = 0

    /**
     * 当前字的时长
     */
    protected var mCurrentWordDuration: Long = 0

    //文本在x轴上的显示位置
    protected var mStartPosition: Float = 0f

    /**
     * 是否是预览歌词
     */
    private var isDrawPreview = false

    private val mHandler = Handler(Looper.getMainLooper())

    var positionInitFinishListener: PositionInitFinishListener? = null

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWordsPaint.apply {
            textSize = (bottom-top)*3/4.toFloat()
            color = context.resources.getColor(R.color.white,null)
            clipBounds
        }
        mWordsSingPaint.apply {
            textSize = (bottom-top)*3/4.toFloat()
            color = context.resources.getColor(R.color.male_voice,null)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isDrawPreview){
            drawPreView(canvas)
        }else{
            doDraw(canvas)
        }
//        measureHaveSingRect()
//        // 绘制歌词
//        canvas.drawText(mLineLyrics, mStartPosition, mWordsPaint.textSize, mWordsPaint)
//        canvas.save()
//        canvas.clipRect(mWordsSingRect)
//        canvas.drawText(mLineLyrics,mStartPosition,mWordsSingPaint.textSize,mWordsSingPaint)
//        canvas.restore()
    }

    /**
     * 子类实现画
     */
    abstract fun doDraw(canvas: Canvas)
    /**
     * 画预览歌词
     */
    abstract fun drawPreView(canvas: Canvas)

    abstract fun isTopLyrics(): Boolean

    protected fun measureHaveSingRect(){
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
        //测量当前在唱的字的宽度
        val currentWordsWidth = mWordsPaint.measureText(mLineLyrics.substring(0 until getWillSingWordsLength())) - haveSingTextWidth
        //描绘已唱部分核心
        mWordsSingRect.right = ((mCurrentPlayPosition - mCurrentWordStartTime)*currentWordsWidth/mCurrentWordDuration) + haveSingTextWidth +mStartPosition
    }

    /**
     * 获取要唱的歌词长度
     */
    protected fun getWillSingWordsLength():Int {
        if (mRhythmList.isEmpty()){
            return 0
        }
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

    /**
     * 获取已唱的歌词长度
     */
    protected fun getHaveSingWordsLength():Int{
        if (mRhythmList.isEmpty()){
            return 0
        }
        var wordsLength = 0

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


    open fun setCurrentPosition(position: Long){
        if (mRhythmList.isEmpty()){
            LogUtils.i("lyricsInfo is empty")
            return
        }
        val rhythm = findCurrentLyrics(position)
        if (rhythm==null){
            //已经唱完最后一个字了
            if (mCurrentPlayDataIndex == mRhythmList.size-1){
                mHandler.postDelayed({
                    mCurrentWord = ""
                    mLineLyrics = ""
                    invalidate()
                },2000)
            }
            return
        }
        mHandler.removeCallbacksAndMessages(null)
        mWordsSingPaint.color = context.resources.getColor(rhythm.wordsColor,null)
        mCurrentWord = rhythm.word
        mLineLyrics = rhythm.lineLyrics
        mCurrentWordIndex = rhythm.wordInLineIndex
        mCurrentWordStartTime = rhythm.startTime
        mCurrentWordDuration = rhythm.duration
        mCurrentPlayPosition = position
        isDrawPreview = false
        LogUtils.i("${if(isTopLyrics()) "顶部" else "底部"}已找到需要展示的歌词:${mLineLyrics},rhythm:$rhythm")
        positionInitFinishListener?.showPreView(rhythm.lineLyricsDataIndex,!isTopLyrics())
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

    fun setData(map: SortedMap<Int, LyricsLineInfo>){
        val size = map.size
        if (size==0){
            LogUtils.e("lyricsInfo is empty")
            return
        }
        release()
        map.forEach {
            val duration = it.value.wordsDisInterval
            val startTime = it.value.wordsStartTime
            val wordsIndex = it.value.wordsIndex
            val wordsList = it.value.lyricsWords
            val lineLyrics = it.value.lineLyrics
            val wordColor = it.value.wordColors
            mLineLyricsList.add(lineLyrics)
            duration.forEachIndexed { index, l ->
                val lastWord = duration.size == index+1
                mRhythmList.add(RhythmView.Rhythm(
                        l,
                        startTime[index],
                        wordsIndex[index],
                        wordsList[index],
                        lineLyrics,
                        lastWord,
                        index,
                        mLineLyricsList.size-1,
                        wordColor[index]
                    )
                )
            }
        }
//        LogUtils.i("${if(isTopLyrics()) "顶部" else "底部"}行歌词设置完毕 $mLineLyricsList")
//        LogUtils.i("${if(isTopLyrics()) "顶部" else "底部"}歌词设置完毕 $mRhythmList")
    }

    private fun release(){
        mRhythmList.clear()
        mLineLyricsList.clear()
        mCurrentWord = ""
        mLineLyrics = ""
        mCurrentWordIndex= 0
        mCurrentPlayPosition = 0
        mCurrentWordStartTime= 0
        mCurrentWordDuration = 0
    }

    fun drawNext(index: Int){
        if (mLineLyricsList.size-1<index){
            mLineLyrics = ""
        }else{
            mLineLyrics = mLineLyricsList[index]
//        LogUtils.i("${if(isTopLyrics()) "顶部" else "底部"}收到: 下标:$index, 当前需要预展示的歌词:$mLineLyrics")
        }
        isDrawPreview = true
        invalidate()

    }
}