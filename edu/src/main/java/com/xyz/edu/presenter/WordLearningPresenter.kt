package com.xyz.edu.presenter

import android.content.Context
import android.os.Bundle
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ZipUtils
import com.google.gson.reflect.TypeToken
import com.xyz.auth.api.IAuthService
import com.xyz.base.utils.L
import com.xyz.download.api.DownloadConfig
import com.xyz.download.api.DownloadListener
import com.xyz.download.service.Utils
import com.xyz.download.service.Utils.appendDispatchStrategy
import com.xyz.download.service.Utils.appendUrlSwitchStrategy
import com.xyz.download.support.manager.DownloadManager
import com.xyz.edu.contract.IWordLearningC
import com.xyz.edu.vo.ZipDataBean
import io.reactivex.Flowable
import java.io.File

class WordLearningPresenter(context: Context, view: IWordLearningC.View, model: IWordLearningC.Model): DisposablePresenter<IWordLearningC.View,IWordLearningC.Model>(context,view, model),IWordLearningC.Presenter {

    private var mZipDownLoadHolder: DownloadManager.DownloadHolder? = null

    override fun downZip(url: String) {
        val loginResult = IAuthService.create(context)?.getLoginResult()!!
        val wrapUrl = url.appendUrlSwitchStrategy(Utils.UrlSwitchStrategy.UDP_TCP_CRO).appendDispatchStrategy(
            Utils.DispatchStrategy.VideoDispatch(
                loginResult.token,
                loginResult.productInfo.first().code
            )
        )
        mZipDownLoadHolder?.cancel()
        mZipDownLoadHolder = DownloadManager.enqueueDownload(context, wrapUrl, getZipDownLoadConfig(wrapUrl))
        mZipDownLoadHolder?.addListener(object : DownloadListener(){
            override fun onCancel(
                url: String?,
                file: File?,
                summery: List<Summery>,
                bundle: Bundle
            ) {

            }

            override fun onComplete(
                url: String?,
                file: File?,
                summery: List<Summery>,
                bundle: Bundle
            ) {
                //1.判空
                if (file!=null){
                    findReadme(file)
                }else{
                    L.e("zipFile onComplete is null!")
                }

            }

            override fun onProgress(
                url: String?,
                file: File?,
                progress: Float,
                speed: Long,
                bundle: Bundle
            ) {
            }

            override fun onStart(url: String?, file: File?, bundle: Bundle) {
            }

            override fun onError(
                url: String?,
                file: File?,
                summery: List<Summery>,
                err: String,
                errorMsg: String?,
                bundle: Bundle
            ) {
                super.onError(url, file, summery, err, errorMsg, bundle)
            }
        })
    }

    fun findReadme(zipFile: File){
        val readmeFileExtension = ".txt"
        Flowable.just(zipFile)
            .map {
                FileUtils.isFileExists("${context.filesDir.absolutePath}/plan/${FileUtils.getFileNameNoExtension(it)}")
            }
            .flatMap {
                if (it){
                    Flowable.fromIterable(FileUtils.listFilesInDir("${context.filesDir.absolutePath}/plan/${FileUtils.getFileNameNoExtension(zipFile)}"))
                }else{
                    Flowable.fromIterable(ZipUtils.unzipFile(zipFile,File("${context.filesDir.absolutePath}/plan/${FileUtils.getFileNameNoExtension(zipFile)}")))
                }
            }
            .filter {
                L.i(it.absolutePath)
                it.name.endsWith(readmeFileExtension)
            }
            .firstOrError()
            .map {
                val zipFileDescribe = FileIOUtils.readFile2String(it)
                GsonUtils.fromJson<List<ZipDataBean>>(zipFileDescribe,object :TypeToken<List<ZipDataBean>>(){}.type)
            }
            .subscribe({
                view.getZipData("${context.filesDir.absolutePath}/plan/${FileUtils.getFileNameNoExtension(zipFile)}",it)
            },{
                view.getZipFileError(it.message?:"")
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }



    }

    override fun onRelease() {
        super.onRelease()
        mZipDownLoadHolder?.cancel()
    }

    private fun getZipDownLoadConfig(url: String): DownloadConfig {
        val dirPath = "${context.filesDir}/zip"
        return DownloadConfig(
            downloadDir = dirPath,
            skipIfAlreadyCompleted = false
        )
    }
}