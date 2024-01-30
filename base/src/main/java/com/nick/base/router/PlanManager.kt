package com.nick.base.router

import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.ZipUtils
import com.nick.base.http.HttpManager
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.utils.L
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

object PlanManager {

    var mDataList: ArrayList<PlanItemBean> = ArrayList()
    var mPreInitDataCallBack: PreInitDataCallBack? = null

    /**
     * 自动
     */
    val mAutoFlag = true
    fun initData(data: List<PlanItemBean>){
        mDataList.addAll(data)
    }

    fun registerDataCallBack(preInitDataCallBack: PreInitDataCallBack){
        mPreInitDataCallBack = preInitDataCallBack
    }

    fun toNextPlanItem(context: Context, index: Int){
        if (!mAutoFlag){
            return
        }
        val nextIndex = index+1

        if (nextIndex >= (mDataList.size)){
            L.w("dataList is last index:$nextIndex")
            ToastUtils.showLong("当前课程已学完")
            return
        }
        val data = mDataList[nextIndex]
        L.i("nextPlanData: $data")
        when(data.contentType){
            1,2->{
                toVideo(context,data.contentUrl,data.contentTitle,data.personPlanItemId,nextIndex)
                mPreInitDataCallBack?.preInitDataFinish()
            }
            3->{
//                val wordLearningIntent = Intent(context, WordLearningActivity::class.java)
//                wordLearningIntent.putExtra(WordLearningActivity.ZIP_URL,data.zip.url)
//                wordLearningIntent.putExtra(WordLearningActivity.ZIP_MD5,data.zip.md5)
//                wordLearningIntent.putExtra(WordLearningActivity.PERSON_PLAN_ITEM_ID,data.personPlanItemId)
//                context.startActivity(wordLearningIntent)
                downZip(context,data.zip.url,data.zip.md5,data.contentUrl,nextIndex)
            }
            5->{
                toWordLearning(context,data.zip.url,data.zip.md5,data.personPlanItemId,nextIndex)
                mPreInitDataCallBack?.preInitDataFinish()
            }
            else ->{
                ToastUtils.showLong("else finish")
            }
        }
    }

    fun downZip(context: Context,url: String,md5: String,gameJson: String,index: Int) {
        //1.判断压缩文件是否存在
        val zipFile = File("${context.filesDir}/zip/$md5")
        L.i("zipFilePath: $zipFile")
//        val zipFile = File("${context.filesDir}/zip/$md5")
        if (FileUtils.isFileExists(zipFile)){
            L.i("FileExists")
            findGameJson(context,zipFile,gameJson,index)
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
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .subscribe({
                val writeFileFlag = FileIOUtils.writeFileFromBytesByStream("${context.filesDir}/zip/$md5", it.bytes())
                if (writeFileFlag){
                    L.i("writeFileFlag: success")
                    findGameJson(context,File("${context.filesDir}/zip/$md5"),gameJson,index)
                }else{
                    FileUtils.delete("${context.filesDir}/zip/$md5")
                    L.e("writeFileFlag is fail!")
                }
            },{
                it.printStackTrace()
            })
    }

    fun findGameJson(context: Context, zipFile: File, gameJson: String,index: Int){
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
                toGame("${context.filesDir.absolutePath}/plan/${FileUtils.getFileNameNoExtension(zipFile)}",gameJson,index)
                mPreInitDataCallBack?.preInitDataFinish()
            },{
                it.printStackTrace()
            })
    }


    fun toVideo(context: Context, url: String, titleName: String, personPlanItemId: String, itemIndex: Int){
        ARouter.getInstance().build(BaseRouter.AROUTER_VIDEOACTIVITY)
            .withString("url", url)
            .withString("titleName", titleName)
            .withString("personPlanItemId", personPlanItemId)
            .withInt("itemIndex", itemIndex)
            .navigation()
    }

    fun toWordLearning(context: Context, url: String, md5: String, personPlanItemId: String, itemIndex: Int){
        ARouter.getInstance().build(BaseRouter.AROUTER_WORDLEARNINGACTIVITY)
            .withString("url", url)
            .withString("md5", md5)
            .withString("personPlanItemId", personPlanItemId)
            .withInt("itemIndex", itemIndex)
            .navigation()
    }

    fun toGame(path:String,json:String,itemIndex: Int){
        ARouter.getInstance().build(BaseRouter.AROUTER_GAME)
            .withString("path", path)
            .withString("json", json)
            .withInt("itemIndex", itemIndex)
            .navigation()
    }

    /**
     * 这个回调是为了在当前页面提前加载下一个页面需要的数据做个缓冲，
     */
    interface PreInitDataCallBack{
        /**
         * 数据预初始化完成后通知调用端关闭页面完成跳转
         */
        fun preInitDataFinish()
    }
}