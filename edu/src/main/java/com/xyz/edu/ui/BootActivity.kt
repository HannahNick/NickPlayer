package com.xyz.edu.ui

import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.ToastUtils
import com.xyz.edu.contract.IBootC
import com.xyz.edu.databinding.ActivityBootBinding
import com.xyz.edu.model.BootModel
import com.xyz.edu.presenter.BootPresenter

class BootActivity : BaseActivity<IBootC.Presenter>(), IBootC.View {

    private val mBinding by lazy { ActivityBootBinding.inflate(layoutInflater) }
    override fun createPresenter(): IBootC.Presenter {
        return BootPresenter(this,this,BootModel(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        presenter.register()

    }

    override fun loginSuccess() {

    }

    override fun loginFail() {
        ToastUtils.showLong("login fail")
    }

    override fun toVideo(url: String) {
        val intent = Intent(this,VideoActivity::class.java)
        intent.putExtra(VideoActivity.VIDEO_URL,url)
        startActivity(intent)
    }

    override fun toWordLearning(zipUrl: String) {

        startActivity(Intent(this,WordLearningActivity::class.java))
    }
}