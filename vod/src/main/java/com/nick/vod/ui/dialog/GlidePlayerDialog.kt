package com.nick.vod.ui.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R
import com.nick.vod.ui.fragment.GlidePlayerFragment


class GlidePlayerDialog: DialogFragment() {

    private val glidePlayerFragment = GlidePlayerFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = dialog?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        LogUtils.i("Dialog onCreateView")
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window = dialog?.window
        window?.decorView?.setPadding(0,0,0,0)
        window?.setBackgroundDrawable(ColorDrawable())
        childFragmentManager.beginTransaction()
            .replace(R.id.root,glidePlayerFragment)
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
        LogUtils.i("Dialog onViewCreated")
    }

    fun showStatus(show: Boolean){
        val window = dialog?.window
        if (show){
            window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }else{
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

    }
}