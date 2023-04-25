package com.nick.base.vo

import java.util.Date

data class MusicVo (
    val id: String,
    /**
     * 专辑名
     */
    val albumName: String,
    /**
     * 演唱者
     */
    val mainActors:String,
    /**
     * 外链地址
     */
    val path: String,
    /**
     * 图片地址
     */
    val imgPath: String,
    /**
     * 创建时间
     */
    val createTime: String,
    /**
     * 更新时间
     */
    val updateTime: String,
)