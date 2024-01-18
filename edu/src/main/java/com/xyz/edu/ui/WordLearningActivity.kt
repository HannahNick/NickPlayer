package com.xyz.edu.ui

import android.os.Bundle
import com.blankj.utilcode.util.ZipUtils
import com.bumptech.glide.Glide
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayerControl
import com.nick.music.player.impl.NickExoPlayer
import com.xyz.edu.contract.IWordLearningC
import com.xyz.edu.databinding.ActivityWordLearningBinding
import com.xyz.edu.model.HomeModel
import com.xyz.edu.presenter.WordLearningPresenter
import com.xyz.edu.vo.ZipDataBean
import java.io.File

class WordLearningActivity : BaseActivity<IWordLearningC.Presenter>(),IWordLearningC.View, PlayInfoCallBack {

    private val mBinding by lazy { ActivityWordLearningBinding.inflate(layoutInflater) }
    private val audioPlayer: PlayerControl by lazy { NickExoPlayer(this) }
    private lateinit var imgList:List<String>
    private lateinit var textList:List<String>

    companion object{
        const val ZIP_URL = "ZIP_URL"
    }

    override fun createPresenter(): IWordLearningC.Presenter {
       return WordLearningPresenter(this,this,HomeModel(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        Glide.with(this)
            .load("https://i2.hdslb.com/bfs/archive/6c92eb1dadedd6d1e54767c97ce5c19e53807ff1.jpg")
            .into(mBinding.ivLessonImg)
        val zipUrl = intent.getStringExtra(ZIP_URL)!!
        presenter.downZip(zipUrl)

        audioPlayer.setPlayList(arrayListOf())
        audioPlayer.setPlayWhenReady(true)

    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }

    override fun playPosition(position: Int) {

    }

    override fun prepareStart(playInfo: PlayInfo) {

    }

    override fun startPlay(position: Long) {

    }

    override fun playEnd(playIndex: Int) {
        // TODO: 切图，换文本
        val currentIndex = playIndex+1
        if (currentIndex < imgList.size){
            Glide.with(this)
                .load(imgList[currentIndex])
                .into(mBinding.ivLessonImg)
            mBinding.tvWord.text = textList[currentIndex]
        }

    }

    override fun getZipData(dirPath: String,zipDataList: List<ZipDataBean>) {
        val audioList = zipDataList.map { MusicVo(
            id = it.id,
            albumName = it.title,
            mainActors = "",
            path = "$dirPath/${it.audiio}",
            pathType = UrlType.DEFAULT
        ) }
        audioPlayer.setPlayList(audioList)
        textList = zipDataList.map { it.text }
        imgList = zipDataList.map { it.img}
//        textList[]

    }

    override fun getZipFileError(message: String) {
        TODO("Not yet implemented")
    }

}