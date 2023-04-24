package com.nick.music.ui.dialog

import androidx.fragment.app.FragmentManager

object MusicDialogInstance {

    private val mMusicDialog by lazy { MusicDialog() }
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