package com.xyz.edu.contract

import com.xyz.base.app.mvp.IBaseContract

interface IVideoC {
    interface Presenter: IBaseContract.IPresenter {

        fun reportStudyResult(personPlanItemId: String)

        fun downSubtitleFile(url: String,md5: String)
    }
    interface View: IBaseContract.IView{
        fun getSubtitleFile(filePath: String)
    }

    interface Model: IBaseContract.IModel{

    }
}