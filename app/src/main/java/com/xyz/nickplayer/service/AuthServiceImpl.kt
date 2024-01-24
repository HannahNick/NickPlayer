package com.xyz.nickplayer.service

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken
import com.xyz.auth.api.BaseAuthService
import com.xyz.auth.api.ILoginResult
import com.xyz.base.service.ServiceProvider
import com.xyz.base.service.auth.api.LoginRequestBodyV3
import com.xyz.base.service.auth.bean.LoginResultBean
import com.xyz.base.service.svc.RESULT
import com.xyz.base.utils.kt.localeValue
import kotlin.random.Random
import kotlin.random.nextLong

/**
 * dispatch需要登录获取到token才能使用，看视频需要登录拿token
 */
@Route(path = com.xyz.auth.api.BuildConfig.AROUTER_PATH_SERVICE_AUTH)
class AuthServiceImpl : BaseAuthService() {
    override suspend fun login(tag: String, context: Context, isUpdate: Boolean): ILoginResult {
        val result = ServiceProvider.getAuthService()
            .signInV3(
                context = context,
                action = LoginRequestBodyV3.Action.Login(),
                productCode = "edu2eng",
                fg = LoginRequestBodyV3.Fg.FOREGROUND,
                timeSeq = emptySequence()
            )
            .blockingFirst()
        return ILoginResultImpl(result)
    }

    override fun openAuthPage(context: Context, prompt: String?) {

    }

    override fun openInfoPage(context: Context) {

    }

    override fun openLoginPage(context: Context) {

    }


    class ILoginResultImpl(private val result: RESULT<LoginResultBean>) : ILoginResult {
        override val createTimeMillis: Long
            get() = result.longTime
        private val resultBean: LoginResultBean
            get() = result.result
        override val expire: Long
            get() = resultBean.expireTime.times(1000).toLong()
        override val liveDuration: Long?
            get() = resultBean.playDurationRange
                ?.filterNotNull()
                ?.takeIf {
                    it.size == 2
                }
                ?.run {
                    Random.nextLong(component1()..component2()).times(1000)
                }
        override val maxAgeMillis: Long
            get() = result.result.maxAge.toLong()
        override val productInfo: List<ILoginResult.ProductInfo>
            get() = resultBean.productModels
                ?.map { IProductInfoImpl(it) }
                .orEmpty()
        override val tbPassword: String
            get() = token
        override val tbUsername: String
            get() = resultBean.userId.toString()
        override val token: String
            get() = resultBean.token.orEmpty()
        override val userId: String
            get() = resultBean.userId.toString()
        override val userType: Int
            get() = resultBean.userType

        override fun isDolbyAudioEnabled(): Boolean {
            return resultBean.dolbyFunction == 1
        }

        override fun isGuest(): Boolean {
            return resultBean.userType != 1
        }

        override fun isLifeTime(): Boolean {
            return resultBean.userRank == "1"
        }

        override fun isPlayThumbnailEnabled(): Boolean {
            return resultBean.thumbnailFunction == 1
        }

        override fun isPlayerSwitchEnable(): Boolean {
            return resultBean.playerFunction == 1
        }

        override fun isVoiceSearchEnable(): Boolean {
            return resultBean.voiceFunction == 1
        }


        private inner class IProductInfoImpl(private val productModelsBean: LoginResultBean.ProductModelsBean) :
            ILoginResult.ProductInfo {
            override val background: String
                get() = productModelsBean.modeBgImg.orEmpty()
            override val code: String
                get() = productModelsBean.productCode.orEmpty()
            override val extra: String
                get() = productModelsBean.extData.orEmpty()
            override val logo: String
                get() = productModelsBean.modeLogo.orEmpty()
            override val title: String
                get() = productModelsBean.modelTitle
                    ?.localeValue(defaultValue = productModelsBean.modelName)
                    .orEmpty()
            override val vailDays: Int
                get() = productModelsBean.validDays
        }

    }
}