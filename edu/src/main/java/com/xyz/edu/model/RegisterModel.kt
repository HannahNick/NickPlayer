package com.xyz.edu.model

import android.content.Context
import com.xyz.base.app.mvp.BaseModel
import com.xyz.base.service.ServiceProvider
import com.xyz.base.service.edu.bean.RegisterBean
import com.xyz.base.service.svc.RESULT
import com.xyz.edu.contract.IRegisterC
import io.reactivex.Flowable

class RegisterModel(context: Context): BaseModel(context),IRegisterC.Model {
    override fun register(
        personName: String,
        personAccount: String,
        password: String,
        age: Int,
        basics: Int,
        sex: Int
    ): Flowable<RESULT<RegisterBean>> {
        return ServiceProvider.getEduService("http://38.91.106.109:9393").eduRegister(personName, personAccount, password, age, basics, sex)
    }

}