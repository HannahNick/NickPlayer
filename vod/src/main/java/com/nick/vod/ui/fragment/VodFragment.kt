package com.nick.vod.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ServiceUtils
import com.nick.base.BaseUrl
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.server.MusicServer
import com.nick.music.server.binder.MusicBinder
import com.nick.vod.databinding.FragmentVodBinding
import java.io.File
import java.util.*

class VodFragment: Fragment(), ServiceConnection, PlayInfoCallBack, SurfaceHolder.Callback {

    private val mBindingView by lazy { FragmentVodBinding.inflate(layoutInflater) }
    private val mTasks: Queue<Runnable> = LinkedList()
    private lateinit var mMusicBinder: MusicBinder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return mBindingView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initServer()
        initListener()
    }

    private fun initServer(){
        if (!ServiceUtils.isServiceRunning(MusicServer::class.java)){
            val intent = Intent(context, MusicServer::class.java)
            ServiceUtils.bindService(intent, this, AppCompatActivity.BIND_AUTO_CREATE)
        }
    }

    private fun initData(){
//        LogUtils.i("电影本地路径:${PathUtils.getExternalMoviesPath()}/bear.mp4  isExists: ${File(PathUtils.getExternalMoviesPath()+"/bear.mp4").exists()}")
        val initDataTask = Runnable {
            val data = listOf(
//                MusicVo("1","bear","bear","path","movies/bear.mp4","123","123"),
                MusicVo("1","bear","bear","play/QbYVXQpd/index.m3u8",UrlType.M3U8,"123","123","123"),
            )
            mMusicBinder.setPlayList(data)
        }
        val registerCallBackTask = Runnable {
            mMusicBinder.registerCallBack(this)
        }
        val initSurfaceHolderTask = Runnable {
            mMusicBinder.attachSurfaceHolder(mBindingView.svVideo.holder)
        }
        mTasks.add(initDataTask)
        mTasks.add(registerCallBackTask)
        mTasks.add(initSurfaceHolderTask)

    }

    private fun initListener(){
        mBindingView.apply {
            ivPlay.setOnClickListener {
                mMusicBinder.play()
            }
            ivPause.setOnClickListener {
                mMusicBinder.pause()
            }
            svVideo.holder.addCallback(this@VodFragment)
        }
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        mMusicBinder = service as MusicBinder
        LogUtils.i("绑定服务回调成功")
        while (mTasks.isNotEmpty()){
            mTasks.poll()?.run()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    override fun playPosition(position: Int) {
    }

    override fun prepareStart(playInfo: PlayInfo) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        LogUtils.i("surfaceCreated")
//        mMusicBinder.attachSurfaceHolder(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        LogUtils.i("surfaceCreated>>> format:$format,width:$width,height:$height")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }
}