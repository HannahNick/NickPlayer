package com.nick.music.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.ContextCompat
import com.nick.music.R

class TimeSeekBar@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    private var currentPosition: Int = 0
    private var totalDuration: Int = 0
    private var thumbText: String = "00:00 / 00:00"

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 20f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val linePaint = Paint().apply {
        color = Color.GRAY
    }

    fun setTotalDuration(duration: Int){
        this.totalDuration = duration
        max = duration
        secondaryProgress = duration
    }

    fun updateThumbText(currentPosition: Int) {
        progress = currentPosition
        this.currentPosition = currentPosition
        thumbText = formatTime(currentPosition) + " / " + formatTime(totalDuration)
        updateThumb()
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateThumb() {
        val thumb = createThumbWithText(thumbText)
        setThumb(thumb)
    }

    private fun createThumbWithText(text: String): Drawable {
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        val width = textBounds.width()+10
        val height = textBounds.height()+10
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)


        val thumbBackground = ContextCompat.getDrawable(context, R.drawable.thumb_background) // 自定义thumb背景

        thumbBackground?.setBounds(0, 0, width, height)
        thumbBackground?.draw(canvas)

        canvas.drawText(text, (width / 2).toFloat(), (height / 2 - textBounds.exactCenterY()), textPaint)

        return BitmapDrawable(resources, bitmap)
    }
}