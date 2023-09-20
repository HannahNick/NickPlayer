package com.nick.music.model

/**
 * 记录当前一行歌词，目前使用在歌词预览
 */
data class KrcLineWord (
    /**
     * 原音歌词一行
     */
    var origin: String,

    /**
     * 是否有翻译歌词
     */
    var hasTransLate: Boolean = false,

    /**
     * 是否有音译歌词
     */
    var hasTransliteration: Boolean = false,

    /**
     * 翻译歌词一行
     */
    var translate: String = "",
    /**
     * 注音歌词一行
     */
    var transliteration: String = "",

    /**
     * 注音歌词数组，记录原音歌词每个字对应的注音
     */
    var transliterationArray: ArrayList<String> = ArrayList(),

    /**
     * 原音歌词数组，记录当前原音歌词每个字
     */
    var originArray: ArrayList<String> = ArrayList()

)
