package com.xyz.edu.presenter

import android.content.Context
import com.xyz.base.app.rx.io2Main
import com.xyz.edu.contract.IBootC
import com.xyz.edu.manager.UserManager
import com.xyz.edu.presenter.base.DisposablePresenter

class BootPresenter(context: Context, view: IBootC.View, model: IBootC.Model): DisposablePresenter<IBootC.View, IBootC.Model>(context,view, model),
    IBootC.Presenter {
    override fun login() {
        model.login("13714570137","12345678")
            .io2Main()
            .subscribe({
                val result = it.result
                UserManager.personId = result.personId
                UserManager.personPlanItemId = result.personPlanItemId
                UserManager.personPlanId = result.personPlanId
                view.loginSuccess()
            },{
                it.printStackTrace()
                view.loginFail()
            }).apply { compositeDisposable.add(this) }
    }

    override fun register() {
        model.register("王大锤","13714570137","12345678",10,1,1)
            .io2Main()
            .subscribe({
                UserManager.personId = it.result.personId
                login()
            },{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }



}