package com.nick.music.view

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R
import com.nick.music.callback.PositionInitFinishListener
import com.nick.music.model.KrcLineWord
import com.nick.music.model.LyricsLineInfo
import com.nick.music.model.TranslateLrcLineInfo
import java.util.*

abstract class KrcLineView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0): View(context,attributeSet,defStyleAttr) {
    /**
     * 原音歌词画笔
     */
    protected val mOriginWordsPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 辅助歌词画笔(注音或翻译)
     */
    protected val mSubsidiaryWordsPaint = Paint(Paint.ANTI_ALIAS_FLAG)

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
    protected val mLineLyricsList = ArrayList<KrcLineWord>()

    /**
     * 当前唱的这行歌词在mLineLyricsList的下标
     */
    protected var mCurrentLineIndex: Int = 0

    /**
     * 预览歌词mLineLyricsList的下标
     */
    protected var mPreViewLineIndex: Int = 0

    /**
     * 当前唱的字在rhythmList的下标
     */
    protected var mCurrentPlayDataIndex: Int = 0

    /**
     * 当前唱的字
     */
    protected var mCurrentOriginalWord: String = ""

    /**
     * 当前原音歌词，一行
     */
    protected var mOriginLineLyrics: String = ""

    /**
     * 当前注音或者翻译歌词，一行
     */
    protected var mSubsidiaryLineLyrics: String = ""

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

    /**
     * 歌词在x轴上的显示位置
     */
    protected var mPaintStartPosition: Float = 0f

    /**
     * 辅助歌词在y轴上的显示位置
     */
    protected var mSubsidiaryStartPositionY: Float = 0f

    /**
     * 原音歌词在y轴上的显示位置
     */
    protected var mOriginStartPositionY: Float = 0f

    /**
     * 是否是预览歌词
     */
    private var isDrawPreview = false

    /**
     * 是否补全已唱
     */
    private var isDrawSingFinish = false
    /**
     * 当前行歌词是否已经开始唱了
     */
    private var mCurrentLineIsStart = false


    private val mHandler = Handler(Looper.getMainLooper())

    var positionInitFinishListener: PositionInitFinishListener? = null

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mOriginWordsPaint.apply {
            textSize = (bottom-top)*3/4.toFloat()-7
            color = context.resources.getColor(R.color.white,null)
        }
        mWordsSingPaint.apply {
            textSize = (bottom-top)*3/4.toFloat()-7
            color = context.resources.getColor(R.color.male_voice,null)
        }
        mSubsidiaryWordsPaint.apply {
            textSize = (bottom-top)/4.toFloat()
            color = context.resources.getColor(R.color.white,null)
        }
        mSubsidiaryStartPositionY = (bottom-top)/4.toFloat()
        mOriginStartPositionY = (bottom-top).toFloat() - 7f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isDrawPreview){
            drawPreView(canvas)
        }else if (isDrawSingFinish){
            drawSingFinish(canvas)
        } else{
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

    /**
     * 补全已唱歌词
     */
    abstract fun drawSingFinish(canvas: Canvas)

    abstract fun isTopLyrics(): Boolean

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
            if (rhythm.originalWord.isNotEmpty()){
                wordsLength+=rhythm.originalWord.length
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
            if (rhythm.originalWord.isNotEmpty()){
                wordsLength+=rhythm.originalWord.length
//                LogUtils.i("已唱:${rhythm.word},长度:${rhythm.word.length},wordsLength:${wordsLength}")
                currentIndex--
            }
        }
//        LogUtils.i("测量字符串:$mLineLyrics,长度:${mLineLyrics.length},当前字:${mCurrentWord},下标${mCurrentWordIndex},已唱长度${wordsLength}")
        return wordsLength
    }


    open fun setCurrentPosition(position: Long){
        if (mRhythmList.isEmpty()){
            return
        }
        val rhythm = findCurrentLyrics(position)
        if (rhythm==null){
            //已经唱完最后一个字了
            if (mCurrentPlayDataIndex == mRhythmList.size-1){
                mHandler.postDelayed({
                    mCurrentOriginalWord = ""
                    mOriginLineLyrics = ""
                    mSubsidiaryLineLyrics = ""
                    invalidate()
                },2000)
            }
            //这行已经唱完了,补全绘制
            if (mCurrentLineIsStart){
                isDrawSingFinish = true
                invalidate()
            }
            return
        }
        mHandler.removeCallbacksAndMessages(null)
        mWordsSingPaint.color = context.resources.getColor(rhythm.wordsColor,null)
        mCurrentOriginalWord = rhythm.originalWord

        mOriginLineLyrics = rhythm.lineLyrics
        mCurrentWordIndex = rhythm.wordInLineIndex
        mCurrentWordStartTime = rhythm.startTime
        mCurrentWordDuration = rhythm.duration
        mCurrentPlayPosition = position
        isDrawPreview = false
        mCurrentLineIsStart = true
        isDrawSingFinish = false
        LogUtils.i("${if(isTopLyrics()) "顶部" else "底部"}已找到需要展示的歌词:${mOriginLineLyrics},lineLyricsDataIndex:${rhythm.lineLyricsDataIndex}")
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
                mCurrentLineIndex = data.lineLyricsDataIndex
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

            mLineLyricsList.add(KrcLineWord(origin = it.value.lineLyrics))
            duration.forEachIndexed { index, l ->
                val lastWord = duration.size == index+1
                mRhythmList.add(RhythmView.Rhythm(
                    duration = l,
                    startTime = startTime[index],
                    wordIndex = wordsIndex[index],
                    originalWord = wordsList[index],
                    lineLyrics = lineLyrics,
                    isLastWord = lastWord,
                    wordInLineIndex = index,
                    lineLyricsDataIndex = mLineLyricsList.size-1,
                    wordsColor = wordColor[index]
                    )
                )
            }
        }
