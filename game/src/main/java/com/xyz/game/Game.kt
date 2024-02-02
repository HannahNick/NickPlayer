package com.xyz.game


import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import com.nick.base.router.BaseRouter
import com.xyz.game.leftOrRight.LeftOrRight
import com.xyz.game.read.Read
import com.xyz.game.whackMole.WhackMole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.ObjectOutputStream

@Route(path = BaseRouter.AROUTER_GAME)
class Game() : AppCompatActivity() {
    private lateinit var intentLauncher:ActivityResultLauncher<Intent>
    val TAG = "tmq"
    var path:String = ""
    var json:String = ""
    private var dataList: Data? = null
    private lateinit var intentTo: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        path = filesDir.path.toString()+"/PaperPig/"
        intentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 处理返回的结果
            if (result.resultCode == Activity.RESULT_OK) {
                intentLauncher.launch(intentTo)
            }
            else
            {
                Log.d("tmq","!!!!reback")

                finish()
            }
        }
        getData()
        dataList?.let {
            if (dataList!!.type == 1) {
                packageDataToExams(dataList!!, LeftOrRight::class.java)
                intentLauncher.launch(intentTo)
            }
            if (dataList!!.type == 2) {
                packageDataToExams(dataList!!, WhackMole::class.java)
                intentLauncher.launch(intentTo)
            }
            if (dataList!!.type == 3) {
                packageDataToExams(dataList!!, Read::class.java)
                intentLauncher.launch(intentTo)
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
                "questionId" to dataList.questionId,
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
        intentTo.putExtra("itemIndex", intent.getIntExtra("itemIndex",0))

    }
    private fun getData(){
        try {
            lifecycleScope.launch() {
                path = intent.getStringExtra("path").toString() + "/"
                json = intent.getStringExtra("json").toString()
                Log.d(TAG, filesDir.path.toString())
                Log.d(TAG, "Thread: ${Thread.currentThread().name}")
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
                dataList = gson.fromJson(jsonContent, Data::class.java)
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        }
    }

}