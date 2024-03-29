package com.xyz.edu.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.edu.R

class PlanListAdapter: BaseQuickAdapter<PlanItemBean,BaseViewHolder>(R.layout.layout_plan_item) {
    override fun convert(helper: BaseViewHolder, item: PlanItemBean) {
        when(item.contentType){
            1 ->{
                helper.setText(R.id.tv_plan_name,"视频")
            }
            2 ->{
                helper.setText(R.id.tv_plan_name,"音频")
            }
            3 ->{
                helper.setText(R.id.tv_plan_name,"题目")
            }
            else ->{
                helper.setText(R.id.tv_plan_name,item.contentTitle)
            }
        }

    }
}