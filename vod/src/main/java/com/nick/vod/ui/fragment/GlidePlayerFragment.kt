package com.nick.vod.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ServiceUtils
import com.blankj.utilcode.util.TimeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableResource
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.nick.base.vo.MusicVo
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayPositionCallBack
import com.nick.music.server.MusicServer
import com.nick.music.server.binder.MusicBinder
import com.nick.music.server.binder.impl.MusicServerBinder
import com.nick.vod.databinding.FragmentGlidePlayerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class GlidePlayerFragment: Fragment(), ServiceConnection, PlayInfoCallBack,PlayPositionCallBack {

    private val mBinding by lazy { FragmentGlidePlayerBinding.inflate(layoutInflater) }
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
        loadGif()
    }

    private fun initData(){
        val initDataTask = Runnable {
            mMusicBinder.setPlayList(loadData())
        }
        val playerTask = Runnable {
            mMusicBinder.play()
        }
        val registerCallBackTask = Runnable {
            mMusicBinder.registerCallBack(this)
        }
        mTasks.add(registerCallBackTask)
        mTasks.add(initDataTask)
        mTasks.add(playerTask)
    }

    private fun loadGif(){
        Glide.with(this)
            .load(File("${context?.filesDir?.absolutePath}/vod/abc.webp"))
            .addListener(object :RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (resource is WebpDrawable){
                        resource.loopCount = -1
                    }
                    return false
                }
            })
            .into(mBinding.ivGif)
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
            ServiceUtils.bindService(intent, this, AppCompatActivity.BIND_AUTO_CREATE)
        }
    }

    override fun onStart() {
        super.onStart()
        if (this::mMusicBinder.isInitialized){
            mMusicBinder.play()
        }
    }

    override fun onStop() {
        super.onStop()
        mMusicBinder.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtils.i("onDestroy")
        ServiceUtils.unbindService(this)
        ServiceUtils.stopService(Intent(context, MusicServer::class.java))
//        mMusicBinder.removeCallBack(this)
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        mMusicBinder = service as MusicServerBinder
        while (mTasks.isNotEmpty()){
            mTasks.poll()?.run()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    override fun playPosition(position: Long) {

        lifecycleScope.launchWhenResumed {
            withContext(Dispatchers.Main){
                mBinding.apply {
                    skPosition.progress = position.toInt()
                }
            }
        }
    }

    override fun prepareStart(playInfo: PlayInfo) {
        mBinding.apply {
            skPosition.max = playInfo.duration.toInt()
        }
    }

    override fun startPlay(position: Long) {
    }

}