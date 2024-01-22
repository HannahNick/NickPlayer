package com.xyz.edu.presenter

import android.content.Context
import com.xyz.base.app.mvp.BasePresenter
import com.xyz.edu.contract.IVideoC
import com.xyz.edu.contract.IWordLearningC
import com.xyz.edu.manager.UserManager
import com.xyz.edu.model.WordLearningModel

class VideoPresenter(context: Context, view: IVideoC.View, model: IVideoC.Model): BasePresenter<IVideoC.View, IVideoC.Model>(context,view, model),
    IVideoC.Presenter {

        private val mWordLearningModel by lazy { WordLearningModel(context) }

    override fun reportStudyResult(personPlanItemId: String) {
        mWordLearningModel.reportStudyResult(UserManager.personPlanId,personPlanItemId,UserManager.personId)
    }
}