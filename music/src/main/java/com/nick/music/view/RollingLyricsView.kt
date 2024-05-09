package com.nick.music.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R
import com.nick.music.entity.Rhythm
import com.nick.music.model.KrcLineWord
import com.nick.music.model.LyricsInfo
import com.nick.music.model.LyricsLineInfo
import com.nick.music.model.TranslateLrcLineInfo
import com.xyz.base.utils.L
import java.util.SortedMap

/**
 * 滚动歌词
 */
class RollingLyricsView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0): View(context,attributeSet,defStyleAttr) {
    /**
     * 原音歌词画笔
     */
    private val mOriginWordsPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 已唱歌词画笔
     */
    private val mWordsSingPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 歌词数据
     */
    private val mRhythmList = ArrayList<Rhythm>()

    /**
     * 行歌词数据
     */
    private val mLineLyricsList = ArrayList<KrcLineWord>()

    /**
     * 当前行歌词
     */
    private var mOriginLineLyrics = ""

    /**
     * 当前唱的字在rhythmList的下标
     */
    private var mCurrentPlayDataIndex: Int = 0
    /**
     * 当前唱的这行歌词在mLineLyricsList的下标
     */
    private var mCurrentLineIndex: Int = 0
        set(value) {
            if (value!=field){
                smoothScrollToNext()
            }
            field = value
        }
    /**
     * 垂直滚动偏移量
     */
    private var mYOffset = 0f
    /**
     * 是否正在执行滚动动画
     */
    private var isRolling = false

    /**
     * 当前行歌词的播放进度，范围从0到1
     */
    private var mCurrentLyricProgress: Float = 0.000f  // 当前行歌词的播放进度，范围从0.0到1.0

    /**
     * 当前播放器播放位置
     */
    private var mCurrentPlayPosition = 0L

    /**
     * 当前字播放开始时间
     */
    private var mCurrentWordStartTime = 0L

    /**
     * 当前字的时长
     */
    private var mCurrentWordDuration = 0L

    /**
     * 当前字所在行的下标
     */
    private var mCurrentWordIndex = 0

