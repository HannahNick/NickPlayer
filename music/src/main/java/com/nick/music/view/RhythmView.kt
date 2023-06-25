package com.nick.music.view

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R
import com.nick.music.model.LyricsInfo
import com.nick.music.model.LyricsTag
import kotlin.collections.ArrayList

/**
 * 节奏view
 * 原理是加载歌词文件，把获取到的歌词持续时长换算成显示长度，然后通过mValueAnimator从零到一完成数值变化，其实就是0-100%的变化
 *

 *
 *
 */
class RhythmView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0): View(context,attributeSet,defStyleAttr) {
    /**
     * 节奏画笔
     */
    private val mRhythmPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 字画笔
     */
    private val mWordsPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 歌唱位置画笔
     */
    private val mSingPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 播放数值发生器，是播放移动的核心0~100%变化
     */
    private val mValueAnimator = ValueAnimator.ofFloat(0f,1f)

    /**
     * 每个字的持续时长
     */
    private val mRhythmDurationList = ArrayList<Long>()

    /**
     * 每个字的绝对开始时间
     */
    private val mRhythmStartTimeList = ArrayList<Long>()

    /**
     * 每个字的显示位置下标
     */
    private val mRhythmWordIndexList = ArrayList<Int>()

    /**
     * 每个字的内容
     */
    private val mRhythmWordList = ArrayList<String>()

    /**
     * 目前移动的距离
     */
    private var mMoveWidth = 0f

    /**
     * 整个view需要绘制多长时间的节奏单位 毫秒
     */
    private val mShowRhythmTime = 5000L

    /**
     * 控件宽度
     */
    private var mViewWidth = 1920f

    /**
     * 控件高度
     */
    private var mViewHeight = 100f

    /**
     * 歌唱位置竖线
     */
    private var mSingLine = mViewWidth/3

    /**
     * 节奏高度，
     */
    private var mLineHeight = 10f

    /**
     * 节奏高度偏移量
     * 节奏是以一个点为垂直中点横向画的，假如从坐标(0,0)画一条宽度为100px，高度为10px的节奏也就是一个矩形，
     *  实际上画的坐标位置是，(0,-5)(0,5)->(100,-5)(100,5)。
     *  但是(0,-5)是超出屏幕的，所以需要这个高度偏移量来调整节奏绘制
     */
    private var mLineHeightOffset = mLineHeight/2

    /**
     * | -_- | -_ -_ --_ _--- -_ |
     * 1     2                   3
     * 1和2是屏幕的宽度
     * 节奏全长的最大宽度 指的是1和3的宽度，由歌曲的时长换算得出
     */
    private var maxRhythmStartX = 0f

    /**
     * 总时长
     */
    var mTotalTime = 0L

    /**
     * 一毫秒的宽度
     */
    private var mOneMileSecondWidth = mViewWidth/mShowRhythmTime

    /**
     * 当前唱的字
     */
    private var mCurrentWords = ""

    /**
     * 数据初始化标志
     */
    private var mDataHasInit = false

    /**
     * 当前播放的歌曲名
     */
    var mTitle = ""

    /**
     * 当前播放的歌手
     */
    var mActor = ""

