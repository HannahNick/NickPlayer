package com.xyz.game.read

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.xyz.game.Exam
import com.xyz.game.Opt
import com.xyz.game.R
import com.xyz.game.TitleLayout
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

class Read : AppCompatActivity() {
    //按钮
    private lateinit var readBtn:ImageButton
    //媒体播放器
    private val talkPlayer = MediaPlayer()
    //标题控件
    private lateinit var titleLayout: TitleLayout
    private lateinit var topicText: TextView
    private lateinit var topic: ImageButton
    private lateinit var waiting: ImageView
    //游戏状态
    var gameState = Opt.Start
    //数据
    lateinit var exam: Exam
    private lateinit var path:String
    var count = 0
    private val handler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            when(msg.what){
                Opt.Start ->{
                    gameState = Opt.Start
                    loadData()
                    talking(exam.getanswerId().toString())
                }
                Opt.Update ->{
                    gameState = Opt.Update
                    waiting.visibility = View.VISIBLE
                    loadData()
                }
                Opt.STOP ->{
                    gameState = Opt.STOP
                    talking(exam.getanswerId().toString())
                    while (true){
                        if(!talkPlayer.isPlaying)
                            break
                    }
                    waiting.visibility = View.GONE
                    setBtn(true)
                    if(count==3)
                    {
                        finish()
                    }
                }
                Opt.End -> {
                    gameState = Opt.End
                    finish()
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        initBinder()
        getData()
    }
    private fun loadData(){
        //设置标题
        topicText.text = exam.gettitle()
        //设置按钮图片
        readBtn.setImageBitmap(BitmapFactory.decodeFile("$path${exam.getpicture().toString()}"))

    }
    private fun getData() {
        try {
            path = intent.getStringExtra("path").toString()
//            val bundle = intent.getBundleExtra("exam")
//            exam = bundle?.getSerializable("exam",Exam::class.java)!!
            val serializabledData = intent.getByteArrayExtra("exam")
            val byteArrayInputStream = ByteArrayInputStream(serializabledData)
            val objectInputStream = ObjectInputStream(byteArrayInputStream)
            exam = objectInputStream.readObject() as Exam
        }catch (e:Exception)
        {
            Log.e("read","PathOrDataWrong")
            finish()
        }
    }
    private fun initBinder(){
        titleLayout = findViewById(R.id.TitleLayout)
        topic = findViewById(R.id.topic)
        topicText = findViewById(R.id.listen_topic)
        readBtn = findViewById(R.id.readBtn)
        waiting = findViewById(R.id.waiting)
        readBtn.setOnClickListener {
            setBtn(false)
            //朗读程序

            //如果读了三次以后，就退出

                handler.sendEmptyMessage(Opt.Update)
                handler.sendEmptyMessageDelayed(Opt.STOP,200)
            count++


        }
    }
    private fun setBtn(flag:Boolean){
        readBtn.isEnabled = flag
        topic.isEnabled = flag
    }
    private fun talking(id:String)
    {
        try {
            val name = exam.getvoice()
            name?.let {
                talkPlayer.apply {
                    reset()
                    setDataSource("$path/$id.mp3")
                    prepare()
                    start()
                }
            }
        }catch (e:Exception){
            Log.e("tmq","talkingWrong")
        }
    }

    override fun onStart() {
        super.onStart()
        handler.sendEmptyMessageDelayed(Opt.Update, 1000)
        handler.sendEmptyMessageDelayed(Opt.STOP,2000)
    }

    override fun onStop() {
        super.onStop()
        talkPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        talkPlayer.release()
    }

}