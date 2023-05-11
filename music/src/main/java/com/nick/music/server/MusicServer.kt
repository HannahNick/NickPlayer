package com.nick.music.server

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.AppUtils
import com.nick.music.player.PlayerControl
import com.nick.music.player.impl.NickExoPlayer
import com.nick.music.player.impl.NickPlayer
import com.nick.music.server.binder.impl.MusicServerBinder

class MusicServer: Service() {

//    private val playerControl = NickPlayer()

    override fun onCreate() {
        super.onCreate()

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBind(intent: Intent?): IBinder {
        val playerControl = NickPlayer()
//        val playerControl = NickExoPlayer(this)
        return MusicServerBinder(playerControl)
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}