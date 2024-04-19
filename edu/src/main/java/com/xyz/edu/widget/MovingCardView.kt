package com.xyz.edu.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.ConvertUtils
import com.xyz.edu.R
import com.xyz.edu.callback.DragCallBack

class MovingCardView@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var mInitialX = 0f
    private var mInitialY = 0f
    private var mDownRawX = 0f
    private var mDownRawY = 0f
    //拖拽回调
    var mDragCallBack: DragCallBack? = null
    //当前View是否进入到目标范围
    var mIsInTarget: Boolean = false
    //当前View是否连接到了目标
    var mIsConnect: Boolean = false
    //目标的下标
    var mTargetIndex: Int = -1

    private val mTextView: AppCompatTextView by lazy { AppCompatTextView(context).apply {
            this.setPadding(ConvertUtils.dp2px(5f),0,ConvertUtils.dp2px(5f),ConvertUtils.dp2px(5f))
        this.setTextColor(Color.WHITE)
        this.setTextSize(ConvertUtils.sp2px(10f).toFloat())
        this.text = "大大打打大大多多"
    } }

    init {
        addView(mTextView)
        background = AppCompatResources.getDrawable(context,R.drawable.answer_bg)
        gravity = Gravity.CENTER
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        //已经连接就不允许交互
        if (mIsConnect){
            return true
        }
        val action = event.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                // Save the initial position.
                mInitialX = x
                mInitialY = y

                // Remember where we started (for dragging)
                mDownRawX = event.rawX
                mDownRawY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                // Calculate the distance moved
                mDragCallBack?.isMoving(this)
//                L.i("move,${dragCallBack==null}")
                val dx = event.rawX - mDownRawX
                val dy = event.rawY - mDownRawY

                // Move the object
                x = mInitialX + dx
                y = mInitialY + dy

                // Invalidate to request a redraw
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                // Use ObjectAnimator to animate back to the original position
                if (mIsInTarget){
                    mDragCallBack?.playConnect(this)
                    mIsConnect = true
                }else{
                    animateBack()
                }
            }
        }
        return true // 消费触摸事件
    }

    private fun animateBack() {
        // Animate X position
        val animatorX = ObjectAnimator.ofFloat(this, "x", x, mInitialX)
        animatorX.duration = 300

        // Animate Y position
        val animatorY = ObjectAnimator.ofFloat(this, "y", y, mInitialY)
        animatorY.duration = 300

        animatorX.start()
        animatorY.start()
    }

    fun setText(content: String){
        mTextView.text = content
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }
}