    private var mLyricHeights = mutableListOf<Int>()
    private var mYOffsetList = mutableListOf<Float>()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        mOriginWordsPaint.apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = 50f
            color = context.resources.getColor(R.color.white,null)
            letterSpacing = 0.1f//设置文本间距
            textAlign = Paint.Align.LEFT
        }

        mWordsSingPaint.apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = 50f
            color = context.resources.getColor(R.color.male_voice,null)
            letterSpacing = 0.1f
            textAlign = Paint.Align.LEFT
        }

        mLinePaint.apply {
            color = context.resources.getColor(R.color.black,null)
        }
    }

    fun setLyricHeights() {
        mLyricHeights.clear()
        mYOffsetList.clear()
        var lastLyricHeights = 0
        mLineLyricsList.forEach { lyric ->
            val layout = createStaticLayout(lyric.origin, TextPaint(mWordsSingPaint), (width - paddingLeft - paddingRight))
            mLyricHeights.add(layout.height)
            mYOffsetList.add(layout.height/2f + lastLyricHeights)
            lastLyricHeights += layout.height
        }
        mYOffset = mYOffsetList[0]
    }

    private fun createStaticLayout(lyric: String, paint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(lyric, 0, lyric.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var currentY = height / 2f - mYOffset // 初始Y位置为视图中心
        canvas.drawLine(0f,height/2f,width.toFloat(),height/2f,mLinePaint)
        canvas.drawLine(width/2f,0f,width/2f,height.toFloat(),mLinePaint)

        mLineLyricsList.indices.forEach { index ->
            val lyric = mLineLyricsList[index]
            val layout = createStaticLayout(lyric.origin, TextPaint(mOriginWordsPaint), (width - paddingLeft - paddingRight))

            canvas.save()
            canvas.translate(0f, currentY)
            layout.draw(canvas)
            if (index==mCurrentLineIndex){
                val highlightLayout = createStaticLayout(lyric.origin, TextPaint(mWordsSingPaint), (width - paddingLeft - paddingRight))

                val clipWidth = layout.getLineWidth(0) * mCurrentLyricProgress
                L.i("layout.getLineWidth(0):${layout.getLineWidth(0)},mCurrentLyricProgress: $mCurrentLyricProgress")
                canvas.clipRect(0f, 0f, clipWidth, layout.height.toFloat())
                highlightLayout.draw(canvas)
            }

            canvas.restore()
            currentY += mLyricHeights[index]  // 更新Y位置为下一行歌词的起始位置
        }
    }

    private fun clipCanvasForHighlight(canvas: Canvas, highlightWidth: Float) {
        canvas.clipRect(0f, 0f, highlightWidth, mWordsSingPaint.textSize, Region.Op.REPLACE)
    }

    fun setData(lyricsInfo: LyricsInfo){
        val topTranslateLrcLineInfo = lyricsInfo.translateLrcLineInfos?:ArrayList()
        val topTransliterationLrcLineInfo = lyricsInfo.transliterationLrcLineInfos?:ArrayList()
        val topOriginal = lyricsInfo.lyricsLineInfoTreeMap
        lyricsInfo.initColor()
        wrapData(topOriginal,topTranslateLrcLineInfo,topTransliterationLrcLineInfo)
    }

    private fun smoothScrollToNext() {

        if (mCurrentLineIndex >= mLineLyricsList.size - 1) {
            return
        }
        isRolling = true
        val targetYOffset = mYOffsetList[mCurrentLineIndex+1]
        val animator = ValueAnimator.ofFloat(mYOffset, targetYOffset)
        animator.addUpdateListener { animation ->
            mYOffset = animation.animatedValue as Float
            invalidate()
        }
        animator.duration = 300  // 设置动画持续时间
        animator.interpolator = LinearInterpolator()  // 设置动画插值器
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 动画结束后更新当前歌词索引，并重置偏移量
                invalidate()
            }
        })
        animator.start()
    }

    private fun findCurrentLyrics(currentTime: Long): Rhythm? {
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

    private fun updateLyricProgress() {
        //已唱的宽度
        val haveSingTextWidth = if (mCurrentWordIndex==0){
            0f
        }else{
            mOriginWordsPaint.measureText(mOriginLineLyrics.substring(0 until getHaveSingWordsLength()))
        }
        //测量当前在唱的字的宽度
        val currentWordsWidth = mOriginWordsPaint.measureText(mOriginLineLyrics.substring(0 until getWillSingWordsLength())) - haveSingTextWidth
        val originWordsWidth = mOriginWordsPaint.measureText(mOriginLineLyrics)

        //当前唱的字的宽度%+已唱字占这行字宽度的%
        mCurrentLyricProgress = (((mCurrentPlayPosition - mCurrentWordStartTime)/mCurrentWordDuration)*currentWordsWidth + haveSingTextWidth)/ originWordsWidth
        invalidate()
//        L.i("""
//            mCurrentWordIndex: $mCurrentWordIndex
//            mCurrentPlayPosition: $mCurrentPlayPosition
//            mCurrentWordStartTime: $mCurrentWordStartTime
//            mCurrentWordDuration: $mCurrentWordDuration
//            currentWordsWidth: $currentWordsWidth
//            haveSingTextWidth: $haveSingTextWidth
//            originWordsWidth: $originWordsWidth
//
//            mCurrentLyricProgress: $mCurrentLyricProgress
//            --------------------------
//        """.trimIndent())
    }

    /**
     * 获取已唱的歌词长度
     */
    private fun getHaveSingWordsLength():Int{
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

    private fun getWillSingWordsLength():Int {
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

    fun setCurrentPosition(position: Long){
        if (mRhythmList.isEmpty()){
            return
        }
        val rhythm = findCurrentLyrics(position)
        if (rhythm!=null){
            mWordsSingPaint.color = context.resources.getColor(rhythm.wordsColor,null)
            mCurrentWordIndex = rhythm.wordInLineIndex
            mOriginLineLyrics = rhythm.lineLyrics
            mCurrentWordStartTime = rhythm.startTime
            mCurrentWordDuration = rhythm.duration
            mCurrentPlayPosition = position
            updateLyricProgress()
        }
        if (isRolling){
            return
        }
        invalidate()
    }



    /**
     * map:原音歌词
     * translateLrcLineInfo: 翻译歌词
     * transliterationLrcLineInfo: 音译歌词
     */
    private fun wrapData(map: SortedMap<Int, LyricsLineInfo>, translateLrcLineInfo: List<TranslateLrcLineInfo>, transliterationLrcLineInfo :List<LyricsLineInfo>){
        if (map.isEmpty()){
            LogUtils.e("lyricsInfo is empty")
            release()
            invalidate()
            return
        }
        val hasTransLate = translateLrcLineInfo.isNotEmpty() && map.size == translateLrcLineInfo.size
        val hasTransliteration = transliterationLrcLineInfo.isNotEmpty() && map.size == transliterationLrcLineInfo.size

        release()
        var tempIndex = 0
        map.forEach {
            val duration = it.value.wordsDisInterval
            val startTime = it.value.wordsStartTime
            val wordsIndex = it.value.wordsIndex
            val wordsList = it.value.lyricsWords
            val lineLyrics = it.value.lineLyrics
            val wordColor = it.value.wordColors
            val krcLineWord = KrcLineWord(origin = it.value.lineLyrics,hasTransLate = hasTransLate,hasTransliteration = hasTransliteration)
            krcLineWord.translate = if (hasTransLate){
                translateLrcLineInfo[tempIndex].lineLyrics
            }else{
                ""
            }
            krcLineWord.transliteration = if (hasTransliteration){
                transliterationLrcLineInfo[tempIndex].lineLyrics
            }else{
                ""
            }
            mLineLyricsList.add(krcLineWord)

            duration.forEachIndexed { index, l ->
                val lastWord = duration.size == index+1
                val rhythm = Rhythm(
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
                if (hasTransliteration){
                    val transliterationWord = transliterationLrcLineInfo[tempIndex].lyricsWords[index]
                    rhythm.transliterationWord = transliterationWord
                    krcLineWord.transliterationArray.add(transliterationWord)
                }
                mRhythmList.add(rhythm)
            }

            tempIndex++

        }
        setLyricHeights()
        invalidate()
    }

    fun release(){
        mRhythmList.clear()
        mLineLyricsList.clear()
        mCurrentPlayDataIndex= 0
        mYOffset = 0f
        mCurrentWordIndex = 0
        mOriginLineLyrics = ""
        mCurrentWordStartTime = 0L
        mCurrentWordDuration = 0
        mCurrentPlayPosition = 0
        mCurrentLyricProgress = 0.000f
    }
}