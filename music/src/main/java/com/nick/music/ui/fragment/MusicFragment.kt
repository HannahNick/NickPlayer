package com.nick.music.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ServiceUtils
import com.blankj.utilcode.util.ServiceUtils.bindService
import com.blankj.utilcode.util.TimeUtils
import com.nick.base.vo.MusicVo
import com.nick.music.R
import com.nick.music.databinding.FragmentMusicPlayBinding
import com.nick.music.entity.PlayInfo
import com.nick.music.krc.KrcLyricsFileReader
import com.nick.music.model.LyricsTag
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.server.MusicServer
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus
import com.nick.music.server.binder.MusicBinder
import com.nick.music.util.Ring
import com.nick.music.view.RhythmView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class MusicFragment:Fragment(), ServiceConnection,PlayInfoCallBack,RhythmView.LyricCallBackListener {

    private val mBinding by lazy { FragmentMusicPlayBinding.inflate(layoutInflater) }
    private val mTasks: Queue<Runnable> = LinkedList()
    private lateinit var mMusicBinder: MusicBinder
    private val mPlayModeList = Ring<PlayMode>().apply { addAll(PlayMode.values().toMutableList()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initServer()
        initListener()
        initAudio()
    }

    private fun initData(){
        val initDataTask = Runnable {
            lifecycleScope.launchWhenResumed {
//                val data = withContext(Dispatchers.IO){
//                    return@withContext HttpManager.api.getAllMusic().data
//                }
//                mMusicBinder.setPlayList(data?:ArrayList())

                LogUtils.i("完成数据请求")
                mMusicBinder.setPlayList(loadData())
            }
        }
        val registerCallBackTask = Runnable {
            mMusicBinder.registerCallBack(this)
        }
        mTasks.add(initDataTask)
        mTasks.add(registerCallBackTask)

    }

    private fun loadData(): List<MusicVo>{
        val filePath = context?.filesDir?.absolutePath?:""
        val mcPath = "$filePath/mc"
        val krcPath = "$filePath/krc"
        val mcList = FileUtils.listFilesInDir(mcPath)
        return mcList.map {
            val albumName = it.name.substring(0,it.name.lastIndexOf("."))
            MusicVo("1",albumName,"","$mcPath/${it.name}", lyricPath = "$krcPath/${albumName}.krc")
        }
    }

    private fun initServer(){
        if (!ServiceUtils.isServiceRunning(MusicServer::class.java)){
            val intent = Intent(context, MusicServer::class.java)
            bindService(intent,this, AppCompatActivity.BIND_AUTO_CREATE)
        }
    }

    private fun initListener(){
        mBinding.apply {
            ivPlay.setOnClickListener {
                val info = mMusicBinder.getPlayInfo()
                LogUtils.i(GsonUtils.toJson(info))
                if (info.playStatus == PlayStatus.PLAY){
                    pause()
                }else{
                    play(info)
                }
            }
            ivPlayNext.setOnClickListener {
                mMusicBinder.playNext()
            }

            ivPlayLast.setOnClickListener {
                mMusicBinder.playLast()
            }


            ivSing.setOnTouchListener { v, event ->

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        rtvRhythm.sing(true)
                    }
                    MotionEvent.ACTION_UP -> {
                        rtvRhythm.sing(false)
                    }
                }
                true
            }
            skPositionBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    mBinding.tvPlayTime.text = TimeUtils.millis2String(progress.toLong(),"mm:ss")
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
//                    mMusicBinder.pause()
//                    rtvRhythm.pause()
//                    mMusicBinder.seek(82425)
//                    rtvRhythm.mResetData = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val seekPosition = seekBar.progress
                    LogUtils.i("onStopTrackingTouch: $seekPosition")
                    mMusicBinder.seek(seekPosition)
                }

            })
            skKeyBar.max = 24
            skKeyBar.progress = 12
            skKeyBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val keyPosition = seekBar.progress
                    tvKeyNum.text = (keyPosition-12).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val keyPosition = seekBar.progress
                    val keyValue = 1f+(keyPosition-12)*0.02f
                    mMusicBinder.setKey(keyValue)
                }
            })

            ivMode.setOnClickListener {
                val playMode = mPlayModeList.next()
                when (playMode){
                    PlayMode.CYCLE-> ivMode.setImageResource(R.drawable.play_cycle)
                    PlayMode.SINGLE-> ivMode.setImageResource(R.drawable.play_single)
                    PlayMode.RANDOM-> ivMode.setImageResource(R.drawable.play_random)
                    else->{}
                }
                mMusicBinder.setPlayMode(playMode?:PlayMode.CYCLE)
            }
            ivMusicList.setOnClickListener {
                LogUtils.json(GsonUtils.toJson(mMusicBinder.getRandomMusicList()))
            }
            rtvRhythm.lyricCallBackListener = this@MusicFragment
        }

    }

    private fun initAudio(){
//        val dispatcherFactory = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0)
//        val pitchHandler = PitchDetectionHandler { pitchDetectionResult, audioEvent ->
//            val resultHz = pitchDetectionResult.pitch
//            lifecycleScope.launchWhenResumed {
//                withContext(Dispatchers.Main){
//                    mBinding.rtvRhythm.apply {
//                        if (resultHz==-1f){
//                            sing(false)
//                        }else{
//                            sing(true)
//                        }
//                    }
//                }
//            }
//        }
//        val processor = PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100f, 1024, pitchHandler)
//        dispatcherFactory.addAudioProcessor(processor)
//        Thread(dispatcherFactory,"Audio Dispatcher").start()
    }

    fun pause(){
        mMusicBinder.pause()
        LogUtils.i("player currentPosition: ${mMusicBinder.getPlayInfo().currentPosition}")
        mBinding.apply {
            ivPlay.setImageResource(R.drawable.play)
            rtvRhythm.pause()
        }

    }

    fun play(info: PlayInfo){
        LogUtils.i("继续播放 position:${info.currentPosition}")
        mMusicBinder.play(info.dataIndex)
        mBinding.apply {
            ivPlay.setImageResource(R.drawable.pause)
            rtvRhythm.resume()
        }
    }

    override fun onStop() {
        super.onStop()
        pause()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        mMusicBinder = service as MusicBinder
        LogUtils.i("绑定服务回调成功")
        while (mTasks.isNotEmpty()){
            mTasks.poll()?.run()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    override fun playPosition(position: Int) {
        val playTime = TimeUtils.millis2String(position.toLong(),"mm:ss")

        lifecycleScope.launchWhenResumed {
            withContext(Dispatchers.Main){
                mBinding.apply {
                    skPositionBar.progress = position
                    tvPlayTime.text = playTime
                    ktvLyric.setCurrentPosition(position.toLong())
                }
            }
        }
    }

    override fun prepareStart(playInfo: PlayInfo) {
        val playInfoDuration = playInfo.duration.toLong()
        val durationTime = TimeUtils.millis2String(playInfoDuration,"mm:ss")
        mBinding.apply {
            skPositionBar.max = playInfo.duration
            tvDurationTime.text = durationTime
            tvAlbumName.text = playInfo.albumName
            tvMainActor.text = playInfo.mainActor
            ivPlay.setImageResource(R.drawable.play)
            if (rtvRhythm.mTitle == playInfo.albumName && rtvRhythm.mActor == playInfo.mainActor){
                return
            }
            val path = playInfo.lyricPath
            if (TextUtils.isEmpty(path)){
                return
            }
            val krcInfo = KrcLyricsFileReader().readFile(File(path))
            if (krcInfo!=null){
                rtvRhythm.setData(krcInfo,playInfoDuration)
                ktvLyric.setData(krcInfo)
                tvAlbumName.text = krcInfo.lyricsTags[LyricsTag.TAG_TITLE] as String
                tvMainActor.text = krcInfo.lyricsTags[LyricsTag.TAG_ARTIST] as String
            }

        }
    }

    override fun startPlay(position: Long) {
        mBinding.apply {
            ivPlay.setImageResource(R.drawable.pause)
            rtvRhythm.seek(position)
            rtvRhythm.start()
        }

    }

    override fun currentSingLyric(lyric: String) {
        mBinding.tvLyric.text = lyric
    }
}