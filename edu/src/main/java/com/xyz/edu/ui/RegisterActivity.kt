package com.xyz.edu.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xyz.edu.R
import com.xyz.edu.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private val mBinding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)


    }
}