package com.xyz.edu.presenter

import android.content.Context
import android.text.TextUtils
import com.blankj.utilcode.util.SPUtils
import com.nick.base.constants.Constant
import com.nick.base.manager.PlanManager
import com.xyz.auth.api.IAuthService
import com.xyz.base.app.rx.io2Main
import com.xyz.base.service.edu.bean.PlanItemBean
import com.xyz.base.utils.L
import com.xyz.edu.contract.IBootC
import com.nick.base.manager.UserManager
import com.xyz.edu.model.PlanModel
import com.xyz.edu.presenter.base.DisposablePresenter
import com.xyz.edu.util.ListAdapterUtil

class BootPresenter(context: Context, view: IBootC.View, model: IBootC.Model): DisposablePresenter<IBootC.View, IBootC.Model>(context,view, model),
    IBootC.Presenter {
    val planModel = PlanModel(context)
    override fun login() {
        val personAccount = SPUtils.getInstance().getString(Constant.USER_ACCOUNT)
        val password = SPUtils.getInstance().getString(Constant.PASSWORD)
        if (TextUtils.isEmpty(personAccount)||TextUtils.isEmpty(password)){
            PlanManager.toLogin()
            view.close()
            return
        }

        model.login("13714570139","12345678")
            .io2Main()
            .subscribe({
                L.i("IAuthService login start")
                IAuthService.create(context)?.login("eduLogin")
                L.i("IAuthService login end")
                val result = it.result
                UserManager.personId = result.personId
                UserManager.personPlanItemId = result.personPlanItemId
                UserManager.personPlanId = result.personPlanId
//                view.loginSuccess()
                getPersonPlanList()
//                requestListData()
//                PlanManager.toVideo(context,"","","",1)
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

    /**
     * 获取学习列表
     */
    private fun requestListData(personPlanItemId: String) {
        planModel.getPersonPlanItemList(UserManager.personPlanId,1, ListAdapterUtil.PAGE_SIZE)
            .io2Main()
            .subscribe({
                val itemIndex = checkHaveStudyItem(personPlanItemId,it.result.pageContent)
                PlanManager.initData(context,it.result.pageContent,itemIndex)
                L.i("requestListData: startIndex $itemIndex")
                PlanManager.toHome(context)
                view.close()
//                PlanManager.startItem(context,itemIndex)
            },{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }

    /**
     * 获取当前用户学习计划
     */
    private fun getPersonPlanList(){
        planModel.getPersonPlanList(UserManager.personId,1,ListAdapterUtil.PAGE_SIZE)
            .io2Main()
            .subscribe({
                L.i("getPersonPlanList data:$it")
                requestListData(it.result.pageContent[0].personPlanItemId)
            },{
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