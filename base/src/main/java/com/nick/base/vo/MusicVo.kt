package com.nick.base.vo

import android.os.Parcelable
import com.nick.base.vo.enum.UrlType
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicVo (
    val id: String = "",
    /**
     * 专辑名
     */
    val songName: String = "",
    /**
     * 演唱者
     */
    val mainActors:String = "",
    /**
     * 外链地址
     */
    var path: String = "",
    /**
     * url类型
     */
    val pathType: UrlType = UrlType.DEFAULT,
    /**
     * 图片地址
     */
    var imgPath: String = "",
    /**
     * 创建时间
     */
    val createTime: String = "",
    /**
     * 更新时间
     */
    val updateTime: String = "",
    /**
     * 直播台名
     */
//    val liveName: String = "",
    /**
     * 歌词路径
     */
    val lyricPath: String = "",
): Parcelable