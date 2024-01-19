package com.xyz.edu.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.edu.R

class PlanListAdapter: BaseQuickAdapter<PlanItemBean,BaseViewHolder>(R.layout.layout_plan_item) {
    override fun convert(helper: BaseViewHolder, item: PlanItemBean) {
        helper.setText(R.id.tv_plan_name,item.contentTitle)
    }
}