package com.nick.base.manager

import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.ZipUtils
import com.nick.base.R
import com.nick.base.http.HttpManager
import com.nick.base.model.WordLearningModel
import com.nick.base.router.BaseRouter
import com.nick.base.util.LRUFileCache
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.utils.L
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

object PlanManager {

    var mDataList: ArrayList<PlanItemBean> = ArrayList()
    var mPreInitDataCallBack: PreInitDataCallBack? = null
    var mCurrentIndex: Int = 0
    val mDownLoadFileCache by lazy { LRUFileCache(30) }

    /**
     * 自动
     */
    val mAutoFlag = true
    fun initData(context: Context,data: List<PlanItemBean>,index: Int){
        L.i("plan data: $data")
        mDataList.clear()
        mDataList.addAll(data)
        mCurrentIndex = index
        mDownLoadFileCache.initializeCache("${context.filesDir}")
    }

    fun registerDataCallBack(preInitDataCallBack: PreInitDataCallBack){
        mPreInitDataCallBack = preInitDataCallBack
    }


    fun toNextPlanItem(context: Context, index: Int = mCurrentIndex,loadingListener: LoadingListener? = null){
        if (!mAutoFlag){
            return
        }
        reportPlan(index)
        val nextIndex = index+1
        startItem(context,nextIndex,loadingListener)
    }

    fun startItem(context: Context, index: Int = mCurrentIndex ,loadingListener: LoadingListener? = null){
        mCurrentIndex = index
        if (index >= (mDataList.size) || index <0){
            L.w("dataList is last index:$index")
            ToastUtils.showLong("当前课程已学完")
            return
        }
        val data = mDataList[index]
        L.i("nextPlanData: $data")
        when(data.contentType){
            1,2->{
                toVideo(context,data.contentUrl,data.contentTitle,data.personPlanItemId,data.zip.url,index)
                mPreInitDataCallBack?.preInitDataFinish()
            }
            3->{
//                val wordLearningIntent = Intent(context, WordLearningActivity::class.java)
//                wordLearningIntent.putExtra(WordLearningActivity.ZIP_URL,data.zip.url)
//                wordLearningIntent.putExtra(WordLearningActivity.ZIP_MD5,data.zip.md5)
//                wordLearningIntent.putExtra(WordLearningActivity.PERSON_PLAN_ITEM_ID,data.personPlanItemId)
//                context.startActivity(wordLearningIntent)
                loadingListener?.showLoading()
                downZip(context,data.zip.url,data.zip.md5){ zipFile->
                    findInvokeFile(context,zipFile,data.contentUrl,index,loadingListener){file->
                        L.i("findInvokeFilePath: ${file.parent}, fileName: ${file.name}")
                        toGame(context,file.parent!!,file.name, mCurrentIndex)
                    }
                }
            }
            5->{
                toWordLearning(context,data.zip.url,data.zip.md5,data.personPlanItemId,index)
                mPreInitDataCallBack?.preInitDataFinish()
            }
            else ->{
                ToastUtils.showLong("else finish")
            }
        }
    }

    /**
     * 上报学完的任务
     */
    private fun reportPlan(index: Int){
        if (index<0){
            return
        }
        val data = mDataList[index]
        val dispose = WordLearningModel().reportStudyResult(UserManager.personPlanId,data.personPlanItemId,UserManager.personId)
            .subscribe({
                L.i("reportPlan success $data")
            },{
                it.printStackTrace()
            })
    }

    fun downZip(context: Context,url: String,md5: String,block:(file: File)->Unit){
        //1.判断压缩文件是否存在
        val zipFile = File("${context.filesDir}/zip/$md5")
        L.i("zipFilePath: $zipFile")
        if (FileUtils.isFileExists(zipFile)){
            L.i("FileExists")
            mDownLoadFileCache.cacheFile(zipFile)
            block(zipFile)
            return
        }
        val dispose = HttpManager.api.downloadFile(url)
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .subscribe({
                val writeFileFlag = FileIOUtils.writeFileFromBytesByStream("${context.filesDir}/zip/$md5", it.bytes())
                if (writeFileFlag){
                    L.i("writeFileFlag: success")
                    mDownLoadFileCache.cacheFile(zipFile)
                    block(zipFile)
                }else{
                    FileUtils.delete(zipFile)
                    L.e("writeFileFlag is fail!")
                }
            },{
                it.printStackTrace()
            })
    }

    /**
     * 解压文件，并找到对应需要执行的文件
     * zipFile:压缩文件
     * fileName:后端指定需要使用的文件
     */
    private fun findInvokeFile(context: Context, zipFile: File, fileName: String, index: Int, loadingListener: LoadingListener? = null, block:(file: File)->Unit){
        val dispose = Flowable.just(zipFile)
            .map {//判断是否已解压
                FileUtils.isFileExists("${context.filesDir.absolutePath}/plan/${zipFile.name}")
            }
            .map {
                if (it){//已经解压了就直接遍历文件返回
                    FileUtils.listFilesInDir("${context.filesDir}/plan/${FileUtils.getFileNameNoExtension(zipFile)}")
                }else{//没解压过就解压文件并遍历返回
                    ZipUtils.unzipFile(zipFile, File("${context.filesDir}/plan/${zipFile.name}"))
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                L.i("toGame")
                //将文件信息传回页面
                block.invoke(File("${context.filesDir.absolutePath}/plan/${FileUtils.getFileNameNoExtension(zipFile)}/$fileName"))
                mPreInitDataCallBack?.preInitDataFinish()
            },{
                it.printStackTrace()
            },{
                L.i("final fun")
                loadingListener?.hideLoading()
            })
    }


    fun toVideo(context: Context, videoUrl: String, titleName: String, personPlanItemId: String, zipUrl: String, itemIndex: Int){
        ARouter.getInstance().build(BaseRouter.AROUTER_VIDEOACTIVITY)
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

    fun toGame(context: Context,path:String,json:String,itemIndex: Int){
        L.i("toGame path:$path,json:$json,index:$itemIndex")
        ARouter.getInstance().build(BaseRouter.AROUTER_GAME)
            .withString("path", path)
            .withString("json", json)
            .withInt("itemIndex", itemIndex)
            .navigation(context)
    }

    fun toLogin(){
        ARouter.getInstance().build(BaseRouter.LOGIN)
            .navigation()
    }

    fun toRegister(){
        ARouter.getInstance().build(BaseRouter.REGISTER)
            .navigation()
    }

    fun toPlanSelect(){
        ARouter.getInstance().build(BaseRouter.STUDY_SELECT)
            .navigation()
    }

    fun toHome(context: Context){
        ARouter.getInstance().build(BaseRouter.HOME)
            .withTransition(R.anim.activity_gide_in_anim,R.anim.activity_gide_out_anim)
            .navigation(context)
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

    interface LoadingListener{
        fun showLoading()

        fun hideLoading()
    }
}