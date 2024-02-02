package com.xyz.game

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

open class AppActivity : AppCompatActivity() {
    protected var backPressedTime1: Long = 0
    protected var backPressedTime2: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime1 >= 2000) {
                    backPressedTime1 = currentTime
                    Toast.makeText(applicationContext, "再按一次退出", Toast.LENGTH_SHORT).show()
                } else {
                    isEnabled = false
                    finish()
                }
            }

        }
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun finish() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime2 >= 2000) {
            backPressedTime2 = currentTime
            Toast.makeText(applicationContext, "再按一次退出", Toast.LENGTH_SHORT).show()
        } else {
            super.finish()
        }
    }

}