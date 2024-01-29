package com.xyz.game


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.xyz.game.leftOrRight.LeftOrRight
import com.xyz.game.read.Read
import com.xyz.game.whackMole.WhackMole
import com.google.gson.Gson

import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.ObjectOutputStream
import java.lang.StringBuilder
class Game() : AppCompatActivity() {
    val TAG = "tmq"
    var path:String = ""
    var json:String = ""
    private var dataList: Data? = null
    private lateinit var intentTo: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        path = filesDir.path.toString()+"/PaperPig/"


        dataList = getData()
        dataList?.let {
            if (dataList!!.type == 1) {
                packageDataToExams(dataList!!, LeftOrRight::class.java)
                startActivity(intentTo)
                finish()
            }
            if (dataList!!.type == 2) {
                packageDataToExams(dataList!!, WhackMole::class.java)
                startActivity(intentTo)
                finish()
            }
            if (dataList!!.type == 3) {
                packageDataToExams(dataList!!, Read::class.java)
                startActivity(intentTo)
                finish()
            }
        }
    }
    private fun <T> packageDataToExams(dataList: Data, cls: Class<T>) {
        val items = dataList.items
        //exam的结构为：
        //题目、答案、答案的音频、答案的图片
        //其他干扰项
        val exam = Exam(
            mapOf(
                "title" to dataList.title,
                "voice" to dataList.answer + ".mp3",
                "picture" to dataList.answer + ".png",
                "answer" to dataList.answer
            ), items
        )
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objArrayOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objArrayOutputStream.writeObject(exam)
        val serializabledData = byteArrayOutputStream.toByteArray()

        intentTo = Intent(this, cls)
//        val bundle = Bundle()
//        bundle.putSerializable("exam", exam)
//        intent_to.putExtra("exam", bundle)
        intentTo.putExtra("exam",serializabledData)
        intentTo.putExtra("path", path)

    }
    private fun getData(): Data? {
        try {
            path = intent.getStringExtra("path").toString()+"/"
            json = intent.getStringExtra("json").toString()
            Log.d(TAG,filesDir.path.toString())
            val filePath = "$path/$json"
            val fileReader = FileReader(filePath)
            val bufferedReader = BufferedReader(fileReader)
            val content = StringBuilder()
            var line = bufferedReader.readLine()
            while (line != null) {
                content.append(line)
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
            val jsonContent = content.toString()
            val gson = Gson()
            return gson.fromJson(jsonContent, Data::class.java)
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        }
        return null
    }


}