package com.xyz.edu.manager

import android.graphics.Rect
import android.view.View
import com.xyz.edu.widget.MovingCardView
import com.xyz.edu.widget.TargetParent

/**
 * 答案和图片View 接触边缘检查
 */
class BoundaryChecker(private val listener: BoundaryListener) {
    val targets = mutableListOf<TargetParent>()

    fun addTarget(target: TargetParent) {
        targets.add(target)
    }

    fun checkBoundaries(source: MovingCardView) {
        if (source.mTargetIndex>=targets.size){
            return
        }

        val sourceRect = Rect()
        source.getGlobalVisibleRect(sourceRect)
        val targetRect = Rect()
        val target = targets[source.mTargetIndex]
        target.getGlobalVisibleRect(targetRect)
        val intersects = Rect.intersects(sourceRect, targetRect)
        listener.onBoundaryIntersected(source, target,intersects)
    }

    fun connectSource(source: MovingCardView,block:(TargetParent)-> Unit){
        val sourceRect = Rect()
        source.getGlobalVisibleRect(sourceRect)
        val targetRect = Rect()
        val target = targets[source.mTargetIndex]
        target.getGlobalVisibleRect(targetRect)
        val intersects = Rect.intersects(sourceRect, targetRect)
        if (intersects){
            block(target)
        }
    }

    interface BoundaryListener {
        fun onBoundaryIntersected(source: MovingCardView, target: View,isIntersects: Boolean)
    }
}
