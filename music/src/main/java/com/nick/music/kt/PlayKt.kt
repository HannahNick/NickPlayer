package com.nick.music.kt

import android.media.MediaPlayer

fun MediaPlayer.play(url: String){
    this.reset()
    this.setDataSource(url)
    this.prepareAsync()
}