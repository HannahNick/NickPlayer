package com.xyz.edu.contract

import com.xyz.base.app.mvp.IBaseContract
import com.xyz.edu.vo.ZipDataBean
import java.io.File

interface IWordLearningC {

    interface Presenter: IBaseContract.IPresenter {

        fun downZip(url: String,md5: String)

    }
    interface View: IBaseContract.IView{
        fun getZipData(dirPath: String,zipDataList: List<ZipDataBean>)

        fun getZipFileError(message: String)

        fun downLoadProgress(progress: Float)
    }

    interface Model: IBaseContract.IModel{

    }
}