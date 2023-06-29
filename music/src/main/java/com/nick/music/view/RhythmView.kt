package com.nick.music.view

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R
import com.nick.music.model.LyricsInfo
import com.nick.music.model.LyricsTag
import java.util.concurrent.atomic.AtomicInteger
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
     * 唱准的节奏画笔
     */
    private val mSingRhythmPaint = Paint(Paint.ANTI_ALIAS_FLAG)

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
     * 歌词数据
     */
    private val mRhythmList = ArrayList<Rhythm>()

    /**
     * 目前移动的距离
     */
    private var mMoveWidth = 0f

    /**
     * 整个view需要绘制多长时间的节奏单位 毫秒
     */
    private val mShowRhythmTime = 3000L

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
     * 线帽半径
     */
    private var mCapWidth = mLineHeight/2

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
     * 当前数据位置
     */
    private var mCurrentDataIndex = 0
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

    /**
     * 歌手唱歌标志位
     */
    private var mSingNow = false

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
//        mCapWidth = mLineHeight/2
        mCapWidth = 0f
        mOneMileSecondWidth = mViewWidth/mShowRhythmTime
//        LogUtils.i("mOneMileSecondWidth: $mOneMileSecondWidth")
        mRhythmPaint.apply {
            strokeCap = Paint.Cap.BUTT
            strokeWidth = mLineHeight
            color = context.resources.getColor(R.color.gray,null)
        }
        mSingRhythmPaint.apply {
            strokeCap = Paint.Cap.BUTT
            strokeWidth = mLineHeight
            color = context.resources.getColor(R.color.sing_rhythm,null)
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
            mRhythmList.forEachIndexed { index, l ->
                drawRhythm(canvas,l.duration,l.wordIndex,l.startTime,l.word,index)
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
    private fun drawRhythm(canvas: Canvas, time: Long, lineIndex: Int, startTime: Long,words: String,dataIndex: Int){
        //节奏开始位置: 歌唱竖线位置+绝对时间位置 - 位置偏移量
        val rhythmStartX = mSingLine + timeToWidth(startTime) - mMoveWidth
//        LogUtils.i("mSingLine: $mSingLine, startTime: $startTime, timeToWidth: ${timeToWidth(startTime)}, mMoveWidth: $mMoveWidth")
        //节奏结束位置: 开始位置+歌词持续时间位置
        val rhythmStopX = rhythmStartX + timeToWidth(time)
        //结束点在屏幕左边，开始点在屏幕右边
        if (rhythmStopX<0 || rhythmStartX > mViewWidth){
            return
        }

        val rhythmStartY = mLineHeight*lineIndex + mLineHeightOffset
        //当前唱的字
        if (rhythmStartX<=mSingLine && rhythmStopX>mSingLine && mCurrentWords !=words){
            mCurrentDataIndex = dataIndex
            mCurrentWords = words
//            LogUtils.i("words:$words , 数据下标:$dataIndex")
        }
//        LogUtils.i("rhythmStartX :$rhythmStartX, lineStartY: $lineStartY, rhythmStopX: $rhythmStopX, lineStopY: $lineStartY")

        canvas.drawLine(rhythmStartX+mCapWidth,rhythmStartY, rhythmStopX-mCapWidth,rhythmStartY,mRhythmPaint)
//        drawSingRhythm(canvas, rhythmStartX, rhythmStopX, rhythmStartY)
        drawSingRhythm2(canvas, rhythmStartX, rhythmStopX, rhythmStartY,lineIndex,dataIndex)
        drawHaveSingRhythm(canvas,dataIndex)
        canvas.drawText(words,rhythmStartX,rhythmStartY,mWordsPaint)
        if (rhythmStopX < mSingLine){
            mRhythmList[dataIndex].wordHaveSing = true
        }
    }

    /**
     * 画完美唱准的节奏
     */
    private fun drawSingRhythm(canvas: Canvas,rhythmStartX: Float,rhythmStopX: Float,rhythmStartY: Float){
        //过了唱歌线就覆盖节奏
        val startX = rhythmStartX + mCapWidth
        val stopX = rhythmStopX - mCapWidth
        val singStopX = mSingLine - mCapWidth
        //如果没有这个条件，在最开始的绘制位置会出现 stop比start要大
        val rhythmMinWidth = singStopX - startX
        if (stopX<singStopX){
            canvas.drawLine(startX,rhythmStartY, stopX,rhythmStartY,mSingRhythmPaint)
        }else if (rhythmMinWidth >= 0 && startX <= mSingLine && stopX >singStopX ){
            canvas.drawLine(startX,rhythmStartY, singStopX,rhythmStartY,mSingRhythmPaint)
        }
    }


    private fun drawSingRhythm2(canvas: Canvas, rhythmStartX: Float, rhythmStopX: Float, rhythmStartY: Float, lineIndex: Int,dataIndex: Int){
        //原节奏开始位置
        val startX = rhythmStartX + mCapWidth
        //原节奏结束位置
        val stopX = rhythmStopX - mCapWidth
        //黑线
        val singStopX = mSingLine - mCapWidth
        //用户歌唱位置
        val singStartX = mSingLine + timeToWidth(mStartRecordTime) - mMoveWidth
        val rhythmMinWidth = singStopX - startX
        if (stopX<singStopX){
//            LogUtils.i("数据:$dataIndex 结束了")
            sing(false,dataIndex)
        }else if (rhythmMinWidth >= 0 && startX <= mSingLine && stopX >singStopX){//节奏宽度要大于0，开始唱的位置在黑线左边，原节奏结束的位置要在黑线右边
            if (mSingNow && singStopX>(singStartX+ mCapWidth)){
                canvas.drawLine(singStartX + mCapWidth,rhythmStartY, singStopX,rhythmStartY ,mSingRhythmPaint)
//                LogUtils.i("画数据下标:$dataIndex")
            }
        }
    }

    private fun drawHaveSingRhythm(canvas: Canvas,dataIndex: Int){
        mSingerRhythmData[dataIndex]?.forEach {
            val rhythmStartX = mSingLine + timeToWidth(it.startTime) - mMoveWidth
            val rhythmStopX = rhythmStartX + timeToWidth(it.time)
            if (rhythmStopX<0){
                return@forEach
            }
            val rhythmStartY = mLineHeight*it.lineIndex + mLineHeightOffset
            canvas.drawLine(rhythmStartX+mCapWidth,rhythmStartY, rhythmStopX-mCapWidth,rhythmStartY,mSingRhythmPaint)
        }
    }

    /**
     * 唱歌竖线
     */
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
            duration.forEachIndexed { index, l ->
                mRhythmList.add(Rhythm(l,startTime[index],wordsIndex[index],wordsList[index]))
            }

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
        mRhythmList.clear()
        mSingerRhythmData.clear()
        mSingTime = 0L
        mStartRecordTime = 0L
        mCurrentDataIndex = 0
        mCurrentWords = ""
        mSingNow = false
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

    fun sing(sing: Boolean,dataIndex: Int = mCurrentDataIndex){
        if (mSingNow == sing){
            return
        }
        //如果原节奏已经唱完了，就不往下走了
        if (mRhythmList[dataIndex].wordHaveSing){
            return
        }

        if (sing){
//            LogUtils.i("开始唱")
            mStartRecordTime = mValueAnimator.currentPlayTime
        }else{
//            LogUtils.i("不唱了")
            mSingTime = mValueAnimator.currentPlayTime - mStartRecordTime
            var singerRhythmList = mSingerRhythmData[dataIndex]
            if (singerRhythmList ==null){
                singerRhythmList = ArrayList()
            }
            singerRhythmList.add(SingerRhythm(mStartRecordTime,mSingTime,mRhythmList[dataIndex].wordIndex))
            mSingerRhythmData[dataIndex] = singerRhythmList
        }
        mSingNow = sing
    }

    /**
     * 歌手已唱节奏数据
     */
    data class SingerRhythm(
        var startTime: Long,
        var time: Long,
        var lineIndex: Int)

    private var mSingTime = 0L
    private var mStartRecordTime = 0L
    private var mSingerRhythmData = SparseArray<ArrayList<SingerRhythm>>()

    data class Rhythm(

        /**
         * 每个字的持续时长
         */
        var duration: Long,

        /**
         * 每个字的绝对开始时间
         */
        var startTime: Long,

        /**
         * 每个字的显示位置下标
         */
        var wordIndex:Int,

        /**
         * 每个字的内容
         */
        var word:String,

        /**
         * 词已唱完
         */
        var wordHaveSing: Boolean = false,
    )
}