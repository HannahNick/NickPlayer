package com.nick.music.player


interface CurrentPositionCallBack {
    fun playPosition(position: Int,duration: Int)
}