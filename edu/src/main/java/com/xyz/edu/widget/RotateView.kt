package com.xyz.edu.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.ConvertUtils
import com.xyz.base.utils.L
import com.xyz.edu.R

class RotateView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var doPlay: Boolean = false
    val rotate1 = ObjectAnimator.ofFloat(this, "rotation", 0f, 10f).apply {
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

    val rotate2 = ObjectAnimator.ofFloat(this, "rotation", 10f, -10f).apply {
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
        moveSource(source){
            (source.parent as? ViewGroup)?.removeView(source)
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConvertUtils.dp2px(40f))
            layoutParams.startToStart = R.id.ropeImage
            layoutParams.endToEnd = R.id.ropeImage
            layoutParams.bottomToBottom = R.id.ropeImage
            addView(source,layoutParams)
            val paddingDp = ConvertUtils.dp2px(20f)
            setPadding(paddingDp,0,paddingDp,paddingDp)
//        animateViewToCenter(source,this)
            source.x = 0f
            source.y = 0f
//        这是一个异步操作
            requestLayout()
            doPlay = true
        }

    }

    private fun moveSource(source: MovingCardView,block:()->Unit){
        //获取当前View的左上角绝对坐标
        val targetLocation = IntArray(2)
        getLocationInWindow(targetLocation)

        val targetX = targetLocation[0] - source.width*0.5 + width*0.5  // 目标X坐标 左上角点的坐标-答案宽度的一半+目标宽度的一半
        val targetY = targetLocation[1]+ height - source.height  // 目标Y坐标

//        val location = IntArray(2)
//        source.getLocationOnScreen(location)
        val currentX = source.x
        val currentY = source.y
        L.i("开始动画: pivotX:${source.pivotX} pivotY:${source.pivotY} width:${source.width} height:${source.height} X:${source.x} Y:${source.y}")
        L.i("targetX:${targetX} targetY:${targetY}")

        val animX = ObjectAnimator.ofFloat(source, "translationX", currentX, targetX.toFloat())
        val animY = ObjectAnimator.ofFloat(source, "translationY", currentY, targetY.toFloat())
        val animR = ObjectAnimator.ofFloat(source, "rotation", source.rotation, 0f)

        animX.duration = 300  // 动画持续时间，例如1000毫秒
        animY.duration = 300
        animR.duration = 300

        // 同时执行两个动画
        val animSet = AnimatorSet()
        animSet.addListener(object :AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                block.invoke()
            }
        })
        animSet.interpolator = DecelerateInterpolator()
        animSet.playTogether(animX, animY,animR)
        animSet.start()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        L.i("onLayout: pivotX:$pivotX pivotY:$pivotY width:$width height:$height X:$x Y:$y")
        if (doPlay){
            play()
        }
    }
}