package com.xyz.edu.model

import android.content.Context
import com.xyz.base.app.mvp.BaseModel
import com.xyz.base.service.ServiceProvider
import com.xyz.base.service.edu.bean.LoginBean
import com.xyz.base.service.edu.bean.PersonWordListBean
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.service.edu.bean.RegisterBean
import com.xyz.base.service.edu.bean.StudyRecordBean
import com.xyz.base.service.svc.PAGE
import com.xyz.base.service.svc.RESULT
import com.xyz.edu.contract.ILoginC
import io.reactivex.Flowable

class LoginModel(context: Context): BaseModel(context),ILoginC.Model {

    override fun register( personName : String,
                           personAccount : String,
                           password : String,
                           age: Int,
                           basics : Int,
                           sex : Int,): Flowable<RESULT<RegisterBean>> {



        return ServiceProvider.getEduService("http://38.91.106.109:9393").eduRegister(personName, personAccount, password, age, basics, sex)
    }

    override fun login(personAccount : String,password : String): Flowable<RESULT<LoginBean>> {
        return ServiceProvider.getEduService(baseUrl = "http://38.91.106.109:9393").eduLogin(personAccount, password)
    }

    fun getPersonPlanItemList(personPlanId: String,
                              pageNum: Int,
                              pageSize: Int,
    ): Flowable<RESULT<PAGE<PlanItemBean>>>{
        return ServiceProvider.getEduService("http://38.91.106.109:9393").getPersonPlanItemList(personPlanId, pageNum, pageSize)
    }

    fun getPersonStudyRecord(personId: String,
    ): Flowable<RESULT<StudyRecordBean>>{
        return ServiceProvider.getEduService().getPersonStudyRecord(personId)
    }
}