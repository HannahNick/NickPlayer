package com.xyz.edu.presenter

import android.content.Context
import com.nick.base.manager.PlanManager
import com.nick.base.manager.UserManager
import com.xyz.auth.api.IAuthService
import com.xyz.base.app.mvp.BasePresenter
import com.xyz.base.app.rx.io2Main
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.utils.L
import com.xyz.edu.contract.ILoginC
import com.xyz.edu.model.BootModel
import com.xyz.edu.model.PlanModel
import com.xyz.edu.presenter.base.DisposablePresenter
import com.xyz.edu.util.ListAdapterUtil

class LoginPresenter(context: Context,view: ILoginC.View,model: ILoginC.Model): DisposablePresenter<ILoginC.View,ILoginC.Model>(context,view, model),ILoginC.Presenter {

    private val mBootModel by lazy { BootModel(context) }
    private val planModel by lazy {PlanModel(context)}

    override fun login(personAccount: String, password: String) {
        view.showLoading(true)
        mBootModel.login(personAccount, password)
            .subscribe({it->
                if (it.code!="200"){
                    view.loginFail(it.message)
                    view.showLoading(false)
                    return@subscribe
                }
                L.i("IAuthService login start")
                IAuthService.create(context)?.login("eduLogin")
                L.i("IAuthService login end")
                val result = it.result
                UserManager.personId = result.personId
                UserManager.personPlanItemId = result.personPlanItemId
                UserManager.personPlanId = result.personPlanId
                getPersonPlanList()
            },{e->
                e.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }

    override fun register(personAccount: String, password: String) {
        mBootModel.register("王尼美","13714570139","12345678",10,1,1)
            .io2Main()
            .subscribe({
//                UserManager.personId = it.result.personId
                login(personAccount,password)
            },{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }

    /**
     * 获取学习列表
     */
    private fun requestListData(personPlanItemId: String) {
        mBootModel.getPersonPlanItemList(UserManager.personPlanId,1, ListAdapterUtil.PAGE_SIZE)
            .io2Main()
            .subscribe({
                PlanManager.initData(it.result.pageContent)
                val itemIndex = checkHaveStudyItem(personPlanItemId,it.result.pageContent)
                L.i("requestListData: startIndex $itemIndex")
                PlanManager.startItem(context,itemIndex)
            },{
                it.printStackTrace()
            },{
                view.showLoading(false)
            }).apply { compositeDisposable.add(this) }
    }

    /**
     * 获取当前用户学习计划
     */
    private fun getPersonPlanList(){
        planModel.getPersonPlanList(UserManager.personId,1, ListAdapterUtil.PAGE_SIZE)
            .io2Main()
            .subscribe({
                L.i("getPersonPlanList data:$it")
                requestListData(it.result.pageContent[0].personPlanItemId)
            },{
                view.showLoading(false)
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }

    /**
     * 查看学习进度
     */
    private fun checkHaveStudyItem(personPlanItemId: String,data: List<PlanItemBean>): Int{
        var itemIndex = -1
        data.forEachIndexed { index, planItemBean ->
            if (planItemBean.personPlanItemId ==personPlanItemId ){
                itemIndex = index
            }
        }
        return itemIndex
    }

}