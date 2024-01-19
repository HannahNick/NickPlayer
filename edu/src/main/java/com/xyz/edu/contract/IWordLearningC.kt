package com.xyz.edu.contract

import com.xyz.base.app.mvp.IBaseContract
import com.xyz.base.service.ServiceProvider
import com.xyz.base.service.svc.RESULT
import com.xyz.edu.vo.ZipDataVo
import io.reactivex.Flowable

interface IWordLearningC {

    interface Presenter: IBaseContract.IPresenter {

        fun downZip(url: String,md5: String)

        fun reportStudyResult(personPlanItemId: Int)

    }
    interface View: IBaseContract.IView{
        fun getZipData(dirPath: String,zipDataList: List<ZipDataVo>)

        fun getZipFileError(message: String)

        fun downLoadProgress(progress: Float)
    }

    interface Model: IBaseContract.IModel{
        fun reportStudyResult(personPlanId : Int,
                              personPlanItemId : Int,
                              personId : Int,
        ): Flowable<RESULT<Any>>
    }
}