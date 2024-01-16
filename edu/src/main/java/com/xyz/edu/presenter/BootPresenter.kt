package com.xyz.edu.presenter

import android.content.Context
import com.xyz.base.app.mvp.BasePresenter
import com.xyz.base.app.rx.io2Main
import com.xyz.edu.contract.IBootC
import com.xyz.edu.contract.IHomeC

class BootPresenter(context: Context, view: IBootC.View, model: IBootC.Model): DisposablePresenter<IBootC.View, IBootC.Model>(context,view, model),
    IBootC.Presenter {
    override fun login() {

    }

    override fun register() {
        model.register("王大锤","13714570137","12345678",10,1,1)
            .io2Main()
            .subscribe({
                it.data.result.personId
            },{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }


}