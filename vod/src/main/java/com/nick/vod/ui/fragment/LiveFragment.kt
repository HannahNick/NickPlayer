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
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.server.MusicServer
import com.nick.music.server.binder.MusicBinder
import com.nick.vod.databinding.LayoutLiveBinding
import com.nick.vod.ui.adapter.LiveAdapter
import java.util.*

class LiveFragment: Fragment(), ServiceConnection, PlayInfoCallBack, SurfaceHolder.Callback{

    private val mBindingView by lazy { LayoutLiveBinding.inflate(layoutInflater) }
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
        val initDataTask = Runnable {
            val data = listOf(
                MusicVo(path = "http://ali-vl.cztv.com/channels/lantian/channel001/360p.m3u8?a=1000&d=2e81e5ce97d2a771861cbe3b0c492876&k=edb9ed932efb100c8acc51590186e08e&t=1684153870", pathType = UrlType.M3U8, liveName = "浙江卫视"),
                MusicVo(path = "https://padtx.qing.mgtv.com/nn_live/nn_x64/cWlkPSZzPTQzZjQyOWI3ZTMzZTNiNzVlMTFjODg2ZDNjODhlYzMxJmVzPTE2ODQyNTQzMTgmdXVpZD0wYjJjMTExMTU1YTg5ZmU0ZGZhYjU4NDc3NDE0YzFiOS03MTU5MGJlMiZ2PTImYXM9MCZjZG5leF9pZD10eF9waG9uZV9saXZl/HNYLMPP360.m3u8?_t=1684225517852&_t=1684225523139", pathType = UrlType.M3U8, liveName = "芒果娱乐"),
                MusicVo(path = "https://sztv-live.cutv.com/AxeFRth/500/q37Ake0.m3u8?sign=c83f63f5ad2220b8b0da5f2cb473dd4e&t=6465a1f7", pathType = UrlType.M3U8, liveName = "深圳卫视"),
                MusicVo(path = "https://sztv-live.cutv.com/2q76Sw2/500/d6k2k70.m3u8?sign=5a19cd512fa208ff3fe9d76e30b4ae70&t=6465a20d", pathType = UrlType.M3U8, liveName = "深圳公共频道"),
                MusicVo(path = "https://sztv-live.cutv.com/1q4iPng/500/f5i1k40.m3u8?sign=5066b6ac5083c16f59a5a813d871672a&t=6465a222", pathType = UrlType.M3U8, liveName = "深圳娱乐频道"),
                MusicVo(path = "https://sztv-live.cutv.com/4azbkoY/500/63r4kz0.m3u8?sign=21eb8f57979d42e78212ee6d8ca1b2af&t=6465a232", pathType = UrlType.M3U8, liveName = "深圳电视剧频道"),
                MusicVo(path = "https://volc-stream.kksmg.com/live/dfws/index.m3u8?volcSecret=4f341cef920f83557063876cdf400f28&volcTime=1684312287", pathType = UrlType.M3U8, liveName = "东方卫视"),
                MusicVo(path = "https://tencent-stream.kksmg.com/live/ylpd.m3u8?txSecret=86dfe8f9d8ac20d2a500f3b28106f5c6&txTime=6464912f", pathType = UrlType.M3U8, liveName = "都市频道"),
                MusicVo(path = "https://tencent-stream.kksmg.com/live/wypd.m3u8?txSecret=d431af9912bf409330443a0abd89a54f&txTime=64649158", pathType = UrlType.M3U8, liveName = "外语频道"),

            )
            mMusicBinder.setPlayList(data)
            mBindingView.rvLive.apply {
                layoutManager = LinearLayoutManager(context)
                mLiveAdapter.addData(data)
                adapter = mLiveAdapter
                mLiveAdapter.setOnItemClickListener { adapter, view, position ->
                    mMusicBinder.play(position)
                    mBindingView.gcLayer.setLiveName(mMusicBinder.getPlayInfo().liveName)
                }
            }
        }
        val registerCallBackTask = Runnable {
            mMusicBinder.registerCallBack(this)
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
            svVideo.holder.addCallback(this@LiveFragment)
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

}