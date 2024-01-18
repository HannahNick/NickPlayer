package com.xyz.edu.contract

import com.xyz.base.app.mvp.IBaseContract
import com.xyz.edu.vo.ZipDataBean
import java.io.File

interface IWordLearningC {

    interface Presenter: IBaseContract.IPresenter {

        fun downZip(url: String)
    }
    interface View: IBaseContract.IView{
        fun getZipData(dirPath: String,zipDataList: List<ZipDataBean>)

        fun getZipFileError(message: String)
    }

    interface Model: IBaseContract.IModel{

    }
}