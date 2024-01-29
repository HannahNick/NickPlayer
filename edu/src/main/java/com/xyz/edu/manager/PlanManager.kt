package com.xyz.edu.manager

import android.content.Context
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.ZipUtils
import com.nick.base.http.HttpManager
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.utils.L
import com.xyz.edu.ui.VideoActivity
import com.xyz.edu.ui.WordLearningActivity
import com.xyz.game.GameStart
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

object PlanManager {

    var mDataList: ArrayList<PlanItemBean> = ArrayList()

    /**
     * 自动
     */
    val mAutoFlag = false
    fun initData(data: List<PlanItemBean>){
        mDataList.addAll(data)
    }

    fun toNextPlanItem(context: Context, index: Int){
        if (!mAutoFlag){
            return
        }

        if (index >= (mDataList.size)){
            L.w("dataList is last index:$index")
            ToastUtils.showLong("当前课程已学完")
            return
        }
        val data = mDataList[index]
        L.i("nextPlanData: $data")
        when(data.contentType){
            1,2->{
                VideoActivity.start(context,data.contentUrl,data.contentTitle,data.personPlanItemId,index)
            }
            3->{
//                val wordLearningIntent = Intent(context, WordLearningActivity::class.java)
//                wordLearningIntent.putExtra(WordLearningActivity.ZIP_URL,data.zip.url)
//                wordLearningIntent.putExtra(WordLearningActivity.ZIP_MD5,data.zip.md5)
//                wordLearningIntent.putExtra(WordLearningActivity.PERSON_PLAN_ITEM_ID,data.personPlanItemId)
//                context.startActivity(wordLearningIntent)
                downZip(context,data.zip.url,data.zip.md5,data.contentUrl)
            }
            5->{
                WordLearningActivity.start(context,data.zip.url,data.zip.md5,data.personPlanItemId,index)
            }
            else ->{
                ToastUtils.showLong("else finish")
            }
        }

    }

    fun downZip(context: Context,url: String,md5: String,gameJson: String) {
        //1.判断压缩文件是否存在
        val zipFile = File("${context.filesDir}/zip/$md5")
        L.i("zipFilePath: $zipFile")
//        val zipFile = File("${context.filesDir}/zip/$md5")
        if (FileUtils.isFileExists(zipFile)){
            L.i("FileExists")
            findGameJson(context,zipFile,gameJson)
            return
        }
        //2.不存在就去下载
//        val wrapUrl = url.appendUrlSwitchStrategy(Utils.UrlSwitchStrategy.TCP_THAN_UDP).appendDispatchStrategy(
//            Utils.DispatchStrategy.VideoDispatch(
//                UserManager.token,
//                UserManager.productCode
//            )
//        )
        val dispose = HttpManager.api.downloadFile(url)
            .subscribeOn(Schedulers.io())
            .subscribe({
                val writeFileFlag = FileIOUtils.writeFileFromBytesByStream("${context.filesDir}/zip/$md5", it.bytes())
                if (writeFileFlag){
                    L.i("writeFileFlag: success")
                    findGameJson(context,File("${context.filesDir}/zip/$md5"),gameJson)
                }else{
                    FileUtils.delete("${context.filesDir}/zip/$md5")
                    L.e("writeFileFlag is fail!")
                }
            },{
                it.printStackTrace()
            })
    }

    fun findGameJson(context: Context, zipFile: File, gameJson: String){
        val dispose = Flowable.just(zipFile)
            .map {//判断是否已解压
                FileUtils.isFileExists("${context.filesDir.absolutePath}/plan/${zipFile.name}")
            }
            .flatMap {
                if (it){//已经解压了就直接遍历文件返回
                    Flowable.fromIterable(FileUtils.listFilesInDir("${context.filesDir}/plan/${FileUtils.getFileNameNoExtension(zipFile)}"))
                }else{//没解压过就解压文件并遍历返回
                    Flowable.fromIterable(
                        ZipUtils.unzipFile(zipFile,
                            File("${context.filesDir}/plan/${zipFile.name}")
                        ))
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                //将文件信息传回页面
                GameStart(context,"${context.filesDir.absolutePath}/plan/${FileUtils.getFileNameNoExtension(zipFile)}",gameJson)
            },{
                it.printStackTrace()
            })
    }

}