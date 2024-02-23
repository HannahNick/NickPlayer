package com.xyz.edu.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ToastUtils
import com.nick.base.manager.PlanManager
import com.nick.base.router.BaseRouter
import com.xyz.edu.R
import com.xyz.edu.contract.ILoginC
import com.xyz.edu.databinding.ActivityLoginBinding
import com.xyz.edu.model.LoginModel
import com.xyz.edu.presenter.LoginPresenter

@Route(path = BaseRouter.LOGIN)
class LoginActivity : BaseActivity<ILoginC.Presenter>(),ILoginC.View {
    private val mBinding by lazy{ActivityLoginBinding.inflate(layoutInflater)}
    override fun createPresenter(): ILoginC.Presenter {
        return LoginPresenter(this,this,LoginModel(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initListener()

    }

    private fun initListener(){
        mBinding.btnLogin.setOnClickListener {
            val username = mBinding.edtUsername.text.toString()
            if (TextUtils.isEmpty(username)){
                ToastUtils.showLong("用户名不能为空")
                return@setOnClickListener
            }
            val pwd = mBinding.edtPwd.text.toString()
            if (TextUtils.isEmpty(pwd)){
                ToastUtils.showLong("密码不能为空")
                return@setOnClickListener
            }
            presenter.login(username,pwd)
        }
        mBinding.tvRegister.setOnClickListener {
            PlanManager.toRegister()
        }
    }

    override fun loginSuccess() {
        finish()
    }

    override fun loginFail(msg: String) {
        ToastUtils.showLong(msg)
    }

    override fun showLoading(show: Boolean, loadingText: CharSequence?) {
        mBinding.aviLoading.visibility = if (show){
            View.VISIBLE
        }else{
            View.GONE
        }
    }
}