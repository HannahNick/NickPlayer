package com.xyz.edu.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.blankj.utilcode.util.ConvertUtils
import com.xyz.base.utils.L
import com.xyz.edu.databinding.ViewTargetBinding
import kotlin.math.max

class TargetParent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr){

    private val mBinding = ViewTargetBinding.inflate(LayoutInflater.from(context),this,true)


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

    }

    fun addSource(source: MovingCardView){
        mBinding.rotateView.addSource(source)
    }
}