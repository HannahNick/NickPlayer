package com.xyz.edu.vo

data class WordLearningWindowItemVo(
    val title: String,
    val progress: Float,
    val state: DownLoadState
){
    enum class DownLoadState{
        PROGRESS,FINISH
    }
}