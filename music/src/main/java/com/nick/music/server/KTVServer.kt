package com.nick.music.server

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.nick.music.player.impl.NickExoPlayer
import com.nick.music.player.impl.NickPlayer
import com.nick.music.server.binder.impl.MusicServerBinder
import com.nick.music.server.binder.impl.TwoPlayerServerBinder

class KTVServer: Service() {

//    private val playerControl = NickPlayer()
    val vodControl by lazy {   NickExoPlayer(this,"KTVServer") }
    val musicControl by lazy {   NickExoPlayer(this,"KTVServer") }
    override fun onCreate() {
        super.onCreate()

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBind(intent: Intent?): IBinder {
//        val playerControl = NickPlayer()

        return TwoPlayerServerBinder(vodControl,musicControl)
    }


    override fun onDestroy() {
        super.onDestroy()
        vodControl.release()
        musicControl.release()
    }
}