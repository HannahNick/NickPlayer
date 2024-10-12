package com.nick.music.entity

import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus

class PlayInfo {
    //当前播放列表下标，与服务器数据一致
    var dataIndex = -1
    //播放状态
    var playStatus = PlayStatus.PAUSE
    //播放状态(扩展)
    var playStatusEx = PlayStatus.PAUSE
    //播放模式
    var playMode = PlayMode.CYCLE
    //图片
    var imgPath = ""
    //内置图片资源文件id
    var localImgRes = -1
    //歌名
    var songName = ""
    //演唱者
    var mainActor = ""
    //当前播放位置
    var currentPosition = 0L
    //播放总时长
    var duration = 0L
    //    var liveName = ""
    //歌词路径
    var lyricPath = ""
    //播放外链
    var url = ""
    override fun toString(): String {
        return "PlayInfo(dataIndex=$dataIndex, playStatus='$playStatus', playMode=$playMode, imgPath='$imgPath',localImgRes=$localImgRes ,songName='$songName', mainActor='$mainActor', currentPosition=$currentPosition, duration=$duration, lyricPath='$lyricPath', url='$url')"
    }
}