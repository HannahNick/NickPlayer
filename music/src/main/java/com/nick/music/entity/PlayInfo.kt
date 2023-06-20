package com.nick.music.entity

import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus

class PlayInfo {
    var dataIndex = -1
    var playStatus = PlayStatus.PAUSE
    var playMode = PlayMode.CYCLE
    //专辑名
    var albumName = ""
    //演唱者
    var mainActor = ""
    var currentPosition = 0
    var duration = 0
    var liveName = ""
    var lyricPath = ""
}