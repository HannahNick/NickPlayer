package com.nick.vod.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.nick.base.vo.MusicVo
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.PlayPositionCallBack
import com.nick.music.player.impl.NickExoPlayer
import com.nick.vod.databinding.LayoutVodBinding
import com.nick.vod.view.LiveGestureControlLayer
import com.nick.vod.wiget.GestureMessageCenter
import com.xyz.base.utils.L
import com.xyz.vod.play.widget.SubRipTextView
import java.io.File

class VodFragment: Fragment(), PlayInfoCallBack, PlayPositionCallBack, SurfaceHolder.Callback,
    LiveGestureControlLayer.GestureCallBack{

    private lateinit var mBindingView: LayoutVodBinding
    private val mPlayerControl by lazy { NickExoPlayer(requireContext(),"VodFragment") }

    companion object{
        val VIDEO_PARAM = "URL_LIST_PARAM"
        fun newInstance(musicVo: MusicVo): VodFragment {
            val fragment = VodFragment()
            val args = Bundle()
            args.putParcelable(VIDEO_PARAM, musicVo)
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
        val musicVo = arguments?.getParcelable<MusicVo>(VIDEO_PARAM)

        if (musicVo!=null){
            LogUtils.i("urlList: $musicVo")
//            mPlayerControl.setPlayList(musicVo)
//            mPlayerControl.playSourceByClip(musicVo,musicVo.startTime,musicVo.stopTime)
        }else{
            LogUtils.w("data is empty")
        }
        mBindingView.gcLayer.apply {
            initMusicBinder(mPlayerControl)
        }
//        mBindingView.tvLiveName.text = mPlayerControl.getPlayInfo().liveName
        mPlayerControl.attachSurfaceHolder(mBindingView.svVideo.holder)

    }

    private fun initListener(){
        mBindingView.apply {
            ivPlay.setOnClickListener {
                mPlayerControl.play()
            }
//            ivCenterPlay.setOnClickListener {
//                mPlayerControl.play()
//            }
            svVideo.holder.addCallback(this@VodFragment)
        }
        mPlayerControl.registerPositionCallBack(this)
        if (activity is PlayInfoCallBack){
            LogUtils.i("activity is PlayInfoCallBack")
            val activityMusicCallBack = activity as PlayInfoCallBack
            mPlayerControl.registerPlayInfoCallBack(activityMusicCallBack)
        }
    }

    override fun playPosition(position: Long) {
        val playPositionText = TimeUtils.millis2String(position.toLong(),"mm:ss")
        mBindingView.apply {
            tvPlayTime.text = playPositionText
            sbSeek.progress = position.toInt()
            gcLayer.flushPlayStatus()

            srtvSubtitle.setPosition(position)
        }

    }

    override fun prepareStart(playInfo: PlayInfo) {
        val durationText = TimeUtils.millis2String(playInfo.duration.toLong(),"mm:ss")
        mBindingView.apply {
            tvPlayDuration.text = "/$durationText"
            tvPlayTime.text = "00:00"
            sbSeek.max = playInfo.duration.toInt()
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
        mPlayerControl.seek(position.toLong())

    }
//
//    override fun loading(show: Boolean) {
//        mBindingView.lpbLoading.visibility = if (show) View.VISIBLE else View.GONE
//    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayerControl.release()
    }
    fun showSubtitle(subtitleFile: File?, baseOffset: Long){
        L.i("showSubtitle: ${subtitleFile?.absolutePath}")
        mBindingView.apply {
            if (FileUtils.isFileExists(subtitleFile)) {
                srtvSubtitle.visibility = View.VISIBLE
                srtvSubtitle.setSrtSource(
                    file = subtitleFile!!,
                    type = SubRipTextView.Type.COMMON,
                    baseOffset = baseOffset
                )
//                srtvSubtitle.setOffset(subtitleAdjustment?.timeOffset?.times(1000) ?: 0)
            } else {
                srtvSubtitle.visibility = View.GONE
            }
        }

    }
}