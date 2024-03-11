package com.xyz.edu.ui

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.context
import com.blankj.utilcode.util.ToastUtils
import com.nick.base.manager.PlanManager
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.utils.L
import com.xyz.edu.contract.IPlanC
import com.xyz.edu.databinding.ActivityPlanBinding
import com.xyz.edu.model.PlanModel
import com.xyz.edu.presenter.PlanPresenter
import com.xyz.edu.ui.adapter.PlanListAdapter
import com.xyz.edu.util.ListAdapterUtil

/**
 * 学习计划列表
 */
class PlanActivity :  BaseActivity<IPlanC.Presenter>(), IPlanC.View {

    private val mBinding by lazy { ActivityPlanBinding.inflate(layoutInflater) }
    private val planPresenter by lazy { PlanPresenter(this,this,PlanModel(this)) }
    private val mPlanAdapter by lazy { PlanListAdapter() }

    override fun createPresenter(): IPlanC.Presenter {
        return planPresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initView()
        initData()
    }

    private fun initView(){
        mBinding.apply {
            srlRefreshlayout.isRefreshing = true
            srlRefreshlayout.setOnRefreshListener {
                planPresenter.refresh()
            }
            rvPlanList.layoutManager = GridLayoutManager(this@PlanActivity,3)
            rvPlanList.adapter = mPlanAdapter
            mPlanAdapter.setOnItemClickListener{ adapter,view,position->
                val data = mPlanAdapter.data[position]
                when(data.contentType){
                    1,2->{
//                        val videoIntent = Intent(this@PlanActivity,VideoActivity::class.java)
//                        videoIntent.putExtra(VideoActivity.VIDEO_URL,data.contentUrl)
//                        videoIntent.putExtra(VideoActivity.VIDEO_NAME,data.contentTitle)
//                        videoIntent.putExtra(VideoActivity.PERSON_PLAN_ITEM_ID,data.personPlanItemId)
//                        startActivity(videoIntent)
//                        PlanManager.toVideo(context,data.contentUrl,data.contentTitle,data.personPlanItemId,data.zip.url,position)
                    }
                    3->{
//                        val wordLearningIntent = Intent(this@PlanActivity,WordLearningActivity::class.java)
//                        wordLearningIntent.putExtra(WordLearningActivity.ZIP_URL,data.zip.url)
//                        wordLearningIntent.putExtra(WordLearningActivity.ZIP_MD5,data.zip.md5)
//                        wordLearningIntent.putExtra(WordLearningActivity.PERSON_PLAN_ITEM_ID,data.personPlanItemId)
//                        startActivity(wordLearningIntent)
//                        PlanManager.downZip(context,data.zip.url,data.zip.md5,data.contentUrl,position)
                    }
                    5->{
//                        val wordLearningIntent = Intent(this@PlanActivity,WordLearningActivity::class.java)
//                        wordLearningIntent.putExtra(WordLearningActivity.ZIP_URL,data.zip.url)
//                        wordLearningIntent.putExtra(WordLearningActivity.ZIP_MD5,data.zip.md5)
//                        wordLearningIntent.putExtra(WordLearningActivity.PERSON_PLAN_ITEM_ID,data.personPlanItemId)
//                        startActivity(wordLearningIntent)
                        PlanManager.toWordLearning(context,data.zip.url,data.zip.md5,data.personPlanItemId,position)
                    }
                    else ->{
                        ToastUtils.showLong("$position")
                    }
                }


            }
            mPlanAdapter.setOnLoadMoreListener({
               planPresenter.loadMore()
            },mBinding.rvPlanList)

        }
    }

    private fun initData(){
        planPresenter.refresh()
    }

    override fun loginSuccess() {

    }

    override fun loginFail() {
    }

    override fun getPlanItemList(dataList: List<PlanItemBean>) {
        L.i(dataList)
        ListAdapterUtil.setUpData(mBinding.srlRefreshlayout,mPlanAdapter,dataList,null)
        PlanManager.initData(this,dataList,0)
//        PlanManager.toNextPlanItem(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        planPresenter.release()
    }
}