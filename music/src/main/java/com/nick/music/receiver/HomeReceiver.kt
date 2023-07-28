package com.nick.music.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess


class HomeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            val reason = intent.getStringExtra("reason")
            if ("homekey" == reason){
                exitProcess(0)
            }
        }

    }
}