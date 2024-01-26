package com.xyz.edu.ui

import android.os.Bundle
import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayerControl
import com.nick.music.player.impl.NickExoPlayer
import com.xyz.base.utils.L
import com.xyz.edu.R
import com.xyz.edu.contract.IWordLearningC
import com.xyz.edu.databinding.ActivityWordLearningBinding
import com.xyz.edu.model.WordLearningModel
import com.xyz.edu.presenter.WordLearningPresenter
import com.xyz.edu.ui.adapter.WordLearningWindowAdapter
import com.xyz.edu.util.DialogUtil
import com.xyz.edu.vo.ZipDataVo

/**
 * 单词学习
 */
class WordLearningActivity : BaseActivity<IWordLearningC.Presenter>(),IWordLearningC.View, PlayInfoCallBack {

    private val mBinding by lazy { ActivityWordLearningBinding.inflate(layoutInflater) }
    private val mDialog by lazy { DialogUtil.getCustomDialog(this, R.layout.layout_word_learning_window,true, Gravity.START) }
    private lateinit var mWindowAdapter: WordLearningWindowAdapter
    private val mAudioPlayer: PlayerControl by lazy { NickExoPlayer(this) }
    private lateinit var mImgList:List<String>
    private lateinit var mTextList:List<String>
    private var mPersonPlanItemId: String = ""

    companion object{
        const val ZIP_URL = "ZIP_URL"
        const val ZIP_MD5 = "ZIP_MD5"
        const val PERSON_PLAN_ITEM_ID = "PERSON_PLAN_ITEM_ID"
    }

    override fun createPresenter(): IWordLearningC.Presenter {
       return WordLearningPresenter(this,this,WordLearningModel(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        Glide.with(this)
            .load("https://i2.hdslb.com/bfs/archive/6c92eb1dadedd6d1e54767c97ce5c19e53807ff1.jpg")
            .into(mBinding.ivLessonImg)
        val zipUrl = intent.getStringExtra(ZIP_URL)?:""
        val zipMd5 = intent.getStringExtra(ZIP_MD5)?:""
        mPersonPlanItemId = intent.getStringExtra(PERSON_PLAN_ITEM_ID)?:""
        L.i("zipUrl:$zipUrl zipMd5:$zipMd5 personPlanItemId:$mPersonPlanItemId")
        mAudioPlayer.registerCallBack(this)
        presenter.downZip(zipUrl,zipMd5)

    }

    private fun initWindow(){
        val holderView = mDialog.holderView
        val rv_list = holderView.findViewById<RecyclerView>(R.id.rv_word_learning_list)
        rv_list.layoutManager = LinearLayoutManager(this)
        mWindowAdapter = WordLearningWindowAdapter()
        mWindowAdapter.setOnItemClickListener{ adapter,view,position ->
            L.i("setOnItemClickListener: $position")
        }
        rv_list.adapter = mWindowAdapter

    }

    override fun onDestroy() {
        super.onDestroy()
        mAudioPlayer.release()
        presenter.release()
    }

    override fun playPosition(position: Int) {

    }

    override fun prepareStart(playInfo: PlayInfo) {

    }

    override fun startPlay(position: Long) {

    }

    override fun playEnd(playIndex: Int) {
        // 切图，换文本
        val currentIndex = playIndex+1
        if (currentIndex < mImgList.size){
            val imgPath = mImgList[currentIndex]
            val imgText = mTextList[currentIndex]
            L.i("imgPath:$imgPath \n imgText:$imgText")
            Glide.with(this)
                .load(imgPath)
                .into(mBinding.ivLessonImg)
            mBinding.tvWord.text = imgText
        }else{//已经播完了，就上报学习记录
            presenter.reportStudyResult(mPersonPlanItemId)
        }

    }

    override fun getZipData(dirPath: String,zipDataList: List<ZipDataVo>) {
        L.i("dirPath:$dirPath ,zipDataList:$zipDataList")
        val audioList = zipDataList.map { MusicVo(
            id = it.id,
            albumName = it.title,
            mainActors = "",
            path = "$dirPath/${it.audio}",
            pathType = UrlType.DEFAULT
        ) }

        mAudioPlayer.setPlayList(audioList)
        mAudioPlayer.setPlayWhenReady(true)
        mTextList = zipDataList.map { it.title }
        mImgList = zipDataList.map { "$dirPath/${it.img}"}
        Glide.with(this)
            .load(mImgList[0])
            .into(mBinding.ivLessonImg)
        mBinding.tvWord.text = mTextList[0]
    }

    override fun getZipFileError(message: String) {
        ToastUtils.showLong("getZipFileError")
    }

    override fun downLoadProgress(progress: Float) {
        mBinding.tvDownloadProgress.text = progress.toString()
    }

}