package com.nick.music.view

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R

class KrcLineView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0): View(context,attributeSet,defStyleAttr) {

    private val mWordsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mWordsSingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMeasureRect = Rect()
    private val mWordsSingRect = Rect()
    /**
     * 播放数值发生器，是播放移动的核心0~100%变化
     */
    private val mValueAnimator = ValueAnimator.ofFloat(0f,1f)
    private var mAnimValue = 0f

    init {
        mValueAnimator.apply {
            interpolator = LinearInterpolator()
            duration = 5000
            repeatCount = 100
            addUpdateListener {
                mAnimValue = it.animatedValue as Float

//                    LogUtils.i("movewidth: $mMoveWidth")
                invalidate()
            }
            doOnEnd {
                LogUtils.i("节奏结束 duration: $duration")
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWordsPaint.apply {
            textSize = 20f
            color = context.resources.getColor(R.color.black,null)
            clipBounds
        }
        mWordsSingPaint.apply {
            textSize = 20f
            color = context.resources.getColor(R.color.sing_rhythm,null)
        }
        mRectPaint.apply {
            color = context.resources.getColor(R.color.sing_rhythm,null)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        mValueAnimator.start()
    }

    val text = "安卓KTV歌词开发,不太好搞"

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制文本
        canvas.drawText(text, 0f, mWordsPaint.textSize, mWordsPaint)

        // 获取绘制文本的宽度和高度
        val textWidth = mWordsPaint.measureText(text)
        mWordsPaint.getTextBounds(text, 0, text.length, mMeasureRect)
        val textHeight = mMeasureRect.height()

        // 在画布上绘制矩形框来表示文本的宽度和高度
        mWordsSingRect.left = 0
        mWordsSingRect.top = 0
        mWordsSingRect.right = textWidth.toInt()
        mWordsSingRect.bottom = textHeight

        canvas.drawRect(mWordsSingRect, mRectPaint)
        canvas.save()
        mWordsSingRect.right = (textWidth*mAnimValue).toInt()
        canvas.clipRect(mWordsSingRect)
        canvas.drawText(text,0f,mWordsSingPaint.textSize,mWordsSingPaint)
        canvas.restore()
    }
}