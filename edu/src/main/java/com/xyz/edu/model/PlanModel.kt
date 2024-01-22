package com.xyz.edu.model

import android.content.Context
import com.xyz.base.app.mvp.BaseModel
import com.xyz.base.service.ServiceProvider
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.service.svc.PAGE
import com.xyz.base.service.svc.RESULT
import com.xyz.edu.contract.IPlanC
import io.reactivex.Flowable

class PlanModel(context: Context): BaseModel(context), IPlanC.Model {
    override fun getPersonPlanItemList(
        personPlanId: String,
        pageNum: Int,
        pageSize: Int
    ): Flowable<RESULT<PAGE<PlanItemBean>>> {
        return ServiceProvider.getEduService("http://38.91.106.109:9393").getPersonPlanItemList(personPlanId, pageNum, pageSize)
    }


}