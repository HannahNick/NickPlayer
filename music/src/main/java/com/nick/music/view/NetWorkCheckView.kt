package com.nick.music.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R
import com.xyz.base.utils.NetworkUtils

class NetWorkCheckView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
):AppCompatImageView(context, attrs, defStyleAttr){

    private val mReceiver by lazy {  NetworkChangeReceiver()}

    init {
        setImageResource(R.drawable.network_error)
        background = context.resources.getDrawable(R.drawable.bg_network_check,null)
        visibility = if (NetworkUtils.isNetworkAvailable(context)){
            View.GONE
        }else{
            View.VISIBLE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        mReceiver.netWorkChangeListener = object : NetWorkChangeListener{
            override fun netWorkChange(netWorkCanUse: Boolean) {
                visibility = if (netWorkCanUse){
                    View.GONE
                }else{
                    View.VISIBLE
                }
            }
        }
        context.registerReceiver(mReceiver,intentFilter)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mReceiver.netWorkChangeListener = null
        context.unregisterReceiver(mReceiver)
    }


    class NetworkChangeReceiver() : BroadcastReceiver() {
        var netWorkChangeListener: NetWorkChangeListener? = null
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.apply {
                netWorkChangeListener?.netWorkChange(NetworkUtils.isNetworkAvailable(context))
            }
        }
    }

    interface NetWorkChangeListener{
        fun netWorkChange(netWorkCanUse: Boolean)
    }
}