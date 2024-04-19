package com.xyz.edu.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import kotlin.math.max

class HorizontalDistributeViewGroup(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxHeight = 0
        var totalWidth = 0

        // 测量所有子视图，并找出最大高度和总宽度
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            totalWidth += child.measuredWidth
            maxHeight = max(maxHeight, child.measuredHeight)
        }

        // 考虑内部间距
        totalWidth += paddingLeft + paddingRight
        maxHeight += paddingTop + paddingBottom

        // 根据子视图的尺寸设置自身尺寸
        setMeasuredDimension(
            resolveSize(totalWidth, widthMeasureSpec),
            resolveSize(maxHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val width = r - l - paddingLeft - paddingRight // 考虑左右内边距

        // 计算每个子视图应分配的宽度
        val childWidth = width / count

        for (i in 0 until count) {
            val child = getChildAt(i)
            val childMeasuredWidth = child.measuredWidth
            val childMeasuredHeight = child.measuredHeight

            // 计算每个子视图的左边和右边位置
            val childLeft = paddingLeft + i * childWidth + (childWidth - childMeasuredWidth) / 2
            val childTop = 0
            val childRight = childLeft + childMeasuredWidth
            val childBottom = childTop + childMeasuredHeight

            // 布局子视图
            child.layout(childLeft, childTop, childRight, childBottom)
        }
    }
}