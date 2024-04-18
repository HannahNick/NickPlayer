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
        val width = r - l // ViewGroup的总宽度

        // 计算所有可见子视图的总宽度
        var totalChildWidth = 0
        for (i in 0 until count) {
            val child = getChildAt(i)
            totalChildWidth += child.measuredWidth
        }

        // 计算居中布局的起始 X 坐标
        var currentX = (width - totalChildWidth) / 2

        for (i in 0 until count) {
            val child = getChildAt(i)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            val childLeft = currentX
            val childTop = paddingTop // 垂直方向上可以根据需要调整对齐方式
            val childRight = childLeft + childWidth
            val childBottom = childTop + childHeight

            // 布局子视图
            child.layout(childLeft, childTop, childRight, childBottom)

            // 更新 currentX，准备下一个子视图的布局
            currentX += childWidth
        }
    }
}