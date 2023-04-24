package com.nick.music.player

import java.time.Duration

interface CurrentPositionCallBack {
    fun playPosition(position: Int,duration: Int)
}