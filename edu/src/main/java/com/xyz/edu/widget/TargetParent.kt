package com.xyz.edu.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.Nullable
import androidx.annotation.RawRes
import com.blankj.utilcode.util.ConvertUtils
import com.bumptech.glide.Glide
import com.xyz.base.utils.L
import com.xyz.edu.databinding.ViewTargetBinding
import kotlin.math.max

class TargetParent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr){

    private val mBinding = ViewTargetBinding.inflate(LayoutInflater.from(context),this,true)
    var index: Int = -1

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

    }

    fun addSource(source: MovingCardView){
        mBinding.rotateView.apply {
            if (rotate1.isStarted||rotate2.isStarted){
                L.i("动画已经在动了")
                return
            }
            this.addSource(source)
        }
    }

    fun setImg(@RawRes @DrawableRes resourceId: Int){
        Glide.with(context)
            .load(resourceId)
            .into(mBinding.ivTarget)
    }
}