package com.xyz.lyrics.proxy

import android.content.Context
import android.net.Uri
import com.xyz.base.app.rx.await
import com.xyz.base.app.rx.io2Main
import com.xyz.base.utils.L
import com.xyz.base.utils.XIterator
import com.xyz.engine.core.vod.route.HttpRoute
import com.xyz.engine.core.vod.route.Route
import com.xyz.luban.proxy.Src
import com.xyz.luban.proxy.vod.HostConnectivityManager
import com.xyz.luban.proxy.vod.M3u8ProxyManager
import com.xyz.luban.proxy.vod.Strategy
import com.xyz.luban.proxy.vod.udp.UdpProxy
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class KtvProxy(context: Context) {


    val udpProxy: UdpProxy by lazy {
        UdpProxy(context = context, tag = "m4a")
    }
    private val mNextIndexFinder = NextIndexFinder()
    fun start() {
//        udpProxy.start()
    }

    var route: HttpRoute? = null

    fun stop() {
        udpProxy.stop()
        route=null
    }

    suspend fun getProxyUrl(url: String, token: String): String {
        L.i("getProxyUrl $url $token")
        L.i("route $route")
        return suspendCancellableCoroutine { continuation ->
            if (route == null) {
                DispatchM4aResultCacheRx.rxDispatchBean(url, token)
                    .map {
                        L.i("dispatch  $it")
                        UDPM4ARoute(it)
                    }.subscribe ({
                        route = it


                        val proxyUrl = route!!.next(mNextIndexFinder)!!.srcUrl.let {
                            L.i("Url $it")
                            Uri.parse(it).buildUpon().let {

                                it.build().toString()
                            }
                        }
                        try {
                            L.i("proxyUrl $proxyUrl")
                            udpProxy.start(proxyUrl, false, object : UdpProxy.Callback {
                                override fun onGotProxyUrl(proxyUrl: String) {
                                    continuation.resume(value = proxyUrl) {
                                        continuation.resumeWithException(it)
                                    }
                                }
                            })
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    },{
                        it.printStackTrace()
                    })
            }else{
                val proxyUrl = route!!.next(mNextIndexFinder)!!.srcUrl.let {
                    L.i("Url $it")
                    Uri.parse(it).buildUpon().let {
                        it.appendQueryParameter("mergeTs", "true")
                        it.build().toString()
                    }
                }
                try {
                    udpProxy.start(proxyUrl, false, object : UdpProxy.Callback {
                        override fun onGotProxyUrl(proxyUrl: String) {
                            continuation.resume(value = proxyUrl) {
                                continuation.resumeWithException(it)
                            }
                        }
                    })
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }


    private inner class NextIndexFinder : XIterator.NextIndexFinder<Route.Entry> {
        override fun find(list: List<Route.Entry>, nowIndex: Int): Int {
            /*
            根据 HostConnectivityManager 判断IP是否可用，不可用则接着切换，直至全部切完
            当切完一遍后全部ip不可用，则默认取第1个ip
             */
            return list.apply {

                L.i("host ${
                    this.joinToString("-") {
                        it.host
                    }
                }")
            }.withIndex().indexOfFirst {
//                        L.i("mBadEntries.contains(it.value) ${mBadEntries.contains(it.value)}")
//                        L.i("HostConnectivityManager.isConnectable(it.value.host) ${HostConnectivityManager.isConnectable(it.value.host)}")
//                        L.i("${it.value.host} is mBadEntries ${mBadEntries.contains(it.value)}   isConnectable ${  HostConnectivityManager.isConnectable(it.value.host)}")
                it.index > nowIndex && HostConnectivityManager.isConnectable(
                    it.value.host
                )
            }.let {
                L.d("index of first >>> $it")
                if (it < 0) {
                    0
                } else {
                    it
                }
            }.apply {
                L.d("next index >>> ${list.size}, $nowIndex -> $this")
            }
        }
    }
}