package com.xyz.edu.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.blankj.utilcode.util.ConvertUtils
import com.xyz.base.utils.L
import com.xyz.edu.R
import com.xyz.edu.callback.DragCallBack

class MovingCardView@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var initialX = 0f
    private var initialY = 0f
    private var downRawX = 0f
    private var downRawY = 0f
    var dragCallBack: DragCallBack? = null
    var isInTarget: Boolean = false
    var isConnect: Boolean = false
    var targetIndex: Int = -1

    private val textView: AppCompatTextView by lazy { AppCompatTextView(context) }

    init {
        textView.apply {
            setPadding(ConvertUtils.dp2px(5f),0,ConvertUtils.dp2px(5f),0)
            setTextColor(Color.BLACK)
            setTextSize(ConvertUtils.sp2px(8f).toFloat())
            gravity = Gravity.CENTER
            text = "MovingCardView"
            height = ConvertUtils.dp2px(40f)
        }
        addView(textView)
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        //已经连接就不允许交互
        if (isConnect){
            return true
        }
        val action = event.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                // Save the initial position.
                initialX = x
                initialY = y

                // Remember where we started (for dragging)
                downRawX = event.rawX
                downRawY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                // Calculate the distance moved
                dragCallBack?.isMoving(this)
//                L.i("move,${dragCallBack==null}")
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY

                // Move the object
                x = initialX + dx
                y = initialY + dy

                // Invalidate to request a redraw
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                // Use ObjectAnimator to animate back to the original position
                if (isInTarget){
                    dragCallBack?.playConnect(this)
                    isConnect = true
                }else{
                    animateBack()
                }
            }
        }
        return true // 消费触摸事件
    }

    private fun animateBack() {
        // Animate X position
        val animatorX = ObjectAnimator.ofFloat(this, "x", x, initialX)
        animatorX.duration = 300

        // Animate Y position
        val animatorY = ObjectAnimator.ofFloat(this, "y", y, initialY)
        animatorY.duration = 300

        animatorX.start()
        animatorY.start()
    }

    fun setText(content: String){
        textView.text = content
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }
}