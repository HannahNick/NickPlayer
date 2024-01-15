package com.xyz.proxy

import android.annotation.SuppressLint
import android.content.Context
import com.xyz.base.app.yesapi.YesApi
import com.xyz.base.utils.L
import com.xyz.proxy.ktv.KtvDispatcher
import com.xyz.proxy.ktv.KtvFetcherFactory
import com.xyz.proxy.ktv.KtvM4aDispatcher
import com.xyz.proxy.ktv.KtvM4aFetcherFactory
import com.xyz.proxy.ktv.KtvM4aRequestInfoParser
import com.xyz.proxy.ktv.newKtvM4aProxy
import com.xyz.proxy.ktv.newKtvProxy
import com.xyz.proxy.okhttp.GlobalOkHttpClient
import com.xyz.proxy.okproxy.FetcherInterceptor
import com.xyz.proxy.okproxy.OkHttpProxy
import com.xyz.proxy.udp.UdpProxyManager
import com.xyz.proxy.vod.HeaderInterceptor
import com.xyz.proxy.vod.TestSrcInterceptor
import com.xyz.proxy.vod.VmHlsInterceptor
import com.xyz.proxy.vod.VodRequestInfoParser

@SuppressLint("StaticFieldLeak")
object KtvPlayProxyManager {

    private lateinit var m4aProxy: IProxy
    private lateinit var ktvProxy: IProxy
    private lateinit var ktvCacheProxy: IProxy

    private lateinit var context: Context
    val GlobalOkHttpClient = YesApi.newDefaultOkHttpClientBuilder().build()
    private val ktvFetcherFactory: KtvFetcherFactory by lazy {
        KtvFetcherFactory(
            parser = VodRequestInfoParser(),
            dispatcher = KtvDispatcher(context),
            udpProxyManager = UdpProxyManager()
        )
    }

    private val ktvM4aFetcherFactory: KtvM4aFetcherFactory by lazy {
        KtvM4aFetcherFactory(
            parser = KtvM4aRequestInfoParser(),
            dispatcher = KtvM4aDispatcher(context),
            udpProxyManager = UdpProxyManager()
        )

    }


    fun init(context: Context) {
        this.context = context
    }


    public fun createM4aProxy(context: Context): IProxy {
        val proxy = newKtvM4aProxy(context)

        return object : IProxy {
            override fun stop() {
                L.i("M4aProxy   proxy isStart ${proxy.isStarted()} bf stop ")
                if (proxy.isStarted()) {
                    proxy.stop()
                }
                L.i("M4aProxy   stop ")
                L.i("M4aProxy   proxy isStart ${proxy.isStarted()} ")
            }

            override fun start() {
                proxy.start()
                L.i("M4aProxy   start ")
            }

            override fun buildProxyUrl(url: String): String {
                return proxy.buildProxyUrl(url).apply {
                    L.i("M4aProxy  $url  buildProxyUrl $this")
                }
            }
        }
    }


    fun createKtvProxy(context: Context): IProxy {
        val proxy = newKtvProxy(context)

        return object : IProxy {
            override fun stop() {

                L.i("KtvProxy   proxy isStart ${proxy.isStarted()} bf stop ")
                if (proxy.isStarted()) {
                    proxy.stop()
                }
                L.i("KtvProxy   stop ")

                L.i("KtvProxy   proxy isStart ${proxy.isStarted()} ")
            }

            override fun start() {
                proxy.start()
                L.i("KtvProxy   start ")
            }

            override fun buildProxyUrl(url: String): String {
                return proxy.buildProxyUrl(url).apply {
                    L.i("KtvProxy  $url  buildProxyUrl  $this")
                }
            }
        }
    }


    fun newKtvProxy(context: Context): OkHttpProxy {
        return OkHttpProxy("Ktv_proxy", GlobalOkHttpClient)
            .apply {
                addInterceptor(
//                AddVodSrcInterceptor(),
//                TestSrcInterceptor(),   // 测试源请求拦截器，用于测试源请求是否可用
                    HeaderInterceptor(),
                    VmHlsInterceptor(),
                    FetcherInterceptor(
                        context = context,
                        factory = ktvFetcherFactory
                    )
                )
            }
    }


    fun newKtvM4aProxy(context: Context): OkHttpProxy {
        return OkHttpProxy("Ktv_m4a_proxy", GlobalOkHttpClient)
            .apply {
                addInterceptor(
                    TestSrcInterceptor(),   // 测试源请求拦截器，用于测试源请求是否可用
                    HeaderInterceptor(),
                    VmHlsInterceptor(),
                    FetcherInterceptor(
                        context = context,
                        factory = ktvM4aFetcherFactory
                    )
                )
            }
    }

}