package com.xyz.edu.presenter

import android.content.Context
import com.xyz.base.app.mvp.BasePresenter
import com.xyz.edu.contract.IHomeC
import com.xyz.edu.contract.IVideoC

class VideoPresenter(context: Context, view: IVideoC.View, model: IVideoC.Model): BasePresenter<IVideoC.View, IVideoC.Model>(context,view, model),
    IVideoC.Presenter {
}