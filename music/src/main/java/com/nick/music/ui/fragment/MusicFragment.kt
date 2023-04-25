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
import com.nick.music.R
import com.nick.music.databinding.FragmentMusicPlayBinding
import com.nick.music.entity.MusicVo
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.server.MusicServer
import com.nick.music.server.PlayMode
import com.nick.music.server.PlayStatus
import com.nick.music.server.binder.MusicBinder
import com.nick.music.util.Ring
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

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
            val data = listOf(
                MusicVo("2","夜空中最亮的星","逃跑计划","${BaseUrl.url}/music/逃跑计划 - 夜空中最亮的星.mp3"),
                MusicVo("1","这班人","陈慧琳","${BaseUrl.url}/music/这班人-陈慧琳.mp3"),
                MusicVo("3","原谅","张玉华","${BaseUrl.url}/music/原谅-张玉华.mp3"),
                MusicVo("3","存在","邓紫棋","${BaseUrl.url}/music/邓紫棋 - 存在.mp3"),
                MusicVo("3","长路漫漫伴你闯","林子祥","${BaseUrl.url}/music/长路漫漫伴你闯-林子祥.mp3"),
                MusicVo("3","Dark Horse","Katy Perry、juicy J","${BaseUrl.url}/music/Katy Perry、juicy J - Dark Horse.mp3"),
            )
            mMusicBinder.setPlayList(data)
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
                    ivPlay.setImageResource(R.drawable.play)
                }else{
                    mMusicBinder.play(info.dataIndex)
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
        }
    }
}