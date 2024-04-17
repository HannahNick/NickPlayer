package com.xyz.edu.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.ConvertUtils
import com.xyz.base.utils.L
import com.xyz.edu.R

class RotateView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var doPlay: Boolean = false
    val rotate1 = ObjectAnimator.ofFloat(this, "rotation", 0f, 15f).apply {
        startDelay = 550
        duration=3000
        //先加速后减速
        interpolator = DecelerateInterpolator()
        addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {

            }

            override fun onAnimationEnd(animation: android.animation.Animator) {
                rotate2.start()
            }

            override fun onAnimationCancel(animation: android.animation.Animator) {
            }

            override fun onAnimationRepeat(animation: android.animation.Animator) {
            }

        })
    }

    val rotate2 = ObjectAnimator.ofFloat(this, "rotation", 15f, -15f).apply {
        repeatCount = ObjectAnimator.INFINITE
        repeatMode = ObjectAnimator.REVERSE
        duration = 3000
        //中间快,开始和结束慢
        interpolator = AccelerateDecelerateInterpolator()
    }

    fun play() {
        if (rotate1.isStarted||rotate2.isStarted){
            L.i("动画已经在动了")
            return
        }
        L.i("开始动画: pivotX:$pivotX pivotY:$pivotY width:$width height:$height X:$x Y:$y")
        //设置旋转圆心坐标
        this.pivotX = width*0.5f
        this.pivotY = 0f

        rotate1.start()
    }

    fun addSource(source: MovingCardView){
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConvertUtils.dp2px(50f))
        addView(source,layoutParams)
        animateViewToCenter(source,this)
        //这是一个异步操作
        requestLayout()
        doPlay = true
    }

    private fun animateViewToCenter(view: View, container: ConstraintLayout) {
        view.post {
            val transition = ChangeBounds()
            //慢慢加速到指定位置
            transition.interpolator = AccelerateInterpolator()
            transition.duration = 500

            TransitionManager.beginDelayedTransition(container, transition)

            val layoutParams = view.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.startToStart = R.id.ropeImage
            layoutParams.endToEnd = R.id.ropeImage
            layoutParams.bottomToBottom = R.id.ropeImage
            view.layoutParams = layoutParams
            view.x = 0f
            view.y = 0f
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        L.i("onLayout: pivotX:$pivotX pivotY:$pivotY width:$width height:$height X:$x Y:$y")
        if (doPlay){
            play()
        }
    }
}