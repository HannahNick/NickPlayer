package com.xyz.edu.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.BarUtils
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
        BarUtils.setStatusBarVisibility(this,false)

        mBinding.apply {
            cardViewB.dragCallBack = this@DragSelectGameActivity
            mBoundaryChecker.addTarget(mBinding.tpContain)
        }

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
        source.isInTarget = isIntersects
    }

}