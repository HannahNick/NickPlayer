package com.nick.music.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ServiceUtils
import com.blankj.utilcode.util.ServiceUtils.bindService
import com.nick.base.BaseUrl
import com.nick.music.R
import com.nick.music.databinding.FragmentMusicPlayBinding
import com.nick.music.entity.MusicVo
import com.nick.music.server.MusicServer
import com.nick.music.server.PlayStatus
import com.nick.music.server.binder.MusicBinder
import java.util.*

class MusicFragment:Fragment(), ServiceConnection {

    private val mBinding by lazy { FragmentMusicPlayBinding.inflate(layoutInflater) }
    private val mTasks: Queue<Runnable> = LinkedList()
    private lateinit var mMusicBinder: MusicBinder

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
        val task = Runnable {
            val data = listOf(
                MusicVo("1","光年之外","邓紫棋","${BaseUrl.url}/music/Born_To_Die-Lana_Del_Rey.mp3"),
                MusicVo("2","光年之外","邓紫棋","${BaseUrl.url}/music/lemon.mp3"),
                MusicVo("3","光年之外","邓紫棋","${BaseUrl.url}/music/Landscape-押尾コータロー.mp3"),
            )
            mMusicBinder.setPlayList(data)
            LogUtils.i("设置播放数据成功")
        }
        mTasks.add(task)

    }

    private fun initServer(){
        if (!ServiceUtils.isServiceRunning(MusicServer::class.java)){
            val intent = Intent(context, MusicServer::class.java)
            bindService(intent,this, AppCompatActivity.BIND_AUTO_CREATE)
        }
    }

    private fun initListener(){
        mBinding.apply {
            ivBack.setOnClickListener {  }
            ivPlay.setOnClickListener {
                val info = mMusicBinder.getPlayInfo()
                if (info.playStatus == PlayStatus.PLAY){
                    mMusicBinder.pause()
                    ivPlay.setImageResource(R.drawable.play)
                }else{
                    mMusicBinder.play()
                    ivPlay.setImageResource(R.drawable.pause)
                }
            }
            ivPlayNext.setOnClickListener {
                mMusicBinder.playNext()
            }

            ivPlayLast.setOnClickListener {
                mMusicBinder.playLast()
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
}