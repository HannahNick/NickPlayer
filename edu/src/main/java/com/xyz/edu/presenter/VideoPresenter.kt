package com.xyz.edu.presenter

import android.content.Context
import com.nick.base.manager.PlanManager
import com.xyz.base.app.rx.io2Main
import com.xyz.edu.contract.IVideoC
import com.nick.base.manager.UserManager
import com.xyz.base.utils.L
import com.xyz.edu.model.WordLearningModel
import com.xyz.edu.presenter.base.DisposablePresenter

class VideoPresenter(context: Context, view: IVideoC.View, model: IVideoC.Model): DisposablePresenter<IVideoC.View, IVideoC.Model>(context,view, model),
    IVideoC.Presenter {

        private val mWordLearningModel by lazy { WordLearningModel(context) }

    override fun reportStudyResult(personPlanItemId: String) {
        mWordLearningModel.reportStudyResult(UserManager.personPlanId,personPlanItemId, UserManager.personId)
            .io2Main()
            .subscribe({

            },{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }

    }

    override fun downSubtitleFile(zipUrl: String, md5: String,subtitleFileName: String) {
        L.i("zipUrl:$zipUrl ,md5:$md5 ,subtitleFileName: $subtitleFileName")
        PlanManager.downZip(context,zipUrl,md5){
            PlanManager.findInvokeFile(context,md5,subtitleFileName,null){subtitleFile->
                view.getSubtitleFile(subtitleFile)
            }
        }
    }
}