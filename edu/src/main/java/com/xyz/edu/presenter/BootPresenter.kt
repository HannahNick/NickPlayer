package com.xyz.edu.presenter

import android.content.Context
import com.xyz.base.app.rx.io2Main
import com.xyz.edu.contract.IBootC
import com.xyz.edu.manager.UserManager

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
                getPersonPlanItemList(1,10)
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
            },{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }

    override fun getPersonPlanItemList(pageNum: Int, pageSize: Int) {
        model.getPersonPlanItemList(UserManager.personId,UserManager.personPlanId,pageNum, pageSize)
            .io2Main()
            .subscribe({
                //视频结果
                val videoResult = it.result.pageContent.filter { it.contentType == 1 }.first()
                view.toVideo(videoResult.contentUrl)
                //单词结果
                val wordResult = it.result.pageContent.filter { it.contentType == 5 }.first()
                //短语结果
                val phraseResult = it.result.pageContent.filter { it.contentType == 6 }.first()

            },{
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }


}