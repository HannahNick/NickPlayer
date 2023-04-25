package com.nick.base.http

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.nick.base.BaseUrl
import com.nick.base.BuildConfig
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object HttpManager {

    //----------------------------图片相关请求------------------------------
    private val sslContext = SSLContext.getInstance("TLSv1.2").apply {
        init(null, trustAllCerts, SecureRandom())
    }

    private val sslSocketFactory = sslContext.socketFactory
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })
    private val connectionPool = ConnectionPool(5, 5, TimeUnit.MINUTES)

    val mClient: OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(
            LoggingInterceptor.Builder()
                .setLevel(if (BuildConfig.DEBUG) Level.BASIC else Level.NONE)
                .log(Platform.INFO)
//                .addHeader("abc","123")
                .build()
        )
        .connectionPool(connectionPool)
        .sslSocketFactory(sslSocketFactory,trustAllCerts[0] as X509TrustManager)
        .connectTimeout(35, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .build()


    private val mRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BaseUrl.url)
        .client(mClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    /**
     * 网络接口
     */
    val api: Api = mRetrofit.create (Api::class.java)


}