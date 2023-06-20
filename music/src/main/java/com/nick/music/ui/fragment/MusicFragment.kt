package com.nick.music.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
            lifecycleScope.launchWhenResumed {
//                val data = withContext(Dispatchers.IO){
//                    return@withContext HttpManager.api.getAllMusic().data
//                }
//                mMusicBinder.setPlayList(data?:ArrayList())

                val path = context?.filesDir?.absolutePath?:""
                LogUtils.i("完成数据请求")
                mMusicBinder.setPlayList(listOf(
                    MusicVo("1","苦瓜","","$path/mc/kg.mp3", lyricPath = "$path/krc/kg.krc"),
                    MusicVo("1","心恋","","$path/mc/xl.flac", lyricPath = "$path/krc/xl.krc"),
                    MusicVo("1","像风一样自由","","$path/mc/fyyzy.mp3", lyricPath = "$path/krc/fyyzy.krc"),
                    MusicVo("1","孤勇者","","$path/mc/gyz.mp3", lyricPath = "$path/krc/gyz.krc"),
                    MusicVo("1","回头太难","","$path/mc/httn.mp3", lyricPath = "$path/krc/httn.krc"),
                    MusicVo("1","K歌之王","","$path/mc/kgzw.mp3", lyricPath = "$path/krc/kgzw.krc"),
                    MusicVo("1","明年今日","","$path/mc/mnjr.mp3", lyricPath = "$path/krc/mnjr.krc"),
                    MusicVo("1","你最珍贵","","$path/mc/nzzg.mp3", lyricPath = "$path/krc/nzzg.krc"),
                    MusicVo("1","葡萄成熟时","","$path/mc/ptcss.mp3", lyricPath = "$path/krc/ptcss.krc"),
                    MusicVo("1","十面埋伏","","$path/mc/smmf.mp3", lyricPath = "$path/krc/smmf.krc"),
                    MusicVo("1","Shall We Talk","","$path/mc/swt.mp3", lyricPath = "$path/krc/swt.krc"),
                    MusicVo("1","相思风雨中","","$path/mc/xsfyz.mp3", lyricPath = "$path/krc/xsfyz.krc"),
                    MusicVo("1","喜悦","","$path/mc/xy.mp3", lyricPath = "$path/krc/xy.krc"),
                    MusicVo("1","夜空中最亮的星","","$path/mc/ykzzldx.mp3", lyricPath = "$path/krc/ykzzldx.krc"),
                    MusicVo("1","因为爱情","","$path/mc/ywaq.mp3", lyricPath = "$path/krc/ywaq.krc"),
                    MusicVo("1","一万次悲伤","","$path/mc/ywcbs.mp3", lyricPath = "$path/krc/ywcbs.krc"),
                    MusicVo("1","遥远的她(粤语版)","","$path/mc/yydt.mp3", lyricPath = "$path/krc/yydt.krc"),
                ))
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
                    ivPlay.setImageResource(R.drawable.play)
                    rtvRhythm.pause()
                }else{
                    mMusicBinder.play(info.dataIndex)
                    ivPlay.setImageResource(R.drawable.pause)
                    rtvRhythm.resume()
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
        val playInfoDuration = playInfo.duration.toLong()
        val durationTime = TimeUtils.millis2String(playInfoDuration,"mm:ss")
        mBinding.apply {
            skPositionBar.max = playInfo.duration
            tvDurationTime.text = durationTime
            tvAlbumName.text = playInfo.albumName
            tvMainActor.text = playInfo.mainActor
            ivPlay.setImageResource(R.drawable.play)
            rtvRhythm.seek(playInfo.currentPosition.toLong())
            if (rtvRhythm.mTitle == playInfo.albumName){
                return
            }
            val path = playInfo.lyricPath
            if (TextUtils.isEmpty(path)){
                return
            }
            val krcInfo = KrcLyricsFileReader().readFile(File(path))
            if (krcInfo!=null){
                rtvRhythm.totalTime = playInfoDuration
                rtvRhythm.setData(krcInfo)
                tvAlbumName.text = krcInfo.lyricsTags[LyricsTag.TAG_TITLE] as String
                tvMainActor.text = krcInfo.lyricsTags[LyricsTag.TAG_ARTIST] as String
            }

        }
    }

    override fun startPlay() {
        mBinding.ivPlay.setImageResource(R.drawable.pause)
        LogUtils.i("节奏已开始")
        mBinding.rtvRhythm.start()
    }
}