package com.xyz.edu.contract

import com.xyz.base.app.mvp.IBaseContract
import com.xyz.base.service.edu.bean.PersonPlanListBean
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.service.svc.PAGE
import com.xyz.base.service.svc.RESULT
import io.reactivex.Flowable

interface IStudySelectC {
    interface Presenter: IBaseContract.IPresenter {

    }
    interface View: IBaseContract.IView{

        fun getPlanItemList(dataList: List<PlanItemBean>)
    }

    interface Model: IBaseContract.IModel{
        fun getPersonPlanItemList(personPlanId: String,
                                  pageNum: Int,
                                  pageSize: Int,
        ): Flowable<RESULT<PAGE<PlanItemBean>>>
    }
}