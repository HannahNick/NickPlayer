package com.xyz.edu.model

import android.content.Context
import com.xyz.base.app.mvp.BaseModel
import com.xyz.base.service.ServiceProvider
import com.xyz.base.service.svc.RESULT
import com.xyz.edu.contract.IWordLearningC
import io.reactivex.Flowable

class WordLearningModel(context: Context): BaseModel(context),IWordLearningC.Model {
    override fun reportStudyResult(
        personPlanId: String,
        personPlanItemId: String,
        personId: String
    ): Flowable<RESULT<Any>> {
        return ServiceProvider.getEduService("http://38.91.106.109:9393").reportStudyResult(personPlanId, personPlanItemId, personId)
    }

}