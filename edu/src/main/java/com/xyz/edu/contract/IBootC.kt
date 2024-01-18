package com.xyz.edu.contract

import com.xyz.base.app.mvp.IBaseContract
import com.xyz.base.service.edu.bean.LoginBean
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.service.edu.bean.RegisterBean
import com.xyz.base.service.svc.DATA
import com.xyz.base.service.svc.PAGE
import com.xyz.base.service.svc.RESULT
import io.reactivex.Flowable

interface IBootC {
    interface Presenter: IBaseContract.IPresenter {
        fun login()

        fun register()

        fun getPersonPlanItemList(pageNum: Int, pageSize: Int)
    }
    interface View: IBaseContract.IView{
        fun loginSuccess()

        fun loginFail()

        fun toVideo(url: String)

        fun toWordLearning(zipUrl: String)
    }

    interface Model: IBaseContract.IModel{
        fun register( personName : String,
                   personAccount : String,
                   password : String,
                   age: Int,
                   basics : Int,
                   sex : Int,): Flowable<RESULT<RegisterBean>>

        fun login(personAccount : String,password : String): Flowable<RESULT<LoginBean>>

        fun getPersonPlanItemList(personId: Int,
                                  personPlanId: Int,
                                  pageNum: Int,
                                  pageSize: Int,
        ): Flowable<RESULT<PAGE<PlanItemBean>>>
    }
}