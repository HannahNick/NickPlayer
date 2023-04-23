package com.nick.music.entity

import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus

class PlayInfo {
    var dataIndex = -1
    var playStatus = PlayStatus.PAUSE
    var playMode = PlayMode.PLAY_CYCLE
    //专辑名
    var albumName = ""
    //演唱者
    var mainActor = ""
}