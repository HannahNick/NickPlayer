package com.xyz.game

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class TitleLayout(context:Context, attrs: AttributeSet) :LinearLayout(context,attrs){
    init {

        LayoutInflater.from(context).inflate(R.layout.title,this)
        val activity =context as Activity

        findViewById<Button>(R.id.back).setOnClickListener{
            activity.setResult(Activity.RESULT_CANCELED)
            activity.finish()
        }
        findViewById<Button>(R.id.replay).setOnClickListener {
            Log.d("tmq","clicked replay")

            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }
    }
}