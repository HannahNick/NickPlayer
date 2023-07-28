package com.nick.music.ui.dialog

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
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R
import com.nick.music.ui.fragment.MusicFragment

class MusicDialog: DialogFragment() {

    private val musicFragment = MusicFragment()

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
            .replace(R.id.root,musicFragment)
            .commitAllowingStateLoss()
        return inflater.inflate(R.layout.dialog_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog?.window
        val lp = window?.attributes
        lp?.width = WindowManager.LayoutParams.MATCH_PARENT
        lp?.height = WindowManager.LayoutParams.MATCH_PARENT
        lp?.gravity = Gravity.BOTTOM
        lp?.windowAnimations = R.style.AnimDownToTop
        window?.attributes = lp
    }

    override fun onDestroy() {
        super.onDestroy()

    }


}