package com.xyz.edu.presenter

import android.content.Context
import com.xyz.auth.api.IAuthService
import com.xyz.base.app.rx.io2Main
import com.xyz.base.utils.L
import com.xyz.edu.contract.IPlanC
import com.nick.base.manager.UserManager
import com.xyz.edu.model.BootModel
import com.xyz.edu.presenter.base.BaseListPresenter
import com.xyz.edu.util.ListAdapterUtil

class PlanPresenter(context: Context, view: IPlanC.View, model: IPlanC.Model): BaseListPresenter<IPlanC.View, IPlanC.Model>(context,view, model),
    IPlanC.Presenter {

    val loginModel: BootModel
    init {
        loginModel = BootModel(context)
    }

    override fun login() {
        loginModel.login("13714570138","12345678")
            .io2Main()
            .subscribe({
                L.i("IAuthService login start")
                IAuthService.create(context)?.login("eduLogin")
                L.i("IAuthService login end")
                val result = it.result
                UserManager.personId = result.personId
                UserManager.personPlanItemId = result.personPlanItemId
                UserManager.personPlanId = result.personPlanId
                requestListData()
            },{
                it.printStackTrace()
                view.loginFail()
            },{
                L.i("login finish")
            }).apply { compositeDisposable.add(this) }
    }

    override fun register() {
        L.i("register")

        loginModel.register("王尼美","13714570138","12345678",10,1,1)
            .io2Main()
            .subscribe({
//                UserManager.personId = it.result.personId
                login()
            },{
                it.printStackTrace()
            },{
                L.i("register finish")
            }).apply { compositeDisposable.add(this) }
    }

    override fun requestListData() {
        model.getPersonPlanItemList(UserManager.personPlanId,mPageNum,ListAdapterUtil.PAGE_SIZE)
            .io2Main()
            .subscribe({
                view.getPlanItemList(it.result.pageContent)
            },{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }



}