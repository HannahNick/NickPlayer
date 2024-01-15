package com.xyz.proxy

interface IProxy {
    fun stop()
    fun start()
    fun buildProxyUrl(url: String): String
}