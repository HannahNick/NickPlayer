package com.xyz.edu.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.nick.base.manager.PlanManager
import com.nick.base.router.BaseRouter
import com.xyz.edu.databinding.ActivityStudySelectBinding
import com.xyz.edu.ui.adapter.StudySelectAdapter

@Route(path = BaseRouter.STUDY_SELECT)
class StudySelectActivity : AppCompatActivity() {

    private val mBinding by lazy { ActivityStudySelectBinding.inflate(layoutInflater) }
    private val mAdapter by lazy { StudySelectAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initView()
    }

    private fun initView(){
        mAdapter.addData(PlanManager.mDataList)
        mBinding.apply {
            rvList.layoutManager = LinearLayoutManager(this@StudySelectActivity)
            rvList.adapter = mAdapter
        }
    }
}