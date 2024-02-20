package com.xyz.edu.ui

import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.ToastUtils
import com.nick.base.manager.PlanManager
import com.xyz.base.utils.L
import com.xyz.edu.contract.IBootC
import com.xyz.edu.databinding.ActivityBootBinding
import com.xyz.edu.model.BootModel
import com.xyz.edu.presenter.BootPresenter

class BootActivity : BaseActivity<IBootC.Presenter>(), IBootC.View, PlanManager.PreInitDataCallBack {

    private val mBinding by lazy { ActivityBootBinding.inflate(layoutInflater) }
    override fun createPresenter(): IBootC.Presenter {
        return BootPresenter(this,this,BootModel(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        L.i("L.isEnable:${L.isEnable}")
        PlanManager.registerDataCallBack(this)
        presenter.login()

    }

    override fun loginSuccess() {
        startActivity(Intent(this,PlanActivity::class.java))
        finish()
    }

    override fun loginFail() {
        ToastUtils.showLong("login fail")
    }

    fun toVideo(url: String) {
        val intent = Intent(this,VideoActivity::class.java)
        intent.putExtra(VideoActivity.VIDEO_URL,url)
        startActivity(intent)
    }

    override fun toWordLearning(zipUrl: String) {
        val intent = Intent(this, WordLearningActivity::class.java)
        intent.putExtra(WordLearningActivity.ZIP_URL,zipUrl)

        startActivity(intent)
    }

    override fun dataInitFinish() {
        PlanManager.toNextPlanItem(this,-1)
    }

    override fun preInitDataFinish() {
        finish()
    }
}