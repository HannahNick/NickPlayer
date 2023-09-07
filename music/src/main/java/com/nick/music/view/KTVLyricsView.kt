package com.nick.music.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.blankj.utilcode.util.LogUtils
import com.nick.music.R
import com.nick.music.callback.PositionInitFinishListener
import com.nick.music.entity.SrcLyricsInfoVo
import com.nick.music.model.LyricsInfo
import com.nick.music.model.LyricsTag
import java.util.ArrayList

class KTVLyricsView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0):FrameLayout(context, attributeSet, defStyleAttr),PositionInitFinishListener {

    private val topLyricsView:TopLyricsView
    private val bottomLyricsView:BottomLyricsView
    /**
     * 当前播放的歌曲名
     */
    var mTitle = ""

    /**
     * 当前播放的歌手
     */
    var mActor = ""

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_lyrics,this,false)
        topLyricsView = view.findViewById(R.id.top_lyrics)
        bottomLyricsView = view.findViewById(R.id.bottom_lyrics)
        topLyricsView.positionInitFinishListener = this
        bottomLyricsView.positionInitFinishListener = this
        addView(view)
    }

    fun setData(lyricsInfo: LyricsInfo){

        val topTranslateLrcLineInfo = lyricsInfo.translateLrcLineInfos?.filterIndexed { index, _ -> index%2 ==0 }?:ArrayList()
        val bottomTranslateLrcLineInfo = lyricsInfo.translateLrcLineInfos?.filterIndexed { index, _ -> index%2 ==1 }?:ArrayList()
        val topTransliterationLrcLineInfo = lyricsInfo.transliterationLrcLineInfos?.filterIndexed { index, _ -> index % 2 == 0 }?:ArrayList()
        val bottomTransliterationLrcLineInfo = lyricsInfo.transliterationLrcLineInfos?.filterIndexed { index, _ -> index%2 ==1 }?:ArrayList()

        val topOriginal = lyricsInfo.lyricsLineInfoTreeMap.filter { it.key%2 ==0 }.toSortedMap()
        val bottomOriginal = lyricsInfo.lyricsLineInfoTreeMap.filter { it.key%2 ==1 }.toSortedMap()
        val title = lyricsInfo.lyricsTags[LyricsTag.TAG_TITLE] as String
        val actor = lyricsInfo.lyricsTags[LyricsTag.TAG_ARTIST] as String
        if (mTitle == title && mActor == actor){
            LogUtils.e("data Has set title: $mTitle, actor: $mActor")
//            return
        }
        mTitle = title
        mActor = actor
        lyricsInfo.initColor()

        topLyricsView.setData(topOriginal,topTranslateLrcLineInfo,topTransliterationLrcLineInfo)
        bottomLyricsView.setData(bottomOriginal,bottomTranslateLrcLineInfo,bottomTransliterationLrcLineInfo)
    }

    fun setCurrentPosition(playPosition: Long){
        topLyricsView.setCurrentPosition(playPosition)
        bottomLyricsView.setCurrentPosition(playPosition)
    }

    fun release(){
        topLyricsView.release()
        bottomLyricsView.release()
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