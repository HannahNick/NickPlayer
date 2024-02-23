package com.nick.base.manager

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import com.nick.base.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

object DialogManager {

    fun initLoading(context: Context): DialogPlus{
        val dialogPlus = DialogPlus.newDialog(context)
            .setContentHolder(ViewHolder(R.layout.layout_loading))
            .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
            .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
            .setGravity(Gravity.CENTER)
            .setCancelable(false)
            .setContentBackgroundResource(R.drawable.shape_transparent)
            .create()
        return dialogPlus
    }
}