package com.xyz.edu.presenter

import android.content.Context
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ZipUtils
import com.google.gson.reflect.TypeToken
import com.nick.base.http.HttpManager
import com.xyz.base.app.rx.io2Main
import com.xyz.base.utils.L
import com.xyz.download.api.DownloadConfig
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
        val zipFile = File("${context.filesDir}/zip/$md5")
        L.i("zipFilePath: $zipFile")
//        val zipFile = File("${context.filesDir}/zip/$md5")
        if (FileUtils.isFileExists(zipFile)){
            L.i("FileExists")
            findReadme(zipFile)
            return
        }
        //2.不存在就去下载
//        val wrapUrl = url.appendUrlSwitchStrategy(Utils.UrlSwitchStrategy.TCP_THAN_UDP).appendDispatchStrategy(
//            Utils.DispatchStrategy.VideoDispatch(
//                UserManager.token,
//                UserManager.productCode
//            )
//        )
        HttpManager.api.downloadFile(url)
            .subscribeOn(Schedulers.io())
            .subscribe({
                val writeFileFlag = FileIOUtils.writeFileFromBytesByStream("${context.filesDir}/zip/$md5", it.bytes())
                if (writeFileFlag){
                    L.i("writeFileFlag: success")
                    findReadme(File("${context.filesDir}/zip/$md5"))
                }else{
                    FileUtils.delete("${context.filesDir}/zip/$md5")
                    L.e("writeFileFlag is fail!")
                }
            },{
                it.printStackTrace()
            }).apply {
                compositeDisposable.add(this)
            }
    }

    override fun reportStudyResult(personPlanItemId: String) {
        model.reportStudyResult(UserManager.personPlanId,personPlanItemId,UserManager.personId)
            .io2Main()
            .subscribe({},{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }

    fun findReadme(zipFile: File){
        val readmeFileName = "readme"
        Flowable.just(zipFile)
            .map {//判断是否已解压
                FileUtils.isFileExists("${context.filesDir.absolutePath}/plan/${zipFile.name}")
            }
            .flatMap {
                if (it){//已经解压了就直接遍历文件返回
                    Flowable.fromIterable(FileUtils.listFilesInDir("${context.filesDir}/plan/${FileUtils.getFileNameNoExtension(zipFile)}"))
                }else{//没解压过就解压文件并遍历返回
                    Flowable.fromIterable(ZipUtils.unzipFile(zipFile,File("${context.filesDir}/plan/${zipFile.name}")))
                }
            }
            .filter {
                L.i(it.absolutePath)
                //找到readme文件
                it.name.contains(readmeFileName)
            }
            .firstOrError()
            .map {
                //解析txt文件，转化文件信息List
                val zipFileDescribe = FileIOUtils.readFile2String(it)
                L.i(zipFileDescribe)
                val zipDataList = GsonUtils.fromJson<List<ZipDataVo>>(zipFileDescribe,object :TypeToken<List<ZipDataVo>>(){}.type)
                //将没有音频的数据过滤
                zipDataList.filter { zipBataBean->
                    zipBataBean.audio.isNotEmpty()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
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