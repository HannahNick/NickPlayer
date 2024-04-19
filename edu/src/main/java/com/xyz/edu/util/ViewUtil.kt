package com.xyz.edu.util

import android.graphics.Point
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import com.xyz.base.utils.L
import com.xyz.edu.widget.MovingCardView
import kotlin.random.Random

object ViewUtil {

    fun placeViews(container: ViewGroup, movingCardView: List<MovingCardView>) {

        for (i in 0 until movingCardView.size){
            movingCardView[i].apply {
                visibility = View.INVISIBLE
                container.addView(this)
                mTargetIndex = i
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
                    val randomY = Random.nextInt(container.height*2/3,container.height - view.height)
                    val rotation = Random.nextInt(-7,8)

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

    private fun checkOverlap(view: View, views: List<View>): Boolean {
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


    fun getCenterPointOfView(view: View): Point {
        val location = IntArray(2)
        view.getLocationOnScreen(location) // 获取屏幕坐标
        val centerX = location[0] + view.width / 2
        val centerY = location[1] + view.height / 2
        return Point(centerX, centerY) // 创建一个 Point 对象表示中心点
    }
}