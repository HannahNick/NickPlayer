package com.nick.vod.ui.dialog

import androidx.fragment.app.FragmentManager

object VodDialogInstance {

    private val mMusicDialog by lazy { VodDialog() }
    private val TAG = "MusicDialog"

    fun show(fragmentManager: FragmentManager){
        if (!mMusicDialog.isVisible){
            mMusicDialog.show(fragmentManager,TAG)
        }
    }

    fun dismiss(){
        if (mMusicDialog.isVisible){
            mMusicDialog.dismiss()
        }
    }
}