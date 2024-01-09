package com.xyz.edu.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.nick.base.ui.BaseActivity
import com.xyz.edu.contract.IHomeC
import com.xyz.edu.databinding.ActivityHomeBinding
import com.xyz.edu.model.HomeModel
import com.xyz.edu.presenter.HomePresenter

class HomeActivity : BaseActivity<IHomeC.Presenter>(),IHomeC.View {

    private val mBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    override fun createPresenter(): IHomeC.Presenter {
       return HomePresenter(this,this,HomeModel(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        Glide.with(this)
            .load("https://i2.hdslb.com/bfs/archive/6c92eb1dadedd6d1e54767c97ce5c19e53807ff1.jpg")
            .into(mBinding.ivLessonImg)
    }
}