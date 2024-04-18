package com.xyz.edu.ui

import android.content.Context
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ConvertUtils
import com.xyz.base.utils.L
import com.xyz.edu.R
import com.xyz.edu.callback.DragCallBack
import com.xyz.edu.databinding.ActivityDragSelectGameBinding
import com.xyz.edu.manager.BoundaryChecker
import com.xyz.edu.widget.MovingCardView
import com.xyz.edu.widget.TargetParent
import kotlin.random.Random

class DragSelectGameActivity : AppCompatActivity(),DragCallBack,BoundaryChecker.BoundaryListener {

    private val mBinding by lazy { ActivityDragSelectGameBinding.inflate(layoutInflater) }
    private val mBoundaryChecker by lazy { BoundaryChecker(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        BarUtils.setStatusBarVisibility(this,false)

        mBinding.apply {
            for (i in 0 until 4){
                val target = TargetParent(this@DragSelectGameActivity,null,0).apply {
                    index = i
                }
                mBoundaryChecker.addTarget(target)
                llTargetPool.addView(target)
            }
        }


        placeViews(mBinding.root,this)
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


    fun placeViews(container: ViewGroup, context: Context) {
        val movingCardView = listOf(
            LayoutInflater.from(context).inflate(R.layout.item_movingcard,null,false) as MovingCardView,
            LayoutInflater.from(context).inflate(R.layout.item_movingcard,null,false) as MovingCardView,
            LayoutInflater.from(context).inflate(R.layout.item_movingcard,null,false) as MovingCardView,
            LayoutInflater.from(context).inflate(R.layout.item_movingcard,null,false) as MovingCardView,
        )
        for (i in 0 until movingCardView.size){
            movingCardView[i].apply {
                dragCallBack = this@DragSelectGameActivity
                visibility = View.INVISIBLE
                container.addView(this)
                targetIndex = i
                setText("movingCardView$i")
            }

        }
        L.i("container childCount:${container.childCount}")

        container.post {
            movingCardView.forEach { view ->
                view.visibility = View.VISIBLE
                var overlap: Boolean
                L.i("container.width: ${container.width} height:${container.height},view.width:${view.width} height:${view.height} rotation:${view.rotation}")

                do {
                    val randomX = Random.nextInt(0,container.width - view.width)
                    val randomY = Random.nextInt(container.height/2,container.height - view.height)
                    val rotation = Random.nextInt(-5,5)

                    view.rotation = rotation.toFloat()
                    view.x = randomX.toFloat()
                    view.y = randomY.toFloat()
                    L.i("rotation: $rotation")
                    overlap = checkOverlap(view, movingCardView)
                    L.i("overlap:$overlap")
                } while (overlap)
            }
        }

    }

    fun checkOverlap(view: View, views: List<View>): Boolean {
        val rect1 = Rect().apply { view.getGlobalVisibleRect(this) }
        views.forEach { other ->
            if (view !== other) {
                val rect2 = Rect().apply { other.getGlobalVisibleRect(this) }
                if (Rect.intersects(rect1, rect2)) {
                    return true
                }
            }
        }
        return false
    }
}