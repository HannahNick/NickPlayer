package com.xyz.lyrics.proxy

import android.content.Context
import android.net.Uri
import android.os.Build
import com.xyz.base.service.ServiceProvider
import com.xyz.base.service.auth.api.ServerTimeHolder
import com.xyz.base.service.live.bean.DispatchBean
import com.xyz.base.service.svc.RESULT
import com.xyz.base.service.svc.SvcSecEncryptRequestBody
import com.xyz.base.utils.DeviceUtil
import com.xyz.base.utils.EncodeUtil
import com.xyz.base.utils.L
import com.xyz.base.utils.isIp
import com.xyz.base.utils.kt.removeBy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import org.reactivestreams.Subscriber
import java.util.concurrent.ConcurrentHashMap

object DispatchM4aResultCacheRx {

    private var mSn: String? = null
    private val sn: String get() = mSn ?: DeviceUtil.getXyzSn()

    private val HOLDERS: ConcurrentHashMap<String, TokenFlowable> = ConcurrentHashMap()

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val mMusicService by lazy {
        ServiceProvider.getMusicService("http://206.221.186.114:9076")
    }

    fun init(context: Context) {
        mSn = DeviceUtil.getXyzSn(context)
    }

    private fun create(
        url: String,
        token: String,
        productCode: String? = null
    ): Flowable<RESULT<DispatchBean>> {
        return mMusicService.audDispatch(
            productCode = productCode ?: "",
            deviceModel = Build.MODEL,
            isp = "",
            body = SvcSecEncryptRequestBody(
                "url" to EncodeUtil.base64Encode(url),
                "m" to Build.MODEL,
                "ip" to "",
                "cty" to "",
                "ctry" to "",
                "ctry" to "",
                "token" to token,
            )
        ).doOnSubscribe {
            L.i("Load final url from >>> Network request, $url")
        }.doOnNext { result ->
            result.result?.hosts?.removeBy { hostsBean -> hostsBean.hosts.isNullOrBlank() }
            HOLDERS[url] = TokenFlowable(result)
            L.d("Update source >>> $url -> ${result.displayStr()}")
        }


    }


    private fun String.replaced(): String {
        return replace(Regex("[^A-Za-z0-9 ]"), "")
            .replace(Regex(" +"), "_")
            .uppercase()
    }

    fun rxDispatchBean2(
        url: String,
        token: String,
        productCode: String? = null,
        expiredTime: Long = Long.MAX_VALUE
    ): Flowable<DispatchBean> {
        val processor = try {
            val tokenFlowable = HOLDERS[url]
            when {
                tokenFlowable?.isExpired(expiredTime) == false -> {
                    L.i("Load final url from >>> TokenHolder, $url")
                    tokenFlowable
                }

                else -> {
                    create(url, token, productCode)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            create(url, token)
        }

        return processor
            .switchIfEmpty(create(url, token))
            .onErrorResumeNext(create(url, token))
            .flatMap {
                L.i("Get final url >>> $url -> ${it.displayStr()} ")
                Flowable.just(it.result)
            }
    }

    fun rxDispatchResult2(
        url: String,
        token: String,
        productCode: String? = null,
        expiredTime: Long = Long.MAX_VALUE
    ): Flowable<List<String>> {
        return rxDispatchBean2(url, token, productCode, expiredTime)
            .map { result ->
                val tmpUri = Uri.parse(result.url)
                val builder = tmpUri.buildUpon()
                val list = result.hosts.map {
                    Triple(it.hosts, it.scheme, it.port)
                } + result.domain.map {
                    Triple(it.domain, it.scheme, it.port)
                }

                list.map { (host: String, scheme: String?, port: Int) ->
                    builder
                        .encodedAuthority(
                            if (host.isIp()) {
                                "$host:$port"
                            } else {
                                host
                            }
                        )
                        .scheme(
                            when {
                                scheme != null -> scheme
                                host.isIp() -> "http"
                                else -> "https"
                            }
                        )
                        .build()
                        .toString()
                }
            }
    }

    fun rxDispatchBean(
        url: String,
        token: String,
        productCode: String? = null
    ): Flowable<DispatchBean> {
        return rxDispatchBean2(url, token, productCode)
    }

    fun rxDispatchResult(
        url: String,
        token: String,
        productCode: String? = null
    ): Flowable<List<String>> {
        return rxDispatchResult2(url, token, productCode)
    }

    fun releasePreload() {
        val disposable = compositeDisposable
        disposable.dispose()
        compositeDisposable = CompositeDisposable()
    }

    private class TokenFlowable(private val result: RESULT<DispatchBean>) :
        Flowable<RESULT<DispatchBean>>() {

        fun isExpired(expiredTime: Long = Long.MAX_VALUE): Boolean {
            return try {
                result.result?.hosts?.isNotEmpty() != true ||
                        ServerTimeHolder.isVodUrlExpired(result.result.url, expiredTime)
            } catch (e: Exception) {
                true
            }.apply {
                "TokenFlowable[${this@TokenFlowable.result.result.url}] is expired >>> $this".let { log ->
                    if (this@apply) {
                        L.e(log)
                    } else {
                        L.i(log)
                    }
                }
            }
        }

        override fun subscribeActual(s: Subscriber<in RESULT<DispatchBean>>?) {
            s?.onNext(result)
            s?.onComplete()
        }

    }

    private fun RESULT<DispatchBean>.displayStr(): String {
        return "${(this.result.hosts.map { "[${it.scheme}]${it.hosts}" } + this.result.domain.map { "[${it.scheme}]${it.domain}" }).joinToString()} | ${this.result.url}"
    }

}