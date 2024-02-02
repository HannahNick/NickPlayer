package com.enick.base.util

import com.blankj.utilcode.util.ToastUtils

object TimeUtil {

    private var mExitTime = 0L

    fun confirmClick(confirmDoBlock: ()-> Unit ){
        if (System.currentTimeMillis() - mExitTime > 2000){
            mExitTime = System.currentTimeMillis()
            ToastUtils.showLong("再按一次退出")
        }else{
            confirmDoBlock.invoke()
        }
    }
}