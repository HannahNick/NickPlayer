package com.nick.music.entity

data class SrcLyricsInfoVo(
    val statements: Statement,
    val lyricsLineInfo: ArrayList<LyricsLineBean>
){

    data class Statement(
        val ar: String,
        val ti: String
    )
    data class LyricsLineBean(
        val originalLineWords: String,
        val translateLineWords: String,
        val phoneticLineWords: String,
        val startTime: Long,
        val duration: Long,
        val color: String,
        val wordsDetail: WordsDetailBean

    ){
        data class WordsDetailBean(
            val originalWords: ArrayList<String>,
            val translateWords: ArrayList<String>,
            val phoneticWords: ArrayList<String>,
            val startTime: ArrayList<Long>,
            val duration: ArrayList<Long>,
        )
    }
}