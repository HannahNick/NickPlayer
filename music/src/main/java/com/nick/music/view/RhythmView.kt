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
     * 当前行歌词
     */
    private var mCurrentLineLyrics = ""

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

    /**
     * 当前唱的行歌词回调
     */
    var lyricCallBackListener: LyricCallBackListener? = null

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
                drawRhythm(canvas,l,index)
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
    private fun drawRhythm(canvas: Canvas, rhythm: Rhythm,dataIndex: Int){
        val startTime = rhythm.startTime
        val time = rhythm.duration
        val lineIndex = rhythm.wordIndex
        val words = rhythm.word
        val lineLyrics = rhythm.lineLyrics
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

        if (rhythmStartX<=mSingLine && rhythmStopX>mSingLine){
            if (mCurrentWords !=words){//当前唱的字
                mCurrentDataIndex = dataIndex
                mCurrentWords = words
            }
            if (mCurrentLineLyrics != lineLyrics ){//当前唱的行歌词
                mCurrentLineLyrics = lineLyrics
                lyricCallBackListener?.currentSingLyric(lineLyrics)
//                LogUtils.i("当前唱的行歌词 :$lineLyrics")
            }

//            LogUtils.i("words:$words , 数据下标:$dataIndex")
        }
//        LogUtils.i("rhythmStartX :$rhythmStartX, lineStartY: $lineStartY, rhythmStopX: $rhythmStopX, lineStopY: $lineStartY")

        canvas.drawLine(rhythmStartX+mCapWidth,rhythmStartY, rhythmStopX-mCapWidth,rhythmStartY,mRhythmPaint)
//        drawSingRhythm(canvas, rhythmStartX, rhythmStopX, rhythmStartY)
        drawSingRhythm2(canvas, rhythmStartX, rhythmStopX, rhythmStartY,dataIndex)
        drawHaveSingRhythm(canvas,dataIndex)
        canvas.drawText(words,rhythmStartX,rhythmStartY,mWordsPaint)
        if (rhythmStopX < mSingLine && !rhythm.wordHaveSing){//字歌词已唱完
            rhythm.wordHaveSing = true
            if (rhythm.isLastWord && !rhythm.lineLyricsHaveSing){//行歌词已唱完
                rhythm.lineLyricsHaveSing = true
//                LogUtils.i("当前唱的行歌词 :$lineLyrics 已唱完")
            }
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
        if (stopX<=singStopX){
            canvas.drawLine(startX,rhythmStartY, stopX,rhythmStartY,mSingRhythmPaint)
        }else if (rhythmMinWidth >= 0 && startX <= mSingLine){
            canvas.drawLine(startX,rhythmStartY, singStopX,rhythmStartY,mSingRhythmPaint)
        }
    }

    /**
     * 画用户正在唱的节奏
     */
    private fun drawSingRhythm2(canvas: Canvas, rhythmStartX: Float, rhythmStopX: Float, rhythmStartY: Float,dataIndex: Int){
        //原节奏开始位置
        val startX = rhythmStartX + mCapWidth
        //原节奏结束位置
        val stopX = rhythmStopX - mCapWidth
        //黑线
        val singStopX = mSingLine - mCapWidth
        //用户歌唱位置
        val singStartX = mSingLine + timeToWidth(mStartRecordTime) - mMoveWidth
        val rhythmMinWidth = singStopX - startX
        if (stopX<=singStopX){//唱的字结束了
//            LogUtils.i("数据:$dataIndex 结束了")
            addSingFinishData(dataIndex)
        }else if (rhythmMinWidth >= 0 && startX <= mSingLine){//节奏宽度要大于0，开始唱的位置在黑线左边，原节奏结束的位置要在黑线右边
            if (mSingNow && singStopX>(singStartX+ mCapWidth)){
                canvas.drawLine(singStartX + mCapWidth,rhythmStartY, singStopX,rhythmStartY ,mSingRhythmPaint)
//                LogUtils.i("画数据下标:$dataIndex")
            }
        }
    }

    /**
     * 画用户已唱完的节奏
     */
    private fun drawHaveSingRhythm(canvas: Canvas,dataIndex: Int){
        mSingerRhythmData[dataIndex]?.forEach {
            val rhythmStartX = mSingLine + timeToWidth(it.startTime) - mMoveWidth
            val rhythmStopX = rhythmStartX + timeToWidth(it.time)
            if (rhythmStopX<0){//超出屏幕就不要画了
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
        release()
        lineInfoMap.forEach {
            val duration = it.value.wordsDisInterval
            val startTime = it.value.wordsStartTime
            val wordsIndex = it.value.wordsIndex
            val wordsList = it.value.lyricsWords
            val lineLyrics = it.value.lineLyrics
            duration.forEachIndexed { index, l ->
                val lastWord = duration.size == index+1
                mRhythmList.add(Rhythm(l,startTime[index],wordsIndex[index],wordsList[index],lineLyrics,lastWord,index))
            }

        }
        mTotalTime = totalTime
        mValueAnimator.setFloatValues(0f,1f)
        mValueAnimator.duration = mTotalTime
        LogUtils.i("totalTime: $mTotalTime")
        maxRhythmStartX = timeToWidth(mTotalTime)
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
        mCurrentLineLyrics = ""
        mSingNow = false
        mMoveWidth = 0f
        lyricCallBackListener?.currentSingLyric("")
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
        if (!mValueAnimator.isRunning){
            return
        }

        if (mSingNow == sing){
            return
        }
        //如果原节奏已经唱完了，就不往下走了
        if (mRhythmList[dataIndex].wordHaveSing){
            if (!sing){//这种情况是，原节奏已经结束了，用户还在唱，此时的mSingNow还是true，这样会造成下一段节奏来时，用户不唱，也会画已唱的节奏
                mSingNow = sing
            }
            return
        }

        if (sing){//记录开唱时间
//            LogUtils.i("开始唱")
            mStartRecordTime = mValueAnimator.currentPlayTime
        }else{//不唱了要把用户的
//            LogUtils.i("不唱了")
            val currentPlayTime = mValueAnimator.currentPlayTime
            mSingTime = currentPlayTime - mStartRecordTime
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
     * 添加唱完数据
     * 用户正在唱，但是这个字的节奏结束了，那么就需要记录一下
     * 这个场景是用户一直唱，然后一个字已唱完，下一个字就不需要重新开始唱来记录，
     */
    private fun addSingFinishData(dataIndex: Int){
        //没有在唱就不添加
        if (!mSingNow){
            return
        }
        //如果原节奏已经唱完了，就不往下走了
        if (mRhythmList[dataIndex].wordHaveSing){
            return
        }

        mSingTime = mValueAnimator.currentPlayTime - mStartRecordTime
        var singerRhythmList = mSingerRhythmData[dataIndex]
        if (singerRhythmList ==null){
            singerRhythmList = ArrayList()
        }
        singerRhythmList.add(SingerRhythm(mStartRecordTime,mSingTime,mRhythmList[dataIndex].wordIndex))
        mSingerRhythmData[dataIndex] = singerRhythmList

        if (dataIndex+1<=mRhythmList.size-1){//下一个字的开始时间就是开始唱的时间
            mStartRecordTime = mRhythmList[dataIndex+1].startTime
        }
    }

    /**
     * 用户已经唱了的时长
     */
    private var mSingTime = 0L

    /**
     * 用户开始唱的时间
     */
    private var mStartRecordTime = 0L

    /**
     * 用户唱歌数据
     */
    private var mSingerRhythmData = SparseArray<ArrayList<SingerRhythm>>()

    /**
     * 当前播放歌词回调
     */
    interface LyricCallBackListener{
        fun currentSingLyric(lyric: String)
    }

    /**
     * 歌手已唱节奏数据
     */
    data class SingerRhythm(
        var startTime: Long,
        var time: Long,
        var lineIndex: Int)

    /**
     * 节奏数据
     */
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
         * 所在的歌词行
         */
        var lineLyrics: String,

        /**
         * 是否最后一个字
         */
        var isLastWord: Boolean,

        /**
         * 该字在该行的下标
         */
        var wordInLineIndex: Int,

        /**
         * 单个歌词已唱完
         */
        var wordHaveSing: Boolean = false,

        /**
         * 一行歌词已唱完
         */
        var lineLyricsHaveSing: Boolean = false,


    )
}