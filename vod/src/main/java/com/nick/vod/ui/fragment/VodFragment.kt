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

class VodFragment: Fragment(), ServiceConnection, PlayInfoCallBack, SurfaceHolder.Callback{

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
                MusicVo(path = "https://sztv-live.cutv.com/AxeFRth/500/q7Ave0R.m3u8?sign=fb88623ccabc098eb19d4261e79b6374&t=646361cf", pathType = UrlType.M3U8, liveName = "深圳卫视"),
                MusicVo(path = "https://sztv-live.cutv.com/ZwxzUXr/500/x1uZvx0.m3u8?sign=b2dfe34eb4dd01f11e36b24a14988d23&t=6463623a", pathType = UrlType.M3U8, liveName = "深圳都市频道"),
                MusicVo(path = "https://sztv-live.cutv.com/2q76Sw2/500/d3k2v70.m3u8?sign=72d150bdea78411548ac66ae3581804f&t=64636276", pathType = UrlType.M3U8, liveName = "深圳公共频道"),
                MusicVo(path = "https://sztv-live.cutv.com/1q4iPng/500/f2i1vp0.m3u8?sign=947d87a69144f0873c6257794f32a9dd&t=646362a8", pathType = UrlType.M3U8, liveName = "深圳娱乐频道"),
                MusicVo(path = "https://sztv-live.cutv.com/4azbkoY/500/6rpvz0k.m3u8?sign=9c78eb218ba7010cdcde737c6c578dd2&t=646362ce", pathType = UrlType.M3U8, liveName = "深圳电视剧频道"),
                MusicVo(path = "https://sztv-live.cutv.com/sztvgjpd/500/a77svt0g.m3u8?sign=9bc0d6a5446282926ba28053fed148be&t=646362f8", pathType = UrlType.M3U8, liveName = "深圳国际频道"),
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

    override fun startPlay() {
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