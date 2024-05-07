package com.nick.music.entity

import com.nick.music.R

/**
 * 节奏数据
 */
data class Rhythm(

    /**
     * 每个字的持续时长
     */
    var duration: Long,

    /**
     * 每个字的绝对开始时间
     */
    var startTime: Long,

    /**
     * 每个字的显示位置下标,这个字段只有在节奏View中才会用到
     */
    var wordIndex:Int,

    /**
     * 原音歌词，一个字
     */
    var originalWord: String,

    /**
     * 翻译歌词，一个字
     */
    var translateWord: String = "",

    /**
     * 音译歌词，一个字
     */
    var transliterationWord: String = "",

    /**
     * 所在的歌词行
     */
    var lineLyrics: String,

    /**
     * 是否最后一个字
     */
    var isLastWord: Boolean,

    /**
     * 该字在该行的下标
     */
    var wordInLineIndex: Int,

    /**
     * 一行歌词在所有数据的下标,目前只有基础歌词会需要这个字段
     */
    val lineLyricsDataIndex: Int,

    /**
     * 当前字的颜色
     */
    val wordsColor: Int = R.color.male_voice,

    /**
     * 单个歌词已唱完
     */
    var wordHaveSing: Boolean = false,

    /**
     * 一行歌词已唱完
     */
    var lineLyricsHaveSing: Boolean = false,
)