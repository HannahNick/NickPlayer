package com.xyz.game

import android.content.Context
import android.content.Intent

class GameStart(context:Context,path:String,json:String){
    init {
        val intent = Intent(context, Game::class.java)
        intent.putExtra("path",path)
        intent.putExtra("json",json)
        context.startActivity(intent)
    }
}