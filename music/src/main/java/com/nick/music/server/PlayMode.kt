package com.nick.music.server

interface PlayMode {
    companion object{
        //循环播放
        const val PLAY_CYCLE = "PLAY_CYCLE"
        //单曲循环
        const val PLAY_SINGLE = "PLAY_SINGLE"
        //随机播放
        const val PLAY_RANDOM = "PLAY_RANDOM"
    }
}