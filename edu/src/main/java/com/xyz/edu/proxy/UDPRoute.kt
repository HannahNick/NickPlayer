package com.xyz.lyrics.proxy

import com.xyz.base.service.live.bean.DispatchBean
import com.xyz.engine.core.vod.route.HttpRoute

class UDPM4ARoute(dispatchBean: DispatchBean) : HttpRoute(dispatchBean) {

//    override fun getM3u8ProxyUrlForRoute(rawLink: String): String? {
////        M3u8ProxyManager.getProxyPort()
//
//        return "http://127.0.0.1:${M3u8ProxyManager.getProxyPort()}${rawLink.toUri().path}"
//
//    }

    override val tag: String
        get() = "self_cdn_with_udp"
    override val routeName: String
        get() = "UDP"
}