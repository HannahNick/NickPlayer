package com.nick.base.model

import android.content.Context
import com.xyz.base.app.mvp.BaseModel
import com.xyz.base.service.ServiceProvider
import com.xyz.base.service.svc.RESULT
import io.reactivex.Flowable

class WordLearningModel() {
    fun reportStudyResult(
        personPlanId: String,
        personPlanItemId: String,
        personId: String
    ): Flowable<RESULT<Any>> {
        return ServiceProvider.getEduService("http://38.91.106.109:9393").reportStudyResult(personPlanId, personPlanItemId, personId)
    }

}