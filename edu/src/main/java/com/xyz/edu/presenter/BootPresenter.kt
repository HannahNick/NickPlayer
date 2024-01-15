package com.xyz.edu.presenter

import android.content.Context
import com.xyz.base.app.mvp.BasePresenter
import com.xyz.edu.contract.IBootC
import com.xyz.edu.contract.IHomeC

class BootPresenter(context: Context, view: IBootC.View, model: IBootC.Model): BasePresenter<IBootC.View, IBootC.Model>(context,view, model),
    IBootC.Presenter {
}