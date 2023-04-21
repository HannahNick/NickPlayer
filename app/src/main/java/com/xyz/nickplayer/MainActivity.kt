package com.xyz.nickplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ServiceUtils
import com.nick.base.BaseUrl
import com.nick.music.entity.MusicVo
import com.nick.music.server.MusicServer
import com.nick.music.server.binder.MusicBinder
import com.xyz.nickplayer.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity(),ServiceConnection {

    private val mBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var mMusicBinder: MusicBinder
    private val mTasks: Queue<Runnable> = LinkedList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initServer()
        initData()
        mBinding.apply {
            btnPlay.setOnClickListener {
                mMusicBinder.play()
            }
        }
    }

    private fun initData(){
        val task = Runnable {
            val data = listOf(
                MusicVo("1","光年之外","邓紫棋","${BaseUrl.url}/music/lemon.mp3"),
                MusicVo("2","光年之外","邓紫棋","${BaseUrl.url}/music/Born_To_Die-Lana_Del_Rey.mp3"),
                MusicVo("3","光年之外","邓紫棋","${BaseUrl.url}/music/Landscape-押尾コータロー.mp3"),
            )
            mMusicBinder.setPlayList(data)
            LogUtils.i("设置播放数据成功")
        }
        mTasks.add(task)

    }

    private fun initServer(){
        if (!ServiceUtils.isServiceRunning(MusicServer::class.java)){
            val intent = Intent(this,MusicServer::class.java)
            bindService(intent,this,BIND_AUTO_CREATE)
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