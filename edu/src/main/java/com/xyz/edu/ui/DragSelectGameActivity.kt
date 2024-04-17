package com.xyz.edu.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.xyz.edu.callback.DragCallBack
import com.xyz.edu.databinding.ActivityDragSelectGameBinding
import com.xyz.edu.manager.BoundaryChecker
import com.xyz.edu.widget.MovingCardView

class DragSelectGameActivity : AppCompatActivity(),DragCallBack,BoundaryChecker.BoundaryListener {

    private val mBinding by lazy { ActivityDragSelectGameBinding.inflate(layoutInflater) }
    private val mBoundaryChecker by lazy { BoundaryChecker(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mBinding.apply {
            val pointX = (cardViewA.x + cardViewA.width)/2
            val pointY = cardViewA.y
            ropeImage.pivotX = pointX
            ropeImage.pivotY = pointY
            cardViewB.dragCallBack = this@DragSelectGameActivity
            rotateView.setOnClickListener {
                rotateView.play()
            }
            mBoundaryChecker.addTarget(mBinding.rotateView)
        }

    }

    override fun isMoving(view: MovingCardView) {
        mBoundaryChecker.checkBoundaries(view)
    }

    override fun playConnect(view: MovingCardView) {
        mBoundaryChecker.connectSource(view){
            mBinding.root.removeView(view)
            it.addSource(view)
        }
    }

    override fun onBoundaryIntersected(source: MovingCardView, target: View, isIntersects: Boolean) {
        source.isInTarget = isIntersects
    }

}