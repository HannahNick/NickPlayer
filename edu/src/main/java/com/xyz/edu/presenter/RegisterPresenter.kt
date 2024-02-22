package com.xyz.edu.presenter

import android.content.Context
import com.blankj.utilcode.util.SPUtils
import com.nick.base.constants.Constant
import com.nick.base.manager.PlanManager
import com.nick.base.manager.UserManager
import com.xyz.auth.api.IAuthService
import com.xyz.base.app.rx.io2Main
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.utils.L
import com.xyz.edu.contract.IRegisterC
import com.xyz.edu.model.BootModel
import com.xyz.edu.model.PlanModel
import com.xyz.edu.presenter.base.DisposablePresenter
import com.xyz.edu.util.ListAdapterUtil

class RegisterPresenter(context: Context,view: IRegisterC.View,model: IRegisterC.Model): DisposablePresenter<IRegisterC.View,IRegisterC.Model>(context, view, model),IRegisterC.Presenter {
    private val mBootModel by lazy { BootModel(context) }
    private val mPlanModel by lazy { PlanModel(context) }

    override fun register(
        personName: String,
        personAccount: String,
        password: String,
        age: Int,
        basics: Int,
        sex: Int
    ) {
        view.showLoading(true)
        model.register(personName, personAccount, password, age, basics, sex)
            .io2Main()
            .subscribe({
                val code = it.code
                if (code!="200"){
                    view.registerFail(it.message)
                    return@subscribe
                }
                login(personAccount, password)
            },{
                it.printStackTrace()
                view.showLoading(false)
            }).apply { compositeDisposable.add(this) }
    }

    private fun login(personAccount: String,
                      password: String,){
        mBootModel.login(personAccount, password)
            .io2Main()
            .subscribe({
                val code = it.code
                if (code!="200"){
                    view.registerFail(it.message)
                    return@subscribe
                }
                L.i("IAuthService login start")
                IAuthService.create(context)?.login("eduLogin")
                L.i("IAuthService login end")
                val result = it.result
                UserManager.personId = result.personId
                UserManager.personPlanItemId = result.personPlanItemId
                UserManager.personPlanId = result.personPlanId
//                view.loginSuccess()
                SPUtils.getInstance().put(Constant.USER_ACCOUNT,personAccount)
                SPUtils.getInstance().put(Constant.PASSWORD,password)
                getPersonPlanList()

            },{
                it.printStackTrace()
                view.showLoading(false)
            }).apply { compositeDisposable.add(this) }
    }

    /**
     * 获取学习列表
     */
    private fun requestListData(personPlanItemId: String) {
        mPlanModel.getPersonPlanItemList(UserManager.personPlanId,1, ListAdapterUtil.PAGE_SIZE)
            .io2Main()
            .subscribe({
                val code = it.code
                if (code!="200"){
                    view.registerFail(it.message)
                    return@subscribe
                }
                val itemIndex = checkHaveStudyItem(personPlanItemId,it.result.pageContent)
                PlanManager.initData(it.result.pageContent,itemIndex)
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
        mPlanModel.getPersonPlanList(UserManager.personId,1, ListAdapterUtil.PAGE_SIZE)
            .io2Main()
            .subscribe({
                L.i("getPersonPlanList data:$it")
                val code = it.code
                if (code!="200"){
                    view.registerFail(it.message)
                    return@subscribe
                }
                requestListData(it.result.pageContent[0].personPlanItemId)
            },{
                it.printStackTrace()
                view.showLoading(false)
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