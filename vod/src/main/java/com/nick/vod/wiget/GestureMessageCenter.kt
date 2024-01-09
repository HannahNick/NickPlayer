package com.nick.vod.wiget

import com.nick.vod.view.LiveGestureControlLayer

object GestureMessageCenter {

    private val callBackList: HashSet<LiveGestureControlLayer.GestureCallBack> = HashSet()

    fun registerCallBack(callBack: LiveGestureControlLayer.GestureCallBack){
        callBackList.add(callBack)
    }

    fun removeCallBack(callBack: LiveGestureControlLayer.GestureCallBack){
        if (callBackList.contains(callBack)){
            callBackList.remove(callBack)
        }
    }

    fun sendFullScreen(){
        callBackList.forEach {
            it.fullScreen()
        }
    }

    fun sendBack(){
        callBackList.forEach {
            it.back()
        }
    }

    fun seek(position: Int){
        callBackList.forEach{
            it.seek(position)
        }
    }
}