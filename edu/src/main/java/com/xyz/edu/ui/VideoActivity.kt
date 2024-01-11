package com.xyz.edu.ui

import android.os.Bundle
import com.blankj.utilcode.util.BarUtils
import com.nick.vod.ui.fragment.VodFragment
import com.xyz.edu.R
import com.xyz.edu.contract.IVideoC
import com.xyz.edu.databinding.ActivityVideoBinding
import com.xyz.edu.model.VideoModel
import com.xyz.edu.presenter.VideoPresenter

class VideoActivity : BaseActivity<IVideoC.Presenter>(), IVideoC.View {

    private val mBinding by lazy { ActivityVideoBinding.inflate(layoutInflater) }

    override fun createPresenter(): IVideoC.Presenter {
        return VideoPresenter(this,this,VideoModel(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        BarUtils.setStatusBarVisibility(this,false)
        initView()
    }

    private fun initView(){
        val vodFragment = VodFragment.newInstance(arrayListOf("${filesDir.absolutePath}/vod/abc.mp4"))
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_contain, vodFragment)
        transaction.commit()
    }
}