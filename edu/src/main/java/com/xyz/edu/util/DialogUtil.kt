package com.xyz.edu.util

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

object DialogUtil {


    fun getCustomDialog(context: Context, @LayoutRes res: Int, cancelable: Boolean,gravity: Int): DialogPlus{
        return DialogPlus.newDialog(context)
            .setContentHolder(ViewHolder(res))
            .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
            .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
            .setGravity(gravity)
            .setCancelable(cancelable)
            .create()
    }
}