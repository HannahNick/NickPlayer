package com.xyz.edu.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.FileUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.enick.base.util.TimeUtil
import com.nick.base.manager.DialogManager
import com.nick.base.manager.PlanManager
import com.nick.base.router.BaseRouter
import com.nick.base.vo.MusicVo
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.music.player.impl.NickExoPlayer
import com.nick.music.server.PlayMode
import com.xyz.edu.R
import com.xyz.edu.databinding.ActivityHomeBinding
@Route(path = BaseRouter.HOME)
class HomeActivity : AppCompatActivity(){

    private val mBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    private val mNickExoPlayer by lazy { NickExoPlayer(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        BarUtils.setStatusBarVisibility(this,false)
        initPlayer()
        initListener()
    }

    private fun initPlayer(){
        mNickExoPlayer.attachSurfaceHolder(mBinding.svVideo.holder)
        mNickExoPlayer.setPlayMode(PlayMode.CYCLE)
        mNickExoPlayer.setPlayList(loadData())
        mNickExoPlayer.play()
    }

    private fun initListener(){
        mBinding.apply {
            btnStart.setOnClickListener {
                PlanManager.startItem(this@HomeActivity,0,object : PlanManager.LoadingListener{
                    override fun showLoading() {
                        btnStart.isClickable = false
                        aviLoading.visibility = View.VISIBLE
                    }

                    override fun hideLoading() {
                        btnStart.isClickable = true
                        aviLoading.visibility = View.GONE
                    }

                })
            }
            btnContinue.setOnClickListener {
                PlanManager.startItem(this@HomeActivity, loadingListener = object : PlanManager.LoadingListener{
                    override fun showLoading() {
                        btnContinue.isClickable = false
                        aviLoading.visibility = View.VISIBLE
                    }

                    override fun hideLoading() {
                        btnContinue.isClickable = true
                        aviLoading.visibility = View.GONE
                    }

                })
            }
        }
        initBackListener()
    }

    private fun playAnim(){
        YoYo.with(Techniques.FadeIn)
            .playOn(mBinding.btnStart)
        YoYo.with(Techniques.FadeIn)
            .playOn(mBinding.btnContinue)
    }

    override fun onStop() {
        super.onStop()
        mNickExoPlayer.pause()
    }

    override fun onStart() {
        super.onStart()
        mBinding.apply {
            btnContinue.visibility = if (PlanManager.mCurrentIndex<0){
                View.GONE
            }else{
                View.VISIBLE
            }
        }
        mNickExoPlayer.play()

    }

    private fun loadData(): MutableList<MusicVo>{
        val filePath = "file:///android_asset/home3.mp4"
        return listOf(MusicVo("1","albumName","",filePath)).toMutableList()
    }

    private fun initBackListener(){
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                TimeUtil.confirmClick { finish() }
            }
        })
    }

}