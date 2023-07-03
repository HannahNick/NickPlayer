package com.nick.vod.ui.fragment

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
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ServiceUtils
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
import com.nick.music.server.KTVServer
import com.nick.music.server.binder.impl.TwoPlayerServerBinder
import com.nick.vod.databinding.FragmentTwoPlayerBinding
import java.util.*

class TwoPlayerFragment: Fragment(), ServiceConnection {

    private val mBinding by lazy { FragmentTwoPlayerBinding.inflate(layoutInflater) }
    private val mTasks: Queue<Runnable> = LinkedList()
    private lateinit var mTwoPlayerBinder : TwoPlayerServerBinder

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
    }

    private fun initData(){
        val initDataTask = Runnable {
            val vodPath = "${context?.filesDir?.absolutePath}/vod/abc.webp"
            mTwoPlayerBinder.setVodPlayerList(listOf(MusicVo(path = vodPath, pathType = UrlType.DEFAULT, liveName = "浙江卫视")))
            mTwoPlayerBinder.setMusicPlayList(
                loadData()
            )
        }
        val initSurfaceHolderTask = Runnable {
            mTwoPlayerBinder.attachSurfaceHolder(mBinding.svVideo.holder)
        }
        val playerTask = Runnable {
            mTwoPlayerBinder.play()
        }
        mTasks.add(initDataTask)
        mTasks.add(initSurfaceHolderTask)
        mTasks.add(playerTask)
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
        if (!ServiceUtils.isServiceRunning(KTVServer::class.java)){
            val intent = Intent(context, KTVServer::class.java)
            ServiceUtils.bindService(intent, this, AppCompatActivity.BIND_AUTO_CREATE)
        }
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        mTwoPlayerBinder = service as TwoPlayerServerBinder
        while (mTasks.isNotEmpty()){
            mTasks.poll()?.run()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

}