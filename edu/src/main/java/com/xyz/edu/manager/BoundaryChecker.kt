package com.xyz.edu.manager

import android.graphics.Rect
import android.view.View
import com.xyz.edu.widget.MovingCardView
import com.xyz.edu.widget.RotateView
import com.xyz.edu.widget.TargetParent

class BoundaryChecker(private val listener: BoundaryListener) {
    private val targets = mutableListOf<TargetParent>()

    fun addTarget(target: TargetParent) {
        targets.add(target)
    }

    fun checkBoundaries(source: MovingCardView) {
        val sourceRect = Rect()
        source.getGlobalVisibleRect(sourceRect)

        targets.forEach { target ->
            val targetRect = Rect()
            target.getGlobalVisibleRect(targetRect)
            val intersects = Rect.intersects(sourceRect, targetRect)
            listener.onBoundaryIntersected(source, target,intersects)
        }
    }

    fun connectSource(source: MovingCardView,block:(TargetParent)-> Unit){
        val sourceRect = Rect()
        source.getGlobalVisibleRect(sourceRect)
        targets.forEach { target ->
            val targetRect = Rect()
            target.getGlobalVisibleRect(targetRect)
            val intersects = Rect.intersects(sourceRect, targetRect)
            if (intersects){
                block(target)
            }
        }
    }

    interface BoundaryListener {
        fun onBoundaryIntersected(source: MovingCardView, target: View,isIntersects: Boolean)
    }
}
