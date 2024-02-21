package com.xyz.edu.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.edu.R

class StudySelectAdapter: BaseQuickAdapter<PlanItemBean, BaseViewHolder>(R.layout.item_study_plan) {
    override fun convert(helper: BaseViewHolder, item: PlanItemBean) {
        helper.setText(R.id.tv_content_title,item.contentTitle)

    }
}