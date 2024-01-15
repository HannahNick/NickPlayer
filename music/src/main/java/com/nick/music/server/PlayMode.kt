package com.nick.music.server

enum class PlayMode {
    /**
     * 默认播放，播完就暂停
      */
    DEFAULT,

    /**
     * 循环播放，播放列表循环
     */
    CYCLE,

    /**
     * 单曲循环
     */
    SINGLE,

    /**
     * 随机播放，播放列表循环随机播放
     */
    RANDOM
}