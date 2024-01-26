package com.nick.base.http

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.nick.base.BaseUrl
import com.nick.base.BuildConfig
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object HttpManager {


    private val connectionPool = ConnectionPool(5, 5, TimeUnit.MINUTES)
    private val mClient: OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(
            LoggingInterceptor.Builder()
                .setLevel(if (BuildConfig.DEBUG) Level.BASIC else Level.NONE)
                .log(Platform.INFO)
//                .addHeader("abc","123")
                .build()
        )
        .connectionPool(connectionPool)
        .connectTimeout(35, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .build()
    private val mRetrofit = Retrofit.Builder()
        .baseUrl(BaseUrl.url)
        .client(mClient)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    /**
     * 网络接口
     */
    val api: Api = mRetrofit.create (Api::class.java)


}