package com.nick.music.callback

interface PositionInitFinishListener{
    /**
     * 展示预览
     */
    fun showPreView(index: Int,isTopLyrics: Boolean)
}