package com.nick.music.util

import java.util.*

object UrlUtil {
    fun isAudioUrl(url: String): Boolean {
        val audioExtensions = listOf(".mp3", ".wav", ".ogg", ".m4a", ".aac", ".flac") // 添加其他可能的音频文件扩展名
        val lowercaseUrl = url.lowercase(Locale.getDefault())

        for (extension in audioExtensions) {
            if (lowercaseUrl.endsWith(extension)) {
                return true
            }
        }

        return false
    }
}