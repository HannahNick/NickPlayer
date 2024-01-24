package com.xyz.nickplayer.app

import android.app.Application
import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.DeviceUtils
import com.xyz.base.utils.DeviceUtil
import com.xyz.base.utils.DeviceUtilImpl
import com.xyz.base.utils.EncodeUtil
import com.xyz.proxy.KtvPlayProxyManager

class MyApplication: Application() {

    init {
        //搜索svcsec查看参数是否完整，这个会直接影响解密库是否能够成功解密
        /**
         * params >>>
         * context: com.xyz.nickplayer.app.MyApplication@fb82189
         * mac: ec:f7:2b:f7:8f:4b
         * sn: 370b030400000000263120440e0056808bdab3
         * tag: 3a52e4dd024584a7|HUAWEI_HUAWEI_ALP-AL00_HWALP_ALP|HWALP_user_10_10.0.0.175C00_1595396463000|29
         */
        DeviceUtil.setImpl(object : DeviceUtilImpl() {
            override val mac: String
                get() = "ec:f7:2b:f7:8f:4b"

            override fun getXyzSn(context: Context?): String {
                return "370b030400000000263120440e0056808bdab3"
            }
        })
    }

    override fun onCreate() {
        super.onCreate()
        KtvPlayProxyManager.init(this)
        ARouter.init(this)
    }
}