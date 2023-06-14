package com.nick.music.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ServiceUtils
import com.blankj.utilcode.util.ServiceUtils.bindService
import com.blankj.utilcode.util.TimeUtils
import com.nick.base.BaseUrl
import com.nick.base.http.HttpManager
import com.nick.base.vo.MusicVo
import com.nick.music.R
import com.nick.music.databinding.FragmentMusicPlayBinding
import com.nick.music.entity.PlayInfo
import com.nick.music.krc.KrcLyricsFileReader
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.server.MusicServer
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus
import com.nick.music.server.binder.MusicBinder
import com.nick.music.util.Ring
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MusicFragment:Fragment(), ServiceConnection,PlayInfoCallBack {

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
    }

    private fun initData(){
        val initDataTask = Runnable {
            lifecycleScope.launchWhenResumed {
//                val data = withContext(Dispatchers.IO){
//                    return@withContext HttpManager.api.getAllMusic().data
//                }
//                mMusicBinder.setPlayList(data?:ArrayList())
                val krcInfo = withContext(Dispatchers.IO){
                    val file = File("${context?.filesDir?.absolutePath}/krc","c.krc")
                    KrcLyricsFileReader().readFile(file)
                }
                mBinding.rtvRhythm.setData(krcInfo)

                val path = context?.filesDir?.absolutePath?:""
                LogUtils.i("完成数据请求")
                mMusicBinder.setPlayList(listOf(MusicVo("1","千千阙歌","陈慧娴","${path}/mc/a.flac")))
            }
        }
        val registerCallBackTask = Runnable {
            mMusicBinder.registerCallBack(this)
        }
        mTasks.add(initDataTask)
        mTasks.add(registerCallBackTask)
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
                    mMusicBinder.pause()
                    rtvRhythm.pause()
                    ivPlay.setImageResource(R.drawable.play)
                }else{
                    mMusicBinder.play(info.dataIndex)
                    rtvRhythm.startDraw()
                    ivPlay.setImageResource(R.drawable.pause)
                }
            }
            ivPlayNext.setOnClickListener {
                mMusicBinder.playNext()
            }

            ivPlayLast.setOnClickListener {
                mMusicBinder.playLast()
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
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val seekPosition = seekBar.progress
                    mMusicBinder.seek(seekPosition)
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
        }

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
                }
            }
        }
    }

    override fun prepareStart(playInfo: PlayInfo) {
        val durationTime = TimeUtils.millis2String(playInfo.duration.toLong(),"mm:ss")
        mBinding.apply {
            skPositionBar.max = playInfo.duration
            tvDurationTime.text = durationTime
            tvAlbumName.text = playInfo.albumName
            tvMainActor.text = playInfo.mainActor
            if (playInfo.playStatus == PlayStatus.PLAY){
                ivPlay.setImageResource(R.drawable.pause)
            }else{
                ivPlay.setImageResource(R.drawable.play)
            }
        }
    }

    override fun startPlay() {
        mBinding.rtvRhythm.startDraw()
    }
}