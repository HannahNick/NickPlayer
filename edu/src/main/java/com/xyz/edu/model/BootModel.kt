package com.xyz.edu.model

import android.content.Context
import com.xyz.base.app.mvp.BaseModel
import com.xyz.base.service.ServiceProvider
import com.xyz.base.service.edu.bean.LoginBean
import com.xyz.base.service.edu.bean.PersonWordListBean
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.service.edu.bean.RegisterBean
import com.xyz.base.service.edu.bean.StudyRecordBean
import com.xyz.base.service.svc.DATA
import com.xyz.base.service.svc.PAGE
import com.xyz.base.service.svc.RESULT
import com.xyz.edu.contract.IBootC
import io.reactivex.Flowable
import retrofit2.http.Field
import retrofit2.http.Query

class BootModel(context: Context): BaseModel(context), IBootC.Model {

    override fun register( personName : String,
              personAccount : String,
               password : String,
              age: Int,
              basics : Int,
              sex : Int,): Flowable<RESULT<RegisterBean>>{
        return ServiceProvider.getEduService().eduRegister(personName, personAccount, password, age, basics, sex)
    }

    override fun login(personAccount : String,password : String): Flowable<RESULT<LoginBean>>{
        return ServiceProvider.getEduService().eduLogin(personAccount, password)
    }

    fun getPersonPlanItemList(personPlanId: Int,
                              pageNum: Int,
                              pageSize: Int,
    ): Flowable<RESULT<PAGE<PlanItemBean>>>{
        return ServiceProvider.getEduService().getPersonPlanItemList(personPlanId, pageNum, pageSize)
    }

    fun getPersonWordList(personId: Int,
    ): Flowable<RESULT<PersonWordListBean>>{
        return ServiceProvider.getEduService().getPersonWordList(personId)
    }

    fun getPersonStudyRecord(personId: Int,
    ): Flowable<RESULT<StudyRecordBean>>{
        return ServiceProvider.getEduService().getPersonStudyRecord(personId)
    }

    fun reportStudyResult(personPlanId : Int,
                          personPlanItemId : Int,
                          personId : Int,
    ): Flowable<RESULT<Any>>{
        return ServiceProvider.getEduService().reportStudyResult(personPlanId, personPlanItemId, personId)
    }
}