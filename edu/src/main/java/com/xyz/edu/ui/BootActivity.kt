package com.xyz.edu.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xyz.edu.R
import com.xyz.edu.contract.IBootC
import com.xyz.edu.contract.IHomeC
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
        setContentView(R.layout.activity_boot)
    }
}