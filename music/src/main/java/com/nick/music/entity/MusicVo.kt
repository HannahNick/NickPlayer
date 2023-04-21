package com.nick.music.entity

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
    val url: String,
)