package com.xyz.game

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class TitleLayout(context:Context, attrs: AttributeSet) :LinearLayout(context,attrs){
    init {
        LayoutInflater.from(context).inflate(R.layout.title,this)
        findViewById<Button>(R.id.back).setOnClickListener{
            val activity =context as Activity
            activity.finish()
        }
    }
}