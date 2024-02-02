package com.xyz.game

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.nick.base.router.PlanManager

open class AppActivity : AppCompatActivity(), PlanManager.PreInitDataCallBack {
    protected var backPressedTime1: Long = 0
    protected var backPressedTime2: Long = 0
    var itemIndex:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemIndex = intent.getIntExtra("itemIndex",0)
        PlanManager.registerDataCallBack(this)
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
            Toast.makeText(applicationContext, "Press exit again", Toast.LENGTH_SHORT).show()
        } else {
            super.finish()
        }
    }
    fun finish(flag:Boolean){
        PlanManager.toNextPlanItem(this,itemIndex)
    }

    override fun preInitDataFinish() {
        super.finish()
    }

}