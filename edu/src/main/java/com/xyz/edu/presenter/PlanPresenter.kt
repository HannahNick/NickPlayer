package com.xyz.edu.presenter

import android.content.Context
import com.xyz.base.app.mvp.BasePresenter
import com.xyz.base.app.rx.io2Main
import com.xyz.edu.contract.IPlanC
import com.xyz.edu.manager.UserManager
import com.xyz.edu.presenter.base.BaseListPresenter
import com.xyz.edu.util.ListAdapterUtil

class PlanPresenter(context: Context, view: IPlanC.View, model: IPlanC.Model): BaseListPresenter<IPlanC.View, IPlanC.Model>(context,view, model),
    IPlanC.Presenter {


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