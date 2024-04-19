package com.xyz.edu.ui

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ConvertUtils
import com.xyz.edu.R
import com.xyz.edu.callback.DragCallBack
import com.xyz.edu.databinding.ActivityDragSelectGameBinding
import com.xyz.edu.manager.BoundaryChecker
import com.xyz.edu.util.ViewUtil
import com.xyz.edu.widget.MovingCardView
import com.xyz.edu.widget.TargetParent

class DragSelectGameActivity : AppCompatActivity(),DragCallBack,BoundaryChecker.BoundaryListener {

    private val mBinding by lazy { ActivityDragSelectGameBinding.inflate(layoutInflater) }
    private val mBoundaryChecker by lazy { BoundaryChecker(this) }
    private var mAnimator: AnimatorSet? = null
    private val mHandler by lazy { Handler(Looper.getMainLooper()) }
    private val mMovingCardList: ArrayList<MovingCardView> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        BarUtils.setStatusBarVisibility(this,false)

        mBinding.apply {
            val targetList = listOf(TargetParent(this@DragSelectGameActivity,null,0).apply {
                index = 0
                setImg(R.drawable.cat)
            },
                TargetParent(this@DragSelectGameActivity,null,0).apply {
                    index = 1
                    setImg(R.drawable.dog)
                },
                TargetParent(this@DragSelectGameActivity,null,0).apply {
                    index = 2
                    setImg(R.drawable.pig)
                },)

            for (i in 0 until targetList.size){
                mBoundaryChecker.addTarget(targetList[i])
                llTargetPool.addView(targetList[i])
            }
            ivBack.setOnClickListener {
                finish()
            }
        }
        mMovingCardList.addAll(listOf(
            (LayoutInflater.from(this).inflate(R.layout.item_movingcard,null,false) as MovingCardView).apply {
                mDragCallBack = this@DragSelectGameActivity
                setText("cat")
            },
            (LayoutInflater.from(this).inflate(R.layout.item_movingcard,null,false) as MovingCardView).apply {
                mDragCallBack = this@DragSelectGameActivity
                setText("dog")
            },
            (LayoutInflater.from(this).inflate(R.layout.item_movingcard,null,false) as MovingCardView).apply {
                mDragCallBack = this@DragSelectGameActivity
                setText("pig")
            },
        ))

        ViewUtil.placeViews(mBinding.root,mMovingCardList)
        mHandler.postDelayed({
            showFinger()
        },5000)

    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        mHandler.removeCallbacksAndMessages(null)
        mAnimator?.cancel()
        when(action){
            MotionEvent.ACTION_UP -> {
                mHandler.postDelayed({
                    showFinger()
                },5000)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun isMoving(view: MovingCardView) {
        mBoundaryChecker.checkBoundaries(view)
    }

    override fun playConnect(view: MovingCardView) {
        mBoundaryChecker.connectSource(view){
            it.addSource(view)
        }
    }

    override fun onBoundaryIntersected(source: MovingCardView, target: View, isIntersects: Boolean) {
        source.mIsInTarget = isIntersects
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mAnimator?.isStarted == true){
            mAnimator?.cancel()
        }
        mHandler.removeCallbacksAndMessages(null)
    }



    private fun showFinger(){
        val movingCardView = mMovingCardList.filter { !it.mIsConnect }.firstOrNull() ?:return

        val targetView = mBoundaryChecker.targets[movingCardView.mTargetIndex]
        val startPoint = ViewUtil.getCenterPointOfView(movingCardView)
        val endPoint = ViewUtil.getCenterPointOfView(targetView)

        val finger = AppCompatImageView(this)
        finger.layoutParams = ConstraintLayout.LayoutParams(ConvertUtils.dp2px(50f),ConvertUtils.dp2px(50f))
        finger.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.finger))
        mBinding.root.addView(finger)

        // 创建渐变动画
        val alphaAnimatorShow = ObjectAnimator.ofFloat(
            finger,
            View.ALPHA,
            0f, // 开始时完全透明
            1f, // 结束时完全不透明
        )
        alphaAnimatorShow.apply {
            duration = 200 // 设置动画持续时间
        }

        val alphaAnimatorDismiss = ObjectAnimator.ofFloat(
            finger,
            View.ALPHA,
            1f, // 开始时完全不透明
            0f, // 结束时完全透明
        )
        alphaAnimatorDismiss.apply {
            startDelay = 2300
            duration = 200
        }

        val translationAnimatorX = ObjectAnimator.ofFloat(
            finger,
            View.TRANSLATION_X,
            startPoint.x.toFloat(), // 开始位置
            endPoint.x.toFloat() // 结束位置
        )
        translationAnimatorX.apply {
            repeatCount = ObjectAnimator.INFINITE
            interpolator = DecelerateInterpolator()
            duration = 2500
        }

        val translationAnimatorY = ObjectAnimator.ofFloat(
            finger,
            View.TRANSLATION_Y,
            startPoint.y.toFloat(), // 开始位置
            endPoint.y.toFloat() // 结束位置
        )
        translationAnimatorY.apply {
            repeatCount = ObjectAnimator.INFINITE
            interpolator = DecelerateInterpolator()
            duration = 2500
            addListener(object :AnimatorListener{
                override fun onAnimationStart(animation: Animator) {
                    alphaAnimatorShow.start()
                    alphaAnimatorDismiss.start()
                }

                override fun onAnimationEnd(animation: Animator) {
                }

                override fun onAnimationCancel(animation: Animator) {
                    mBinding.root.removeView(finger)
                }

                override fun onAnimationRepeat(animation: Animator) {
                    alphaAnimatorShow.start()
                    alphaAnimatorDismiss.start()
                }

            })
        }

        mAnimator = AnimatorSet()
        mAnimator?.playTogether(translationAnimatorX, translationAnimatorY)
        mAnimator?.start()
    }



}