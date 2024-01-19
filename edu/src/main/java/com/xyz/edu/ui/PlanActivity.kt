package com.xyz.edu.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.edu.R
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
                ToastUtils.showLong("$position")
            }
            mPlanAdapter.setOnLoadMoreListener({
               planPresenter.loadMore()
            },mBinding.rvPlanList)

        }
    }

    private fun initData(){
        planPresenter.refresh()
    }

    override fun getPlanItemList(dataList: List<PlanItemBean>) {
        ListAdapterUtil.setUpData(mBinding.srlRefreshlayout,mPlanAdapter,dataList,null)
    }

    override fun onDestroy() {
        super.onDestroy()
        planPresenter.release()
    }
}