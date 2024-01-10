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
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ServiceUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.server.MusicServer
import com.nick.music.server.binder.MusicBinder
import com.nick.vod.databinding.LayoutLiveBinding
import com.nick.vod.databinding.LayoutVodBinding
import com.nick.vod.ui.adapter.LiveAdapter
import com.nick.vod.view.LiveGestureControlLayer
import com.nick.vod.wiget.GestureMessageCenter
import java.util.*

class VodFragment: Fragment(), ServiceConnection, PlayInfoCallBack, SurfaceHolder.Callback,
    LiveGestureControlLayer.GestureCallBack{

    private val mBindingView by lazy { LayoutVodBinding.inflate(layoutInflater) }
    private val mTasks: Queue<Runnable> = LinkedList()
    private lateinit var mMusicBinder: MusicBinder
    private val mLiveAdapter = LiveAdapter()

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
        GestureMessageCenter.registerCallBack(this)
        val initDataTask = Runnable {
            mMusicBinder.registerCallBack(this)
            val data = listOf(
                MusicVo(path = "${context?.filesDir?.absolutePath}/vod/abc.mp4", pathType = UrlType.DEFAULT, liveName = "浙江卫视"),

            )
            mMusicBinder.setPlayList(data)
//            mBindingView.rvVod.apply {
//                layoutManager = LinearLayoutManager(context)
//                mLiveAdapter.addData(data)
//                adapter = mLiveAdapter
//                mLiveAdapter.setOnItemClickListener { adapter, view, position ->
//                    mMusicBinder.play(position)
//                    mBindingView.gcLayer.setLiveName(mMusicBinder.getPlayInfo().liveName)
//                }
//            }
        }
        val registerCallBackTask = Runnable {
            mBindingView.gcLayer.apply {
                initMusicBinder(mMusicBinder)
                setLiveName(mMusicBinder.getPlayInfo().liveName)
            }

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
        val playPositionText = TimeUtils.millis2String(position.toLong(),"mm:ss")
        mBindingView.apply {
            tvPlayTime.text = playPositionText
            sbSeek.progress = position
        }

    }

    override fun prepareStart(playInfo: PlayInfo) {
        val durationText = TimeUtils.millis2String(playInfo.duration.toLong(),"mm:ss")
        mBindingView.apply {
            tvPlayDuration.text = "/$durationText"
            tvPlayTime.text = "00:00"
            sbSeek.max = playInfo.duration
        }
    }

    override fun startPlay(position: Long) {
        mBindingView.gcLayer.setPlayStart()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        LogUtils.i("surfaceCreated")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        LogUtils.i("surfaceChanged>>> format:$format,width:$width,height:$height")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun seek(position: Int) {
        mMusicBinder.seek(position)

    }

    override fun loading(show: Boolean) {
        mBindingView.lpbLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

}