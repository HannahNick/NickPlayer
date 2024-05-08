package com.nick.music.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
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
     * 当前唱的字在rhythmList的下标
     */
    protected var mCurrentPlayDataIndex: Int = 0

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
    private var yOffset = 0f
    /**
     * 是否正在执行滚动动画
     */
    private var isRolling = false

    private var lyricHeights = mutableListOf<Int>()

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
        lyricHeights.clear()
        mLineLyricsList.forEach { lyric ->
            val layout = createStaticLayout(lyric.origin, TextPaint(mWordsSingPaint), (width - paddingLeft - paddingRight))
            lyricHeights.add(layout.height)
        }
        yOffset = lyricHeights[0]/2f
    }

    private fun createStaticLayout(lyric: String, paint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(lyric, 0, lyric.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var currentY = height / 2f - yOffset // 初始Y位置为视图中心
        canvas.drawLine(0f,height/2f,width.toFloat(),height/2f,mLinePaint)
        canvas.drawLine(width/2f,0f,width/2f,height.toFloat(),mLinePaint)

        mLineLyricsList.indices.forEach { index ->
            val lyric = mLineLyricsList[index]
            val layout = createStaticLayout(lyric.origin, if (index==mCurrentLineIndex) TextPaint(mWordsSingPaint) else TextPaint(mOriginWordsPaint), (width - paddingLeft - paddingRight))
            canvas.save()
            canvas.translate(0f, currentY)
            layout.draw(canvas)
            canvas.restore()
            currentY += lyricHeights[index]  // 更新Y位置为下一行歌词的起始位置
        }
    }

    fun setData(lyricsInfo: LyricsInfo){
        val topTranslateLrcLineInfo = lyricsInfo.translateLrcLineInfos?:ArrayList()
        val topTransliterationLrcLineInfo = lyricsInfo.transliterationLrcLineInfos?:ArrayList()
        val topOriginal = lyricsInfo.lyricsLineInfoTreeMap
        lyricsInfo.initColor()
        wrapData(topOriginal,topTranslateLrcLineInfo,topTransliterationLrcLineInfo)
    }

    private fun smoothScrollToNext() {

        if (mCurrentLineIndex >= mLineLyricsList.size - 1) return
        isRolling = true

        val currentHeight = lyricHeights[mCurrentLineIndex]
        val nextHeight = lyricHeights[mCurrentLineIndex + 1]
        val scrollAmount = (currentHeight + nextHeight) / 2f // 平均两行高度的一半作为滚动距离
        L.i("scrollAmount: $scrollAmount")

        // 配置并启动动画，动画逻辑类似上一个解决方案
        val animator = ValueAnimator.ofFloat(yOffset, yOffset+scrollAmount)
        animator.addUpdateListener { animation ->
            yOffset = animation.animatedValue as Float
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

    fun setCurrentPosition(position: Long){
        if (mRhythmList.isEmpty()){
            return
        }
        val rhythm = findCurrentLyrics(position)
        if (rhythm!=null){
            mWordsSingPaint.color = context.resources.getColor(rhythm.wordsColor,null)
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
        yOffset = 0f
    }
}