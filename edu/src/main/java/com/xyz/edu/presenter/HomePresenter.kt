package com.xyz.edu.presenter

import android.content.Context
import com.xyz.base.app.mvp.BasePresenter
import com.xyz.edu.contract.IHomeC

class HomePresenter(context: Context,view: IHomeC.View,model: IHomeC.Model): BasePresenter<IHomeC.View,IHomeC.Model>(context,view, model),IHomeC.Presenter {
}