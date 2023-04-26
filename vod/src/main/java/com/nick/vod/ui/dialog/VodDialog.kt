package com.nick.vod.ui.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import com.nick.music.R
import com.nick.vod.ui.fragment.VodFragment

class VodDialog: DialogFragment() {

    private val vodFragment = VodFragment()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window = dialog?.window
        window?.decorView?.setPadding(0,0,0,0)
        window?.setBackgroundDrawable(ColorDrawable())
        childFragmentManager.beginTransaction()
            .replace(R.id.root,vodFragment)
            .commitAllowingStateLoss()
        return inflater.inflate(R.layout.dialog_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<AppCompatImageView>(R.id.iv_back).setOnClickListener {
            dismissAllowingStateLoss()
        }
        val window = dialog?.window
        val lp = window?.attributes
        lp?.width = WindowManager.LayoutParams.MATCH_PARENT
        lp?.height = WindowManager.LayoutParams.MATCH_PARENT
        lp?.gravity = Gravity.BOTTOM
        lp?.windowAnimations = R.style.AnimDownToTop
        window?.attributes = lp
    }


}