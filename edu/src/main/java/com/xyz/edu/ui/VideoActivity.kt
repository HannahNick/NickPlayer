package com.xyz.edu.ui

import android.os.Bundle
import com.blankj.utilcode.util.BarUtils
import com.nick.music.entity.PlayInfo
import com.nick.music.player.PlayInfoCallBack
import com.nick.vod.ui.fragment.VodFragment
import com.nick.vod.view.LiveGestureControlLayer
import com.nick.vod.wiget.GestureMessageCenter
import com.xyz.base.utils.L
import com.xyz.edu.R
import com.xyz.edu.contract.IVideoC
import com.xyz.edu.databinding.ActivityVideoBinding
import com.xyz.edu.model.VideoModel
import com.xyz.edu.presenter.VideoPresenter
import com.xyz.proxy.IProxy
import com.xyz.proxy.KtvPlayProxyManager
import kotlin.properties.Delegates

class VideoActivity : BaseActivity<IVideoC.Presenter>(), IVideoC.View, PlayInfoCallBack,
    LiveGestureControlLayer.GestureCallBack{

    private val mBinding by lazy { ActivityVideoBinding.inflate(layoutInflater) }
    private var mPersonPlanItemId: String = ""
    companion object{
        const val VIDEO_URL = "VIDEO_URL"
        const val VIDEO_NAME = "VIDEO_NAME"
        const val PERSON_PLAN_ITEM_ID = "PERSON_PLAN_ITEM_ID"
    }

    private var mProxy: IProxy? by Delegates.observable(null) { property, oldValue, newValue ->
        L.i("newValue $newValue oldValue $oldValue")
        if (newValue != null && oldValue != newValue) {
            L.i("oldValue stop")
            oldValue?.stop()
        }
    }

    override fun createPresenter(): IVideoC.Presenter {
        return VideoPresenter(this,this,VideoModel(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        BarUtils.setStatusBarVisibility(this,false)
        initView()
    }

    private fun initView(){
        GestureMessageCenter.registerCallBack(this)
        mPersonPlanItemId = intent.getStringExtra(PERSON_PLAN_ITEM_ID)?:""
        val videoUrl = intent.getStringExtra(VIDEO_URL)?:""
        val videoName = intent.getStringExtra(VIDEO_NAME)?:""
        mProxy = KtvPlayProxyManager.createKtvProxy(this)
        mProxy?.start()
        val proxyUrl = mProxy?.buildProxyUrl(videoUrl)?.apply {
            L.i(" buildProxyUrl $this")
        }?:""
        val vodFragment = VodFragment.newInstance(arrayListOf(proxyUrl), arrayListOf(videoName))
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_contain, vodFragment)
        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        mProxy?.stop()
    }

    override fun playPosition(position: Int) {

    }

    override fun prepareStart(playInfo: PlayInfo) {

    }

    override fun startPlay(position: Long) {

    }

    override fun playEnd(playIndex: Int) {
        super.playEnd(playIndex)
        presenter.reportStudyResult(mPersonPlanItemId)
    }

    override fun back() {
        super.back()
        finish()
    }

}