//        LogUtils.i("${if(isTopLyrics()) "顶部" else "底部"}行歌词设置完毕 $mLineLyricsList")
//        LogUtils.i("${if(isTopLyrics()) "顶部" else "底部"}歌词设置完毕 $mRhythmList")
    }

    /**
     * map:原音歌词
     * translateLrcLineInfo: 翻译歌词
     * transliterationLrcLineInfo: 音译歌词
     */
    fun setData(map: SortedMap<Int, LyricsLineInfo>,translateLrcLineInfo: List<TranslateLrcLineInfo>,transliterationLrcLineInfo :List<LyricsLineInfo>){
        if (map.isEmpty()){
            LogUtils.e("lyricsInfo is empty")
            return
        }

        release()
        var tempIndex = 0
        map.forEach {
            val duration = it.value.wordsDisInterval
            val startTime = it.value.wordsStartTime
            val wordsIndex = it.value.wordsIndex
            val wordsList = it.value.lyricsWords
            val lineLyrics = it.value.lineLyrics
            val wordColor = it.value.wordColors
            val krcLineWord = KrcLineWord(origin = it.value.lineLyrics)
            krcLineWord.translate = if (translateLrcLineInfo.isNotEmpty()){
                translateLrcLineInfo[tempIndex].lineLyrics
            }else{
                ""
            }
            krcLineWord.transliteration = if (transliterationLrcLineInfo.isNotEmpty()){
                transliterationLrcLineInfo[tempIndex].lineLyrics
            }else{
                ""
            }
            mLineLyricsList.add(krcLineWord)

            duration.forEachIndexed { index, l ->
                val lastWord = duration.size == index+1
                val rhythm = RhythmView.Rhythm(
                        duration = l,
                        startTime = startTime[index],
                        wordIndex = wordsIndex[index],
                        originalWord = wordsList[index],
                        lineLyrics = lineLyrics,
                        isLastWord = lastWord,
                        wordInLineIndex = index,
                        lineLyricsDataIndex = mLineLyricsList.size-1,
                        wordsColor = wordColor[index]
                )
                krcLineWord.originArray.add(wordsList[index])
                if (transliterationLrcLineInfo.isNotEmpty()){
                    val transliterationWord = transliterationLrcLineInfo[tempIndex].lyricsWords[index]
                    rhythm.transliterationWord = transliterationWord
                    krcLineWord.transliterationArray.add(transliterationWord)
                }
                mRhythmList.add(rhythm)
            }

            tempIndex++

        }
    }

    fun release(){
        mRhythmList.clear()
        mLineLyricsList.clear()
        mCurrentOriginalWord = ""
        mOriginLineLyrics = ""
        mCurrentWordIndex= 0
        mCurrentPlayPosition = 0
        mCurrentWordStartTime= 0
        mCurrentWordDuration = 0
        mCurrentPlayDataIndex= 0
        mSubsidiaryLineLyrics = ""
        isDrawPreview = false
        isDrawSingFinish = false
        mCurrentLineIsStart = false
    }

    fun drawNext(index: Int){
        if (mLineLyricsList.size-1<index){
            mOriginLineLyrics = ""
            mSubsidiaryLineLyrics = ""
        }else{
            val krcLineWord = mLineLyricsList[index]
            mOriginLineLyrics = krcLineWord.origin
            mSubsidiaryLineLyrics = if (!TextUtils.isEmpty(krcLineWord.transliteration)){//优先展示音译歌词
                krcLineWord.transliteration
            }else{
                krcLineWord.translate
            }
            mPreViewLineIndex = index
//            LogUtils.i("${if(isTopLyrics()) "顶部" else "底部"}收到: 下标:$index, 当前需要预展示的歌词:$mOriginLineLyrics 副歌词:$mSubsidiaryLineLyrics")
        }
        isDrawPreview = true
        mCurrentLineIsStart = false
        isDrawSingFinish = false
        invalidate()
    }
}