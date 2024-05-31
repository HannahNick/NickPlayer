package com.nick.vod.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.nick.base.vo.MusicVo
import com.nick.vod.R

class LiveAdapter: BaseQuickAdapter<MusicVo, BaseViewHolder>(R.layout.adapter_live_item) {
    override fun convert(holder: BaseViewHolder, item: MusicVo) {
        holder.setText(R.id.tv_name,item.songName)
    }
}