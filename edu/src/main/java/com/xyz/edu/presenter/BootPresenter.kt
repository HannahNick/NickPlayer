package com.xyz.edu.presenter

import android.content.Context
import com.nick.base.router.PlanManager
import com.xyz.auth.api.IAuthService
import com.xyz.base.app.rx.io2Main
import com.xyz.base.utils.L
import com.xyz.edu.contract.IBootC
import com.xyz.edu.manager.UserManager
import com.xyz.edu.model.PlanModel
import com.xyz.edu.presenter.base.DisposablePresenter
import com.xyz.edu.util.ListAdapterUtil

class BootPresenter(context: Context, view: IBootC.View, model: IBootC.Model): DisposablePresenter<IBootC.View, IBootC.Model>(context,view, model),
    IBootC.Presenter {
    val planModel = PlanModel(context)
    override fun login() {
        model.login("13714570139","12345678")
//            .io2Main()
            .subscribe({
                L.i("IAuthService login start")
                IAuthService.create(context)?.login("eduLogin")
                L.i("IAuthService login end")
                val result = it.result
                UserManager.personId = result.personId
                UserManager.personPlanItemId = result.personPlanItemId
                UserManager.personPlanId = result.personPlanId
//                view.loginSuccess()
                requestListData()
//                getPersonPlanList()
            },{
                it.printStackTrace()
                view.loginFail()
            },{
                L.i("login finish")
            }).apply { compositeDisposable.add(this) }
    }

    override fun register() {
        L.i("register")

        model.register("王尼美","13714570139","12345678",10,1,1)
            .io2Main()
            .subscribe({
//                UserManager.personId = it.result.personId
                login()
            },{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }

    private fun requestListData() {
        planModel.getPersonPlanItemList(UserManager.personPlanId,1, ListAdapterUtil.PAGE_SIZE)
            .io2Main()
            .subscribe({
                PlanManager.initData(it.result.pageContent)
                PlanManager.toNextPlanItem(context,-1)
            },{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }

    private fun getPersonPlanList(){
        planModel.getPersonPlanList(UserManager.personPlanId,1,ListAdapterUtil.PAGE_SIZE)
            .io2Main()
            .subscribe({
               L.i(it)
            },{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }

}