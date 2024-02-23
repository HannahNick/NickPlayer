package com.xyz.game

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.nick.base.manager.DialogManager
import com.nick.base.manager.PlanManager
import com.orhanobut.dialogplus.DialogPlus

open class AppActivity : AppCompatActivity(), PlanManager.PreInitDataCallBack {
    protected var backPressedTime1: Long = 0
    protected var backPressedTime2: Long = 0
    var itemIndex:Int = 0
    private val dialogPlus by lazy { DialogManager.initLoading(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemIndex = intent.getIntExtra("itemIndex",0)
        PlanManager.registerDataCallBack(this)
//        val callback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                val currentTime = System.currentTimeMillis()
//                if (currentTime - backPressedTime1 >= 2000) {
//                    backPressedTime1 = currentTime
//                    Toast.makeText(applicationContext, "再按一次退出", Toast.LENGTH_SHORT).show()
//                } else {
//                    isEnabled = false
//                    finish()
//                }
//            }
//
//        }
//        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun finish() {
        super.finish()
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - backPressedTime2 >= 2000) {
//            backPressedTime2 = currentTime
//            Toast.makeText(applicationContext, "Press exit again", Toast.LENGTH_SHORT).show()
//        } else {
//            super.finish()
//        }
    }
    fun finish(flag:Boolean){
        PlanManager.toNextPlanItem(this,itemIndex,object : PlanManager.LoadingListener{
            override fun showLoading() {
                dialogPlus.show()
            }

            override fun hideLoading() {
                dialogPlus.dismiss()
            }

        })
    }

    override fun preInitDataFinish() {
        super.finish()
    }

}