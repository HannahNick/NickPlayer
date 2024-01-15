package com.xyz.edu.contract

import com.xyz.base.app.mvp.IBaseContract

interface IBootC {
    interface Presenter: IBaseContract.IPresenter {

    }
    interface View: IBaseContract.IView{
    }

    interface Model: IBaseContract.IModel{

    }
}