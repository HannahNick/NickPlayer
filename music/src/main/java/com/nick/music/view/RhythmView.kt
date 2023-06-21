package com.nick.music.view

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

    private val mRhythmPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mWordsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSingPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mLineHeight = 10f
    private val mValueAnimator = ValueAnimator.ofFloat(0f,1f)
    private val mRhythmDurationList = ArrayList<Long>()
    private val mRhythmStartTimeList = ArrayList<Long>()
    private val mRhythmWordIndexList = ArrayList<Int>()
    private val mRhythmWordList = ArrayList<String>()
    private var mDataHasInit = false
    var mTitle = ""
    var mActor = ""

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
                    mValueAnimator.duration = mTotalTime
                    mValueAnimator.setFloatValues(0f,1f)
                    LogUtils.i("结束并重置 duration: $duration")
                }
            }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
//        LogUtils.i("onLayout left:$left,top: $top,right: $right,bottom: $bottom")
        mViewWidth = right.toFloat()
        mViewHeight = (bottom - top).toFloat()
        mLineHeight = mViewHeight/10
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
        LogUtils.i("mSingLine: $mSingLine, startTime: $startTime, timeToWidth: ${timeToWidth(startTime)}, mMoveWidth: $mMoveWidth")
        val rhythmStopX = rhythmStartX + timeToWidth(time)
        //结束点在屏幕左边，开始点在屏幕右边
        if (rhythmStopX<0 || rhythmStartX > mViewWidth){
            return
        }
        val lineStartY = mLineHeight*lineIndex + 5
        LogUtils.i("rhythmStartX :$rhythmStartX, lineStartY: $lineStartY, rhythmStopX: $rhythmStopX, lineStopY: $lineStartY")
        canvas.drawLine(rhythmStartX,lineStartY, rhythmStopX,lineStartY,mRhythmPaint)
        canvas.drawText(words,rhythmStartX,lineStartY+5,mWordsPaint)
    }

    private fun drawSingLine(canvas: Canvas){
        canvas.drawLine(mSingLine,0f,mSingLine,mViewHeight,mSingPaint)
    }

    private fun timeToWidth(time: Long): Float{
        return mOneMileSecondWidth*time
    }

    fun setData(lyricsInfo: LyricsInfo,duration: Long){
        val title = lyricsInfo.lyricsTags[LyricsTag.TAG_TITLE] as String
        val actor = lyricsInfo.lyricsTags[LyricsTag.TAG_ARTIST] as String
        if (mTitle == title && mActor == actor){
            LogUtils.e("data Has set")
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
        mTotalTime = duration
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
        LogUtils.i("节奏暂停")
        mValueAnimator.pause()
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
            LogUtils.i("节奏继续")
            mValueAnimator.resume()
        }
    }

    /**
     * 跳播
     */
    fun seek(position: Long){
        if (mValueAnimator.isRunning){
            mValueAnimator.currentPlayTime = position
            return
        }
        val positionF = position.toFloat()
        val totalF = mTotalTime.toFloat()
        val presentF = positionF/totalF
        LogUtils.i("seek: $position ,duration: ${mTotalTime - position},百分数: $presentF")
        mValueAnimator.apply {
            setFloatValues(presentF,1F)
            duration = mTotalTime - position
        }
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