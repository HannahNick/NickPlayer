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
import com.xyz.edu.contract.IRegisterC
import com.xyz.edu.databinding.ActivityRegisterBinding
import com.xyz.edu.model.RegisterModel
import com.xyz.edu.presenter.RegisterPresenter

@Route(path = BaseRouter.REGISTER)
class RegisterActivity : BaseActivity<IRegisterC.Presenter>(),IRegisterC.View {

    private val mBinding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }
    override fun createPresenter(): IRegisterC.Presenter {
        return RegisterPresenter(this,this,RegisterModel(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        initListener()
    }

    private fun initListener(){
        mBinding.apply {
            btnRegister.setOnClickListener {
                val userAccount = edtUserAccount.text.toString()
                if (TextUtils.isEmpty(userAccount)){
                    ToastUtils.showLong("账号不能为空")
                    return@setOnClickListener
                }
                val pwd = edtPwd.text.toString()
                if (TextUtils.isEmpty(pwd)){
                    ToastUtils.showLong("密码不能为空")
                    return@setOnClickListener
                }
                val userName = edtUserName.text.toString()
                if (TextUtils.isEmpty(userName)){
                    ToastUtils.showLong("密码不能为空")
                    return@setOnClickListener
                }
                val age = edtAge.text.toString()
                if (TextUtils.isEmpty(age)){
                    ToastUtils.showLong("密码不能为空")
                    return@setOnClickListener
                }
                val sexButtonId = rgSex.checkedRadioButtonId
                val sex = if (sexButtonId==R.id.rb_male){
                    1
                }else{
                    0
                }
                val basicButtonId = rgBasic.checkedRadioButtonId
                val basic = if (basicButtonId==R.id.rb_yes){
                    1
                }else{
                    0
                }
                presenter.register(personAccount = userAccount,
                    password = pwd,
                    personName = userName,
                    age = age.toInt(),
                    sex = sex,
                    basics = basic)
                btnRegister.isClickable = false
            }
            tvRegister.setOnClickListener {
                PlanManager.toLogin()
            }
        }
    }

    override fun registerSuccess() {
        finish()
    }

    override fun registerFail(msg: String) {
        ToastUtils.showLong(msg)
        mBinding.btnRegister.isClickable = true
        showLoading(false)
    }

    override fun showLoading(show: Boolean, loadingText: CharSequence?) {
        mBinding.aviLoading.visibility = if (show){
            View.VISIBLE
        }else{
            View.GONE
        }
    }
}