package com.nick.music.model

/**
 * 记录当前一行歌词
 */
data class KrcLineWord (
    /**
     * 原音
     */
    var origin: String,
    /**
     * 翻译
     */
    var translate: String = "",
    /**
     * 注音
     */
    var transliteration: String = "",
)
