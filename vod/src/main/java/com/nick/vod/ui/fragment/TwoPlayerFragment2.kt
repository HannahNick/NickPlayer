package com.nick.vod.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ServiceUtils
import com.nick.base.vo.MusicVo
import com.nick.music.entity.AudioTrackType
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.server.KTVServer
import com.nick.music.server.MusicServer
import com.nick.music.server.PlayMode
import com.nick.music.server.TrackType
import com.nick.music.server.binder.impl.MusicServerBinder
import com.nick.music.util.UrlUtil
import com.nick.vod.databinding.FragmentTwoPlayer2Binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class TwoPlayerFragment2: Fragment(), ServiceConnection, PlayInfoCallBack, SurfaceHolder.Callback {

    private val mBinding by lazy { FragmentTwoPlayer2Binding.inflate(layoutInflater) }
    private val mTasks: Queue<Runnable> = LinkedList()
    private lateinit var mMusicBinder : MusicServerBinder

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
        mBinding.btnChangeTrack.setOnClickListener {
            mMusicBinder.changeTrack(AudioTrackType.ORIGIN)
        }

    }

    private fun initData(){
        val playerTask = Runnable {
            mMusicBinder.setPlayList(loadData())
            mMusicBinder.play()
        }
        val registerCallBackTask = Runnable {
            mMusicBinder.registerCallBack(this)
        }
        mTasks.add(registerCallBackTask)
        mTasks.add(playerTask)
    }

    private fun loadData(): MutableList<MusicVo>{
        val filePath = context?.filesDir?.absolutePath?:""
        val mcPath = "$filePath/mc"
        val krcPath = "$filePath/krc"
        val mcList = FileUtils.listFilesInDir(mcPath)
        return mcList.map {
            val albumName = it.name.substring(0,it.name.lastIndexOf("."))
            MusicVo("1",albumName,"","$mcPath/${it.name}", lyricPath = "$krcPath/${albumName}.krc")
        }.toMutableList()
    }

    private fun initServer(){
        if (!ServiceUtils.isServiceRunning(MusicServer::class.java)){
            val intent = Intent(context, MusicServer::class.java)
            ServiceUtils.bindService(intent, this, AppCompatActivity.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (this::mMusicBinder.isInitialized){
            mMusicBinder.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ServiceUtils.unbindService(this)
        ServiceUtils.stopService(Intent(context, KTVServer::class.java))
//        if (this::mTwoPlayerBinder.isInitialized){
//            mTwoPlayerBinder.release()
//        }
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        mMusicBinder = service as MusicServerBinder
        while (mTasks.isNotEmpty()){
            mTasks.poll()?.run()
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        LogUtils.i("onServiceDisconnected :${name.className}")
    }

    override fun playPosition(position: Long) {
        lifecycleScope.launchWhenResumed {
            withContext(Dispatchers.Main){
                mBinding.apply {
                    ktvLyricParent.setPlayerPosition(position)
                }
            }
        }
    }

    override fun prepareStart(playInfo: PlayInfo) {
        val path = playInfo.lyricPath
        if (TextUtils.isEmpty(path)){
            return
        }

        mBinding.apply {
            if (UrlUtil.isAudioUrl(playInfo.url)){
                val loopPath = "${context?.filesDir?.absolutePath}/vod/adc.mp4"
                ktvLyricParent.setKrc(File(path))
                ktvLyricParent.setLoopFile(File(loopPath))
                ktvLyricParent.play()
            }
        }
    }

    override fun startPlay(position: Long) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
//        if (this::mTwoPlayerBinder.isInitialized){
//            mTwoPlayerBinder.attachSurfaceHolder(holder)
//        }
        LogUtils.i("surfaceCreated :")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        LogUtils.i("surfaceChanged :")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        if (this::mTwoPlayerBinder.isInitialized){
//            mTwoPlayerBinder.clearSurfaceHolder(holder)
//        }
        LogUtils.i("surfaceDestroyed :")
    }

}