    init {
        mValueAnimator.apply {
                interpolator = LinearInterpolator()
                addUpdateListener {
                    val animVal = it.animatedValue as Float
                    mMoveWidth = maxRhythmStartX * animVal
//                    LogUtils.i("movewidth: $mMoveWidth")
                    invalidate()
                }
                doOnEnd {
                    LogUtils.i("节奏结束 duration: $duration")
                }
                setEvaluator(object : TypeEvaluator<Float>{
                    override fun evaluate(
                        fraction: Float,
                        startValue: Float,
                        endValue: Float
                    ): Float {
                        return fraction
                    }
                })
            }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
//        LogUtils.i("onLayout left:$left,top: $top,right: $right,bottom: $bottom")
        mViewWidth = right.toFloat()
        mViewHeight = (bottom - top).toFloat()
        mLineHeight = mViewHeight/10
        mLineHeightOffset = mLineHeight/2
        mSingLine = mViewWidth/3
        mOneMileSecondWidth = mViewWidth/mShowRhythmTime
        LogUtils.i("mOneMileSecondWidth: $mOneMileSecondWidth")
        mRhythmPaint.apply {
            style = Paint.Style.STROKE
//            strokeCap = Paint.Cap.ROUND
            strokeWidth = mLineHeight
            color = context.resources.getColor(R.color.gray,null)
        }
        mWordsPaint.apply {
            textSize = 20f
            color = context.resources.getColor(R.color.black,null)
        }

        mSingPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = context.resources.getColor(R.color.black,null)
        }

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mDataHasInit){
            mRhythmDurationList.forEachIndexed { index, l ->
                drawRhythm(canvas,l,mRhythmWordIndexList[index],mRhythmStartTimeList[index],mRhythmWordList[index])
            }
        }
        drawSingLine(canvas)
    }

    /**
     * 画节奏
     * time: 字的时长毫秒为单位，也是每个节奏的宽度，view的可视范围内显示5秒的节奏,假设view的宽度是1920px 那么其一毫秒的节奏长度是1920/5000
     * 假设每个字的时长为i,那么换算其节奏的长度就是i*1920/5000
     * lineIndex: 表示节奏在垂直方向的序号，默认划分是10级，从上到下，
     * startTime:节奏开始时间，要绝对时间。
     *
     */
    private fun drawRhythm(canvas: Canvas, time: Long, lineIndex: Int, startTime: Long,words: String){
        val rhythmStartX = mSingLine + timeToWidth(startTime) - mMoveWidth
//        LogUtils.i("mSingLine: $mSingLine, startTime: $startTime, timeToWidth: ${timeToWidth(startTime)}, mMoveWidth: $mMoveWidth")
        val rhythmStopX = rhythmStartX + timeToWidth(time)
        //结束点在屏幕左边，开始点在屏幕右边
        if (rhythmStopX<0 || rhythmStartX > mViewWidth){
            return
        }
        val rhythmStartY = mLineHeight*lineIndex + mLineHeightOffset
        //当前唱的字
        if (rhythmStartX<mSingLine && rhythmStopX>mSingLine && mCurrentWords !=words){
//            LogUtils.i(words)
            mCurrentWords = words
        }
//        LogUtils.i("rhythmStartX :$rhythmStartX, lineStartY: $lineStartY, rhythmStopX: $rhythmStopX, lineStopY: $lineStartY")
        canvas.drawLine(rhythmStartX,rhythmStartY, rhythmStopX,rhythmStartY,mRhythmPaint)
        canvas.drawText(words,rhythmStartX,rhythmStartY+mLineHeightOffset,mWordsPaint)
    }

    private fun drawSingLine(canvas: Canvas){
        canvas.drawLine(mSingLine,0f,mSingLine,mViewHeight,mSingPaint)
    }

    private fun timeToWidth(time: Long): Float{
        return mOneMileSecondWidth*time
    }

    fun setData(lyricsInfo: LyricsInfo, totalTime: Long){
        val title = lyricsInfo.lyricsTags[LyricsTag.TAG_TITLE] as String
        val actor = lyricsInfo.lyricsTags[LyricsTag.TAG_ARTIST] as String
        if (mTitle == title && mActor == actor){
            LogUtils.e("data Has set title: $mTitle, actor: $mActor")
            return
        }

        val lineInfoMap = lyricsInfo.lyricsLineInfoTreeMap
        val size = lineInfoMap.size
        if (size==0){
            LogUtils.e("lyricsInfo is empty")
            return
        }
        clearData()
        lineInfoMap.forEach {
            val duration = it.value.wordsDisInterval
            val startTime = it.value.wordsStartTime
            val wordsIndex = it.value.wordsIndex
            val wordsList = it.value.lyricsWords
            mRhythmDurationList.addAll(duration.toTypedArray())
            mRhythmStartTimeList.addAll(startTime.toTypedArray())
            mRhythmWordIndexList.addAll(wordsIndex.toTypedArray())
            mRhythmWordList.addAll(wordsList)
        }
        mTotalTime = totalTime
        mValueAnimator.cancel()
        mValueAnimator.setFloatValues(0f,1f)
        mValueAnimator.duration = mTotalTime
        LogUtils.i("totalTime: $mTotalTime")
        maxRhythmStartX = timeToWidth(mTotalTime)
        mMoveWidth = 0f
        mDataHasInit = true
        mTitle = title
        mActor = actor
        invalidate()

    }

    private fun clearData(){
        mRhythmDurationList.clear()
        mRhythmStartTimeList.clear()
        mRhythmWordIndexList.clear()
        mRhythmWordList.clear()
    }

    /**
     * 暂停
     */
    fun pause(){
        mValueAnimator.pause()
        LogUtils.i("节奏暂停 position:${mValueAnimator.currentPlayTime}")
    }

    /**
     * 从头开始
     */
    fun start(){
        if (mValueAnimator.isRunning){
            return
        }

        LogUtils.i("节奏开始")
        mValueAnimator.start()
    }

    /**
     * 继续
     */
    fun resume(){
        if (mValueAnimator.isPaused){
            mValueAnimator.resume()
            LogUtils.i("节奏继续 position:${mValueAnimator.currentPlayTime}")
        }
    }

    /**
     * 跳播
     */
    fun seek(position: Long){
        if (mValueAnimator.isRunning){
            LogUtils.i("seek: $position")
            mValueAnimator.currentPlayTime = position
            return
        }
        val positionF = position.toFloat()
        val totalF = mTotalTime.toFloat()
        val presentF = positionF/totalF

        mValueAnimator.setCurrentFraction(presentF)
        mMoveWidth = maxRhythmStartX * presentF
        invalidate()
    }

    fun previewPosition(position: Long){
        LogUtils.i("previewPosition: $position ")
        mValueAnimator.currentPlayTime = position
    }

    fun release(){
        clearData()
        mValueAnimator.cancel()
    }
}