package com.nick.music.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.nick.music.player.impl.NickPlayer
import com.nick.music.server.binder.impl.MusicServerBinder

class MusicServer: Service() {

    private val playerControl = NickPlayer()
    private val musicBinder = MusicServerBinder(playerControl)

    override fun onCreate() {
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder {
        return musicBinder
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}