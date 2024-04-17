package com.xyz.edu.callback

import com.xyz.edu.widget.MovingCardView

interface DragCallBack {
    fun isMoving(view: MovingCardView)

    fun playConnect(view: MovingCardView)
}