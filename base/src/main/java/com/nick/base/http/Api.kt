package com.nick.base.http

import com.nick.base.vo.MusicVo
import com.nick.base.vo.base.BaseVo
import retrofit2.http.*

interface Api {

    @GET("musicPlayer/music/getAllMusic")
    suspend fun getAllMusic(): BaseVo<List<MusicVo>>

}