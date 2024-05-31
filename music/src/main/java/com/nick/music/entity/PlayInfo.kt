package com.nick.music.entity

import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus

class PlayInfo {
    var dataIndex = -1
    var playStatus = PlayStatus.PAUSE
    var playMode = PlayMode.CYCLE
    var imgPath = ""
    var songName = ""
    //演唱者
    var mainActor = ""
    var currentPosition = 0L
    var duration = 0L
    //    var liveName = ""
    var lyricPath = ""
    var url = ""
}