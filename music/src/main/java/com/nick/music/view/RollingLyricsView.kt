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

    /**
     * 已唱的y轴位置
     */
    var yPosition = height/2 - yOffset


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        mOriginWordsPaint.apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = (bottom-top)/5f
            color = context.resources.getColor(R.color.white,null)
            letterSpacing = 0.1f//设置文本间距
            textAlign = Paint.Align.LEFT
        }

        mWordsSingPaint.apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = (bottom-top)/5f
            color = context.resources.getColor(R.color.male_voice,null)
            letterSpacing = 0.1f
            textAlign = Paint.Align.LEFT
        }

        mLinePaint.apply {
            color = context.resources.getColor(R.color.black,null)
        }
    }

//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        if (mLineLyricsList.isEmpty()) return
//
//        canvas.drawLine(0f,height/2f,width.toFloat(),height/2f,mLinePaint)
//        for (i in 0 until mLineLyricsList.size){
//            val lyric = mLineLyricsList[i].origin
//            //当前要唱的行歌词
//            if (mCurrentLineIndex == i){
//                drawLyricText(canvas,lyric,width,mWordsSingPaint)
//                continue
//            }
//            drawLyricText(canvas,lyric,width,mOriginWordsPaint)
//        }
//    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mLineLyricsList.isEmpty()) return

        canvas.drawLine(0f,height/2f,width.toFloat(),height/2f,mLinePaint)
        for (i in 0 until mLineLyricsList.size){
            val lyric = mLineLyricsList[i].origin
            val y = (height / 2) + (i - mCurrentLineIndex) * mWordsSingPaint.textSize * 2 // 计算每行歌词的基线位置
            //当前要唱的行歌词
            if (mCurrentLineIndex == i){
                drawLyricText(canvas,lyric,width,y,mWordsSingPaint)
                continue
            }
            drawLyricText(canvas,lyric,width,y,mOriginWordsPaint)
        }
    }

    private fun drawLyricText(canvas: Canvas, text: String, width: Int, yPos: Float,paint: Paint) {
        val textPaint = TextPaint(paint) // 使用现有的 Paint 设置创建 TextPaint
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()

        canvas.save()
        // 计算居中偏移
        val textHeight = staticLayout.height
        canvas.translate(0f, yPos - textHeight / 2)  // 调整canvas位置到文本绘制起点
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawLyricText(canvas: Canvas, text: String, width: Int,paint: Paint) {
        val textPaint = TextPaint(paint) // 使用现有的 Paint 设置创建 TextPaint
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()

        canvas.save()
        // 计算居中偏移
        canvas.translate(0f, yPosition)  // 调整canvas位置到文本绘制起点
        staticLayout.draw(canvas)
        canvas.restore()
        yPosition = yPosition + staticLayout.height - yOffset
    }

    fun setData(lyricsInfo: LyricsInfo){
        val topTranslateLrcLineInfo = lyricsInfo.translateLrcLineInfos?:ArrayList()
        val topTransliterationLrcLineInfo = lyricsInfo.transliterationLrcLineInfos?:ArrayList()
        val topOriginal = lyricsInfo.lyricsLineInfoTreeMap
        lyricsInfo.initColor()
        wrapData(topOriginal,topTranslateLrcLineInfo,topTransliterationLrcLineInfo)
    }

    private fun smoothScrollToNext() {
        isRolling = true

        val textPaint = TextPaint(mOriginWordsPaint)
        val text = mLineLyricsList[mCurrentLineIndex].origin
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()

        val animator = ValueAnimator.ofFloat( yOffset,yOffset+staticLayout.height.toFloat())
        animator.addUpdateListener { animation ->
            yOffset = animation.animatedValue as Float
            L.i("yOffset: $yOffset")
            invalidate()
        }
        animator.duration = 300  // 动画持续时间，可根据需要调整
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 动画结束时更新当前歌词索引，重置 yOffset
                isRolling = false
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
        invalidate()
    }

    fun release(){
        mRhythmList.clear()
        mLineLyricsList.clear()
        mCurrentPlayDataIndex= 0
    }
}