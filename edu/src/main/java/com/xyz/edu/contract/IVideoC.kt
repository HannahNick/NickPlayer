package com.xyz.edu.contract

import com.xyz.base.app.mvp.IBaseContract
import java.io.File

interface IVideoC {
    interface Presenter: IBaseContract.IPresenter {

        fun reportStudyResult(personPlanItemId: String)

        fun downSubtitleFile(zipUrl: String, md5: String,subtitleFileName: String)
    }
    interface View: IBaseContract.IView{
        fun getSubtitleFile(file: File)
    }

    interface Model: IBaseContract.IModel{

    }
}