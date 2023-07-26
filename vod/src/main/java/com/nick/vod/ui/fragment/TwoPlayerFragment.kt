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
import com.nick.base.vo.enum.UrlType
import com.nick.music.entity.PlayInfo
import com.nick.music.krc.KrcLyricsFileReader
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.server.KTVServer
import com.nick.music.server.PlayMode
import com.nick.music.server.binder.impl.TwoPlayerServerBinder
import com.nick.vod.databinding.FragmentTwoPlayerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class TwoPlayerFragment: Fragment(), ServiceConnection, PlayInfoCallBack, SurfaceHolder.Callback {

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
        initView()
    }

    private fun initView(){
        mBinding.svVideo.holder.addCallback(this)
    }

    private fun initData(){
        val initDataTask = Runnable {
            val vodPath = "${context?.filesDir?.absolutePath}/vod/adc.mp4"
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
            mTwoPlayerBinder.setPlayMode(PlayMode.SINGLE)
            mTwoPlayerBinder.muteVod()
        }
        val registerCallBackTask = Runnable {
            mTwoPlayerBinder.registerCallBack(this)
        }
        mTasks.add(registerCallBackTask)
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

    override fun onStop() {
        super.onStop()
        if (this::mTwoPlayerBinder.isInitialized){
            mTwoPlayerBinder.setPlayWhenReady(false)
        }
    }

    override fun onStart() {
        super.onStart()
        if (this::mTwoPlayerBinder.isInitialized){
            mTwoPlayerBinder.setPlayWhenReady(true)
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
        mTwoPlayerBinder = service as TwoPlayerServerBinder
        while (mTasks.isNotEmpty()){
            mTasks.poll()?.run()
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        LogUtils.i("onServiceDisconnected :${name.className}")
    }

    override fun playPosition(position: Int) {
        lifecycleScope.launchWhenResumed {
            withContext(Dispatchers.Main){
                mBinding.apply {
                    skPosition.progress = position
                    ktvLyric.setCurrentPosition(position.toLong())
                }
            }
        }
    }

    override fun prepareStart(playInfo: PlayInfo) {
        val path = playInfo.lyricPath
        if (TextUtils.isEmpty(path)){
            return
        }
        val krcInfo = KrcLyricsFileReader().readFile(File(path))
        mBinding.apply {
            skPosition.max = playInfo.duration
            if (krcInfo!=null){
                ktvLyric.setData(krcInfo)
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