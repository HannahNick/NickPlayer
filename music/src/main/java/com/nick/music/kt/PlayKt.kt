package com.nick.music.kt

import android.media.MediaPlayer

fun MediaPlayer.play(url: String){
    this.stop()
    this.seekTo(0)
    this.setDataSource(url)
    this.prepareAsync()
}