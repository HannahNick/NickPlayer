package com.xyz.edu.presenter

import android.content.Context
import androidx.annotation.CallSuper
import com.xyz.base.app.mvp.BasePresenter
import com.xyz.base.app.mvp.IBaseContract
import com.xyz.base.utils.L
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class DisposablePresenter<VIEW : IBaseContract.IView, MODEL : IBaseContract.IModel>(
    context: Context, view: VIEW, model: MODEL
) : BasePresenter<VIEW, MODEL>(context, view, model), CoroutineScope by MainScope() {

    private var innerCompositeDisposable: CompositeDisposable? = null
    protected val compositeDisposable
        get() = innerCompositeDisposable ?: CompositeDisposable().apply {
            innerCompositeDisposable = this
        }
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        L.e("coroutine: error ${throwable.message}")
    }

    @CallSuper
    override fun onRelease() {
        super.onRelease()
        innerCompositeDisposable?.dispose()
        innerCompositeDisposable = null
        cancel()
    }

    protected fun Disposable.applyAddTo() {
        compositeDisposable.add(this)
    }
}