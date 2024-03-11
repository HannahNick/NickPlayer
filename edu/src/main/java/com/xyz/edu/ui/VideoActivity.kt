package com.xyz.edu.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.BarUtils
import com.nick.base.router.BaseRouter
import com.nick.base.manager.PlanManager
import com.nick.base.vo.MusicVo
import com.nick.base.vo.enum.UrlType
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

@Route(path = BaseRouter.AROUTER_VIDEOACTIVITY)
class VideoActivity : BaseActivity<IVideoC.Presenter>(), IVideoC.View, PlayInfoCallBack,
    LiveGestureControlLayer.GestureCallBack, PlanManager.PreInitDataCallBack{

    private val mBinding by lazy { ActivityVideoBinding.inflate(layoutInflater) }
    private var mPersonPlanItemId: String = ""
    private var mItemIndex: Int = 0
    companion object{
        const val VIDEO_URL = "videoUrl"
        const val VIDEO_NAME = "titleName"
        const val PERSON_PLAN_ITEM_ID = "personPlanItemId"
        const val ITEM_INDEX = "itemIndex"
        const val ZIP_URL = "zipUrl"

        fun start(context: Context, url: String, titleName: String, personPlanItemId: String, itemIndex: Int){
            val videoIntent = Intent(context,VideoActivity::class.java)
            videoIntent.putExtra(VIDEO_URL,url)
            videoIntent.putExtra(VIDEO_NAME,titleName)
            videoIntent.putExtra(PERSON_PLAN_ITEM_ID,personPlanItemId)
            videoIntent.putExtra(ITEM_INDEX,itemIndex)
//            videoIntent.putExtra(ZIP_PATH)
            context.startActivity(videoIntent)
        }
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
        PlanManager.registerDataCallBack(this)
        initView()
    }

    private fun initView(){
        GestureMessageCenter.registerCallBack(this)
        val data = PlanManager.mDataList[PlanManager.mCurrentIndex]

        mPersonPlanItemId = data.personPlanItemId
        mItemIndex = PlanManager.mCurrentIndex
        val videoUrl = data.contentUrl
//        val videoUrl = "http://video.f666666.xyz/mp4/17190049/file/313_17190049"
        val videoName = data.contentTitle

        mProxy = if (videoUrl.endsWith("m3u8")){
            KtvPlayProxyManager.createKtvProxy(this)
        }else{
            KtvPlayProxyManager.createM4aProxy(this)
        }
        mProxy?.start()
        val proxyUrl = mProxy?.buildProxyUrl(videoUrl)?.apply {
            L.i(" buildProxyUrl $this")
        }?:""
        val vodFragment = VodFragment.newInstance(MusicVo(
            path = proxyUrl,
            liveName = videoName,
            pathType = UrlType.M3U8
        ))
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
        L.i("播放结束，上报学习结果")
//        presenter.reportStudyResult(mPersonPlanItemId)
        PlanManager.toNextPlanItem(this,mItemIndex)
    }

    override fun preInitDataFinish() {
        L.i("preInitDataFinish")
        finish()
    }

    override fun getSubtitleFile(filePath: String) {
        TODO("Not yet implemented")
    }

}