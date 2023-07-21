package com.nick.music.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.nick.music.R
import com.nick.music.callback.PositionInitFinishListener
import com.nick.music.model.LyricsInfo

class KTVLyricsView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0):FrameLayout(context, attributeSet, defStyleAttr),PositionInitFinishListener {

    private val topLyricsView:TopLyricsView
    private val bottomLyricsView:BottomLyricsView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_lyrics,this,false)
        topLyricsView = view.findViewById(R.id.top_lyrics)
        bottomLyricsView = view.findViewById(R.id.bottom_lyrics)
        topLyricsView.positionInitFinishListener = this
        bottomLyricsView.positionInitFinishListener = this
        addView(view)
    }

    fun setData(lyricsInfo: LyricsInfo){
        val topMap = lyricsInfo.lyricsLineInfoTreeMap.filter { it.key%2 ==0 }.toSortedMap()
        val bottomMap = lyricsInfo.lyricsLineInfoTreeMap.filter { it.key%2 ==1 }.toSortedMap()
        topLyricsView.setData(topMap)
        bottomLyricsView.setData(bottomMap)
    }

    fun setCurrentPosition(playPosition: Long){
        topLyricsView.setCurrentPosition(playPosition)
        bottomLyricsView.setCurrentPosition(playPosition)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.layout(0, 0, child.measuredWidth, child.measuredHeight)
        }
    }

    override fun showPreView(index: Int,isTopLyrics: Boolean) {
        if (isTopLyrics){
            topLyricsView.drawNext(index+1)
        }else{
            bottomLyricsView.drawNext(index)
        }
    }

}