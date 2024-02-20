package com.xyz.edu.contract

import com.xyz.base.app.mvp.IBaseContract
import com.xyz.base.service.edu.bean.LoginBean
import com.xyz.base.service.edu.bean.RegisterBean
import com.xyz.base.service.svc.RESULT
import io.reactivex.Flowable

interface ILoginC {

    interface Model: IBaseContract.IModel{
        fun register( personName : String,
                      personAccount : String,
                      password : String,
                      age: Int,
                      basics : Int,
                      sex : Int,): Flowable<RESULT<RegisterBean>>

        fun login(personAccount : String,password : String): Flowable<RESULT<LoginBean>>
    }

    interface View: IBaseContract.IView{
        fun loginSuccess()


        fun loginFail(msg: String)
    }

    interface Presenter: IBaseContract.IPresenter{
        fun login(personAccount : String,password : String)

        fun register(personAccount : String,password : String)
    }
}