package com.xyz.edu.contract

import com.xyz.base.app.mvp.IBaseContract
import com.xyz.base.service.edu.bean.RegisterBean
import com.xyz.base.service.svc.RESULT
import io.reactivex.Flowable

interface IRegisterC {
    interface Model: IBaseContract.IModel{
        fun register( personName : String,
                      personAccount : String,
                      password : String,
                      age: Int,
                      basics : Int,
                      sex : Int,): Flowable<RESULT<RegisterBean>>
    }

    interface View: IBaseContract.IView{
        fun registerSuccess()
        fun registerFail(msg: String)
    }

    interface Presenter: IBaseContract.IPresenter{
        fun register(personName : String,
                     personAccount : String,
                     password : String,
                     age: Int,
                     basics : Int,
                     sex : Int,)
    }
}