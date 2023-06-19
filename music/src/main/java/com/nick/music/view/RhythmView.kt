package com.nick.music.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R
import com.nick.music.model.LyricsInfo
import kotlin.collections.ArrayList

/**
 * 节奏view
 * 原理是加载歌词文件，把获取到的歌词持续时长换算成显示长度，然后通过mValueAnimator从零到一完成数值变化，其实就是0-100%的变化
 *
 */
class RhythmView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0): View(context,attributeSet,defStyleAttr) {

    private val mRhythmPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mWordsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSingPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mLineHeight = 10f
    private val mValueAnimator = ValueAnimator.ofFloat(0f,1f)
    private val rhythmDurationList = ArrayList<Long>()
    private val rhythmStartTimeList = ArrayList<Long>()
    private val rhythmWordIndexList = ArrayList<Int>()
    private val rhythmWordList = ArrayList<String>()
    private var mDataHasInit = false

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
     * 节奏的最大宽度
     */
    private var maxRhythmStartX = 0f

    /**
     * 总时长
     */
    private var totalTime = 0L

    /**
     * 一毫秒的宽度
     */
    private var mOneMileSecondWidth = mViewWidth/mShowRhythmTime


    init {
        mValueAnimator.apply {
                interpolator = LinearInterpolator()
                addUpdateListener {
                    mMoveWidth = maxRhythmStartX * (it.animatedValue as Float)
//                    LogUtils.i("movewidth: $mMoveWidth")
                    invalidate()
                }
                doOnEnd {
                    mValueAnimator.duration = totalTime
                    mValueAnimator.setFloatValues(0f,1f)
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
            rhythmDurationList.forEachIndexed { index, l ->
                drawRhythm(canvas,l,rhythmWordIndexList[index],rhythmStartTimeList[index],rhythmWordList[index])
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
        val rhythmStopX = rhythmStartX + timeToWidth(time)
        //结束点在屏幕左边，开始点在屏幕右边
        if (rhythmStopX<0 || rhythmStartX > mViewWidth){
            return
        }
        val lineStartY = mLineHeight*lineIndex + 5
//        LogUtils.i("lineStartX :$lineStartX, lineStartY: $lineStartY, lineStopX: $lineStopX, lineStopY: $lineStartY")
        canvas.drawLine(rhythmStartX,lineStartY, rhythmStopX,lineStartY,mRhythmPaint)
        canvas.drawText(words,rhythmStartX,lineStartY+5,mWordsPaint)
    }

    private fun drawSingLine(canvas: Canvas){
        canvas.drawLine(mSingLine,0f,mSingLine,mViewHeight,mSingPaint)
    }

    private fun timeToWidth(time: Long): Float{
        return mOneMileSecondWidth*time
    }

    fun setData(lyricsInfo: LyricsInfo){


        val lineInfoMap = lyricsInfo.lyricsLineInfoTreeMap
        val size = lineInfoMap.size
        if (size==0){
            LogUtils.e("lyricsInfo is empty")
            return
        }
        val total = lyricsInfo.lyricsTags["total"]
        if (total!=null && total is String){
            totalTime = total.toLong()
        }else{
            LogUtils.e("total is empty")
            return
        }
        if (mValueAnimator.isStarted){
            LogUtils.i("暂停了播放")
            mValueAnimator.pause()
        }
        clearData()
        lineInfoMap.forEach {
            val duration = it.value.wordsDisInterval
            val startTime = it.value.wordsStartTime
            val wordsIndex = it.value.wordsIndex
            val wordsList = it.value.lyricsWords
            rhythmDurationList.addAll(duration.toTypedArray())
            rhythmStartTimeList.addAll(startTime.toTypedArray())
            rhythmWordIndexList.addAll(wordsIndex.toTypedArray())
            rhythmWordList.addAll(wordsList)
        }

        mValueAnimator.setFloatValues(0f,1f)
        mValueAnimator.duration = totalTime
        LogUtils.i("totalTime: $totalTime")
        maxRhythmStartX = timeToWidth(totalTime)
        mDataHasInit = true
        invalidate()

    }

    private fun clearData(){
        rhythmDurationList.clear()
        rhythmStartTimeList.clear()
        rhythmWordIndexList.clear()
        rhythmWordList.clear()
    }

    /**
     * 暂停
     */
    fun pause(){
        mValueAnimator.pause()
    }

    /**
     * 继续或开始
     */
    fun startDraw(){
        if (mValueAnimator.isPaused){
            mValueAnimator.resume()
            return
        }
        LogUtils.i("开始播放")
        mValueAnimator.start()
    }

    /**
     * 跳播
     */
    fun seek(position: Long){
        val positionF = position.toFloat()
        val totalF = totalTime.toFloat()
        LogUtils.i("跳转播放: $position ,duration: ${totalTime - position},百分数: ${positionF/totalF}")
        mValueAnimator.apply {
            setFloatValues(positionF/totalF,1F)
            duration = totalTime - position
            start()
        }
    }

    fun release(){
        clearData()
        mValueAnimator.cancel()
    }
}