package com.nick.vod.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.impl.NickExoPlayer
import com.nick.vod.R
import com.nick.vod.databinding.LayoutVodBinding
import com.nick.vod.view.LiveGestureControlLayer
import com.nick.vod.wiget.GestureMessageCenter
import kotlin.collections.ArrayList

class VodFragment: Fragment(), PlayInfoCallBack, SurfaceHolder.Callback,
    LiveGestureControlLayer.GestureCallBack{

    private lateinit var mBindingView: LayoutVodBinding
    private val mPlayerControl by lazy { NickExoPlayer(requireContext()) }

    companion object{
        val URL_LIST_PARAM = "URL_LIST_PARAM"
        val VIDEO_LIST_NAME = "VIDEO_LIST_NAME"
        fun newInstance(urlList: ArrayList<String>,nameList: ArrayList<String>): VodFragment {
            val fragment = VodFragment()
            val args = Bundle()
            args.putStringArrayList(URL_LIST_PARAM, urlList)
            args.putStringArrayList(VIDEO_LIST_NAME, nameList)
            fragment.setArguments(args)
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBindingView = LayoutVodBinding.inflate(inflater, container, false)
        return mBindingView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initData()
    }

    private fun initData(){
//        LogUtils.i("电影本地路径:${PathUtils.getExternalMoviesPath()}/bear.mp4  isExists: ${File(PathUtils.getExternalMoviesPath()+"/bear.mp4").exists()}")
        GestureMessageCenter.registerCallBack(this)
        val urlList = arguments?.getStringArrayList(URL_LIST_PARAM)
        val nameList = arguments?.getStringArrayList(VIDEO_LIST_NAME)

        if (urlList!=null){
            val data = urlList.mapIndexed { index, s ->
                return@mapIndexed MusicVo(path = s, pathType = UrlType.DEFAULT, liveName = nameList?.get(index)?:"")
            }
            LogUtils.i("dataList: $data")
            mPlayerControl.setPlayList(data)
        }else{
            LogUtils.w("data is empty")
        }
        mBindingView.gcLayer.apply {
            initMusicBinder(mPlayerControl)
        }
        mBindingView.tvLiveName.text = mPlayerControl.getPlayInfo().liveName
        mPlayerControl.attachSurfaceHolder(mBindingView.svVideo.holder)

    }

    private fun initListener(){
        mBindingView.apply {
            ivPlay.setOnClickListener {
                mPlayerControl.play()
            }
            ivCenterPlay.setOnClickListener {
                mPlayerControl.play()
            }
            svVideo.holder.addCallback(this@VodFragment)
        }
        mPlayerControl.registerCallBack(this)
        if (activity is PlayInfoCallBack){
            LogUtils.i("activity is PlayInfoCallBack")
            val activityMusicCallBack = activity as PlayInfoCallBack
            mPlayerControl.registerCallBack(activityMusicCallBack)
        }
    }

    override fun playPosition(position: Int) {
        val playPositionText = TimeUtils.millis2String(position.toLong(),"mm:ss")
        mBindingView.apply {
            tvPlayTime.text = playPositionText
            sbSeek.progress = position
            gcLayer.flushPlayStatus()
        }

    }

    override fun prepareStart(playInfo: PlayInfo) {
        val durationText = TimeUtils.millis2String(playInfo.duration.toLong(),"mm:ss")
        mBindingView.apply {
            tvPlayDuration.text = "/$durationText"
            tvPlayTime.text = "00:00"
            sbSeek.max = playInfo.duration
            gcLayer.flushPlayStatus()
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
        mPlayerControl.seek(position)

    }

    override fun loading(show: Boolean) {
        mBindingView.lpbLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayerControl.release()
    }
}