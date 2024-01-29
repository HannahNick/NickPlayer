package com.xyz.game.leftOrRight

import android.graphics.Bitmap
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


class LeftOrRight : AppCompatActivity() {

    //按钮
    private val btnSel = ArrayList<ImageButton>()

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
    var exam: Exam? = null
    private var answerIndex = 0
    private lateinit var btnId: ArrayList<String>
    private var path: String? = null

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Opt.Start -> {
                    gameState = Opt.Start
                    //开始
                    loadData()
                    talking(exam!!.getanswerId()!!)
                }

                Opt.Update -> {
                    gameState = Opt.Update
                    talking(exam!!.getanswerId()!!)
                    loadData()
                    //更新
                    waiting.visibility = View.GONE
                    setbtn(true)
                }

                Opt.STOP -> {
                    gameState = Opt.STOP
                    //暂停，播放动画
                    while (true) {
                        if (!talkPlayer.isPlaying)
                            break
                    }
                }

                Opt.End -> {
                    gameState = Opt.End
                    //结束
                    finish()
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.leftorright)
        initBinder()
        getData()
    }

    private fun getData() {
        path = intent.getStringExtra("path")
        val serializabledData = intent.getByteArrayExtra("exam")
        val byteArrayInputStream = ByteArrayInputStream(serializabledData)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        exam = objectInputStream.readObject() as Exam
//        exam = bundle?.getSerializable("exam", Exam::class.java)
        if (path == null || exam == null) {
            finish()
        }
    }

    private fun initBinder() {
        titleLayout = findViewById(R.id.TitleLayout)
        topic = findViewById(R.id.topic)
        topicText = findViewById(R.id.listen_topic)
        btnSel.add(findViewById(R.id.left1))
        btnSel.add(findViewById(R.id.right2))
        btnSel.add(findViewById(R.id.left2))
        btnSel.add(findViewById(R.id.right1))
        waiting = findViewById(R.id.waiting)
        topic.setOnClickListener {
            talking(exam!!.getanswerId()!!)
        }
        btnSel[0].setOnClickListener {
            //按下会发声并且判断对错
            putdownbtn(0)
        }
        btnSel[1].setOnClickListener {
            putdownbtn(1)
        }
        btnSel[2].setOnClickListener {
            putdownbtn(2)
        }
        btnSel[3].setOnClickListener {
            putdownbtn(3)
        }
    }

    //根据数据更新当前页面
    fun loadData() {
        exam!!.radomitem()
        btnId = ArrayList()
        topicText.text = exam!!.gettitle()
        val size = exam!!.number
        if (size <= 4) {
            for (i in 0 until size) {
                val id = exam!!.getitems()[i].id
                btnSel[i].setImageBitmap(getBitmap(id))
                btnId.add(id)
                if (id == exam!!.getanswerId())
                    answerIndex = i
            }
        } else {
            //如果size>4，就先依次赋值
            var flag = false
            for (i in 0 until 4) {
                val id = exam!!.getitems()[i].id
                btnSel[i].setImageBitmap(getBitmap(id))
                if (id == exam!!.getanswerId()) {
                    answerIndex = i
                    btnId.add(exam!!.getanswerId()!!)
                    flag = true
                } else
                    btnId.add(id)

            }
            if (!flag) {
                answerIndex = (0 until 3).random()
                try {
                    btnSel[answerIndex].setImageBitmap(getBitmap(exam!!.getanswerId()!!))
                } catch (e: Exception) {
                    Log.e("error", "No_RESOURCE:$e")
                }

                btnId[answerIndex] = exam!!.getanswerId()!!
            }
        }

    }

    private fun getBitmap(picture: String): Bitmap {
        return BitmapFactory.decodeFile("$path/$picture.png")
    }

    private fun getTalking(music: String): String {
        return "$path/$music.mp3"
    }

    private fun putdownbtn(index: Int) {
        setbtn(false)
        talking(btnId[index])
        if (btnId[index] != exam!!.getanswerId()) {
            waiting.setImageResource(R.drawable.wrong)
            waiting.visibility = View.VISIBLE
            handler.sendEmptyMessageDelayed(Opt.STOP, 100)
            handler.sendEmptyMessageDelayed(Opt.Update, 1000)
        } else {
            handler.sendEmptyMessageDelayed(Opt.STOP, 200)
            waiting.setImageResource(R.drawable.right)
            waiting.visibility = View.VISIBLE
            handler.sendEmptyMessageDelayed(Opt.End, 1000)
        }

    }

    fun talking(id: String) {
        //首先获取exam中的音乐名称
        try {
            talkPlayer.reset()
            talkPlayer.setDataSource(getTalking(id))
            talkPlayer.prepare()
            talkPlayer.start()
        } catch (e: Exception) {
            Log.e("error", "No_RESOURCE:$e")
        }

    }

    fun setbtn(flag: Boolean) {
        for (i in btnSel) {
            i.isEnabled = flag
        }
        topic.isEnabled = flag
    }

    override fun onStart() {
        super.onStart()
        handler.sendEmptyMessageDelayed(Opt.Start, 1000)
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