package com.xyz.edu.presenter

import android.content.Context
import android.os.Bundle
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ZipUtils
import com.google.gson.reflect.TypeToken
import com.xyz.auth.api.IAuthService
import com.xyz.base.app.rx.io2Main
import com.xyz.base.utils.L
import com.xyz.download.api.DownloadConfig
import com.xyz.download.api.DownloadListener
import com.xyz.download.service.Utils
import com.xyz.download.service.Utils.appendDispatchStrategy
import com.xyz.download.service.Utils.appendUrlSwitchStrategy
import com.xyz.download.support.manager.DownloadManager
import com.xyz.edu.contract.IWordLearningC
import com.xyz.edu.manager.UserManager
import com.xyz.edu.presenter.base.DisposablePresenter
import com.xyz.edu.vo.ZipDataVo
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

class WordLearningPresenter(context: Context, view: IWordLearningC.View, model: IWordLearningC.Model): DisposablePresenter<IWordLearningC.View, IWordLearningC.Model>(context,view, model),IWordLearningC.Presenter {

    private var mZipDownLoadHolder: DownloadManager.DownloadHolder? = null


    override fun downZip(url: String,md5: String) {
        //1.判断压缩文件是否存在
        val zipFile = File("${context.filesDir}/zip/PaperPig.zip")
//        val zipFile = File("${context.filesDir}/zip/$md5")
        if (FileUtils.isFileExists(zipFile)){
            findReadme(zipFile)
            return
        }
        //2.不存在就去下载
        val loginResult = IAuthService.create(context)?.getLoginResult()!!
        val wrapUrl = url.appendUrlSwitchStrategy(Utils.UrlSwitchStrategy.UDP_TCP_CRO).appendDispatchStrategy(
            Utils.DispatchStrategy.VideoDispatch(
                loginResult.token,
                loginResult.productInfo.first().code
            )
        )
        mZipDownLoadHolder?.cancel()
        mZipDownLoadHolder = DownloadManager.enqueueDownload(context, wrapUrl, getZipDownLoadConfig(md5))
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
                view.downLoadProgress(progress)
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

    override fun reportStudyResult(personPlanItemId: Int) {
        model.reportStudyResult(UserManager.personPlanId,personPlanItemId,UserManager.personId)
            .io2Main()
            .subscribe({},{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }

    fun findReadme(zipFile: File){
        L.i("0:${Thread.currentThread().name}")
        val readmeFileName = "readme"
        Flowable.just(zipFile)
            .map {//判断是否已解压
                L.i("1:${Thread.currentThread().name}")
                FileUtils.isFileExists("${context.filesDir.absolutePath}/plan/${FileUtils.getFileNameNoExtension(it)}")
            }
            .flatMap {
                L.i("2:${Thread.currentThread().name}")
                if (it){//已经解压了就直接遍历文件返回
                    Flowable.fromIterable(FileUtils.listFilesInDir("${context.filesDir.absolutePath}/plan/${FileUtils.getFileNameNoExtension(zipFile)}"))
                }else{//没解压过就解压文件并遍历返回
                    Flowable.fromIterable(ZipUtils.unzipFile(zipFile,File("${context.filesDir.absolutePath}/plan/")))
                }
            }
            .filter {
                L.i("3:${Thread.currentThread().name}")
                L.i(it.absolutePath)
                //找到readme文件
                it.name.contains(readmeFileName)
            }
            .firstOrError()
            .map {
                L.i("4:${Thread.currentThread().name}")
                //解析txt文件，转化文件信息List
                val zipFileDescribe = FileIOUtils.readFile2String(it)
                L.i(zipFileDescribe)
                val zipDataList = GsonUtils.fromJson<List<ZipDataVo>>(zipFileDescribe,object :TypeToken<List<ZipDataVo>>(){}.type)
                //将没有音频的数据过滤
                zipDataList.filter { zipBataBean->
                    zipBataBean.audiio.isNotEmpty()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                L.i("5:${Thread.currentThread().name}")
                //将文件信息传回页面
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

    private fun getZipDownLoadConfig(md5: String): DownloadConfig {
        val dirPath = "${context.filesDir}/zip"
        return DownloadConfig(
            downloadDir = dirPath,
            filename = md5,
            fileMd5 = md5,
            skipIfAlreadyCompleted = false
        )
    }
}