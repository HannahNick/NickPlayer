package com.xyz.nickplayer.app

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter
import com.xyz.nickplayer.BuildConfig
import com.xyz.proxy.KtvPlayProxyManager

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        KtvPlayProxyManager.init(this)
        ARouter.init(this)
    }
}