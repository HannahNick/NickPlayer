package com.xyz.edu.presenter.base

import android.content.Context
import com.xyz.base.app.mvp.IBaseContract

abstract class BaseListPresenter<VIEW : IBaseContract.IView, MODEL : IBaseContract.IModel>(
    context: Context, view: VIEW, model: MODEL
): DisposablePresenter<VIEW, MODEL>(context,view,model) {

    protected var mPageNum = 1

    fun loadMore() {
        mPageNum++
        requestListData()
    }

    fun refresh() {
        mPageNum = 1
        requestListData()
    }

    protected abstract fun requestListData()
}