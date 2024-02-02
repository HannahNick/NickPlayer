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
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.xyz.game.AppActivity
import com.xyz.game.Exam
import com.xyz.game.Opt
import com.xyz.game.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

class Read : AppActivity() {
    val NUM = 4

    //图片
    private lateinit var picture: ImageView
    private lateinit var word: TextView
    private lateinit var pword: TextView

    //媒体播放器
    private val talkPlayer = MediaPlayer()

    //标题控件
    private lateinit var topicText: TextView
    private lateinit var topic: ImageView

    //游戏状态
    var gameState = Opt.Start

    //数据
    lateinit var exam: Exam
    private lateinit var path: String
    var count = 0
    var btnflag = false
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Opt.Start -> {
                    gameState = Opt.Start

                    talking(exam.getquestionId().toString())
                }

                Opt.Update -> {
                    gameState = Opt.Update
                    talking(exam.getanswerId().toString())
                }

                Opt.STOP -> {
                    gameState = Opt.STOP
                    try {
                        while (true) {
                            if (!talkPlayer.isPlaying)
                                break

                        }
                    } catch (e: Exception) {
                        Log.e("tmq", "too many click")
                    }
                    count++
                    if (count >= NUM) {
                        finish(false)
                    }
                    setBtn(true)
                }

                Opt.End -> {
                    gameState = Opt.End
                    finish(false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        initBinder()
        getData()
        loadData()
    }

    private fun loadData() {
        //设置标题
        topicText.text = exam.gettitle()
        word.text = exam.getanswer()
        pword.text = exam.getanswer()
        setBtn(true)
        settopic()
        //设置按钮图片
        picture.setImageBitmap(BitmapFactory.decodeFile("$path${exam.getpicture().toString()}"))

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
        } catch (e: Exception) {
            Log.e("read", "PathOrDataWrong")
            handler.sendEmptyMessage(Opt.End)
        }
    }

    private fun initBinder() {
        topic = findViewById(R.id.topic)
        word = findViewById(R.id.word)
        pword = findViewById(R.id.pword)
        topicText = findViewById(R.id.listen_topic)
        picture = findViewById(R.id.readBtn)
        findViewById<Button>(R.id.back).setOnClickListener {
            finish()
        }
        picture.setOnClickListener {
            if (btnflag) {
                setBtn(false)
                //朗读程序
                //如果读了三次以后，就退出
                handler.sendEmptyMessage(Opt.Update)
                handler.sendEmptyMessageDelayed(Opt.STOP, 1000)
            }
        }
        topic.setOnClickListener {
            talking(exam.getquestionId().toString())
        }
    }

    private fun setBtn(flag: Boolean) {
        picture.isEnabled = flag
        topic.isEnabled = flag
        btnflag = flag
        if (flag) {
            word.visibility = View.VISIBLE
            pword.visibility = View.INVISIBLE
        } else {
            word.visibility = View.INVISIBLE
            pword.visibility = View.VISIBLE
        }

    }

    private fun talking(id: String) {
        if (count < NUM) {
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
            } catch (e: Exception) {
                Log.e("tmq", "talkingWrong")
            }
        }
    }

    fun settopic() {
        val topicObserver = topic.viewTreeObserver
        topicObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                val layoutParams = topic.layoutParams
                layoutParams.width = topicText.width + 200
                Log.d("tmq", "View" + layoutParams.width.toString())
                Log.d("tmq", "Text" + topicText.width)
                topic.layoutParams = layoutParams
                topic.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        setBtn(false)
        handler.sendEmptyMessageDelayed(Opt.Start, 100)
//        handler.sendEmptyMessageDelayed(Opt.Update, 100)
        handler.sendEmptyMessageDelayed(Opt.STOP, 120)
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