package com.xyz.edu.contract

import com.xyz.base.app.mvp.IBaseContract
import com.xyz.base.service.edu.bean.LoginBean
import com.xyz.base.service.edu.bean.RegisterBean
import com.xyz.base.service.svc.DATA
import com.xyz.base.service.svc.RESULT
import io.reactivex.Flowable

interface IBootC {
    interface Presenter: IBaseContract.IPresenter {
        fun login()

        fun register()
    }
    interface View: IBaseContract.IView{
    }

    interface Model: IBaseContract.IModel{
        fun register( personName : String,
                   personAccount : String,
                   password : String,
                   age: Int,
                   basics : Int,
                   sex : Int,): Flowable<DATA<RESULT<RegisterBean>>>

        fun login(personAccount : String,password : String): Flowable<DATA<RESULT<LoginBean>>>
    }
}