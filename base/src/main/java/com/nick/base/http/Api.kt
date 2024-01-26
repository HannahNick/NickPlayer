package com.nick.base.http

import io.reactivex.Flowable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url


interface Api {
    @GET
    fun downloadFile(@Url fileUrl: String): Flowable<ResponseBody>

}