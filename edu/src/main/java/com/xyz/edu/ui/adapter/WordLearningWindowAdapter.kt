package com.xyz.edu.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.xyz.edu.R
import com.xyz.edu.databinding.LayoutZipItemBinding
import com.xyz.edu.vo.WordLearningWindowItemVo

class WordLearningWindowAdapter(): BaseQuickAdapter<WordLearningWindowItemVo, BaseViewHolder>(R.layout.layout_zip_item) {
    override fun convert(helper: BaseViewHolder?, item: WordLearningWindowItemVo?) {
        TODO("Not yet implemented")
    }


}