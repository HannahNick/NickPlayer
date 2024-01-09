package com.xyz.edu.ui

import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.nick.base.R
import com.xyz.base.app.mvp.IBaseContract
import com.xyz.base.app.mvp.MvpActivity

abstract class BaseActivity<T : IBaseContract.IPresenter>: MvpActivity<T>() {

    /**
     * 顶部toolbar
     */
    protected lateinit var mToolbar: Toolbar
    protected lateinit var mToolbarTitle: TextView

    //不显示toolber
    protected val MODE_NONE = 0

    //有返回按钮
    protected val MODE_BACK = 1

    //白色返回按钮
    protected val MODE_WHITHBACK = 2


    /**
     * 初始化顶部标题栏
     *
     * @param title
     * @param mode
     */
    protected open fun setUpToolbar(title: String?, mode: Int) {
        if (mode != MODE_NONE) {
            mToolbar = findViewById(R.id.toolbar)
            mToolbarTitle = findViewById(R.id.toolbar_title)
            if (mode == MODE_BACK) {
                mToolbar.setNavigationIcon(R.drawable.arrow_back)
            } else if (mode == MODE_WHITHBACK) {
                mToolbar.setNavigationIcon(R.drawable.white_return)
            }
            mToolbar.setNavigationOnClickListener { view -> onNavigationBtnClicked() }
            mToolbarTitle.text = title
        }
    }

    /**
     * 顶部标题栏返回
     */
    protected open fun onNavigationBtnClicked() {
        finish()
    }

}