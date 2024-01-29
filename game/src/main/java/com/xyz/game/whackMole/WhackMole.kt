package com.xyz.game.whackMole

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
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

class WhackMole : AppCompatActivity() {
    //按钮
    private val btnSel =ArrayList<ImageButton>()
    private var btnSelSize = 0
    private var soiltext = ArrayList<TextView>()
    private val soil = ArrayList<ImageView>()

    //媒体播放器
    private val talkPlayer = MediaPlayer()

    //标题控件
    private lateinit var topicText: TextView
    private lateinit var topic: ImageButton
    private lateinit var waiting: ImageView

    //游戏状态
    var gameState = Opt.Start

    //数据
    var exam: Exam? =null
    private var answerIndex = 0
    private lateinit var btnId:ArrayList<String>
    private var path:String? =null

    private val handler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            when(msg.what){
                Opt.Start -> {
                    gameState = Opt.Start
                    //开始
                    loadData()
                    talking(exam!!.getanswerId()!!)
                    setbtn(true)
                }
                Opt.Update ->{
                    gameState = Opt.Update
                    talking(exam!!.getanswerId()!!)
                    loadData()
                    setbtn(true)
                    //更新
                    waiting.visibility = View.GONE
                }
                Opt.STOP ->{
                    gameState = Opt.STOP
                    //暂停，播放动画
                    while (true) {
                        if (!talkPlayer.isPlaying)
                            break
                    }
                }
                Opt.End ->{
                    gameState = Opt.End
                    //结束
                    finish()
                }
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.whackmole)
        getData()
        initBinder()
    }
    private fun getData() {
        path = intent.getStringExtra("path")
//        val bundle = intent.getBundleExtra("exam")
//        exam = bundle?.getSerializable("exam",Exam::class.java)
        val serializabledData = intent.getByteArrayExtra("exam")
        val byteArrayInputStream = ByteArrayInputStream(serializabledData)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        exam = objectInputStream.readObject() as Exam
        if(path==null || exam == null)
        {
            finish()
        }
    }

    private fun initBinder() {
        topic = findViewById(R.id.Listen)
        topicText = findViewById(R.id.gametitle)
        waiting = findViewById(R.id.finished_img)
        btnSel.add(findViewById(R.id.mouse1))
        btnSel.add(findViewById(R.id.mouse2))
        btnSel.add(findViewById(R.id.mouse3))
        btnSel.add(findViewById(R.id.mouse4))
        btnSel.add(findViewById(R.id.mouse5))
        soiltext.add(findViewById(R.id.soil_text1))
        soiltext.add(findViewById(R.id.soil_text2))
        soiltext.add(findViewById(R.id.soil_text3))
        soiltext.add(findViewById(R.id.soil_text4))
        soiltext.add(findViewById(R.id.soil_text5))
        for(i in 0..4)
        {
            btnSel[i].setOnClickListener {
                putdownbtn(i)
            }
        }
        topic.setOnClickListener {
            talking(exam!!.getanswerId()!!)
        }
        btnSelSize = if (exam!!.number<=5) exam!!.number else 5
        soil.add(findViewById(R.id.soil1))
        soil.add(findViewById(R.id.soil2))
        soil.add(findViewById(R.id.soil3))
        soil.add(findViewById(R.id.soil4))
        soil.add(findViewById(R.id.soil5))
        for(i in 0 until btnSelSize)
        {
            soil[i].visibility = View.VISIBLE
        }
    }
    private fun loadData(){
        exam!!.radomitem()
        btnId = ArrayList()
        topicText.text = exam!!.gettitle()
        val size = exam!!.number
        if(size <= 5)
        {
            for(i in 0 until size)
            {
                val id = exam!!.getitems()[i].id
                soiltext[i].text = exam!!.getitems()[i].title
                btnId.add(id)
                if(id == exam!!.getanswerId())
                    answerIndex = i
            }
        }
        else
        {
            var flag = false
            //先依次赋值
            for(i in 0 .. 4)
            {
                val id = exam!!.getitems()[i].id
                soiltext[i].text = exam!!.getitems()[i].title

                if(id == exam!!.getanswerId()){
                    answerIndex = i
                    btnId.add(exam!!.getanswerId()!!)
                    flag = true
                }
                else
                    btnId.add(id)
            }
            if(!flag)
            {
                answerIndex =(0 until 4).random()
                soiltext[answerIndex].text = exam!!.getanswer()
                btnId[answerIndex] = exam!!.getanswerId()!!
            }
        }
    }
    fun talking(id:String) {
        //首先获取exam中的音乐名称
        try {
            val name = exam!!.getvoice()
            if (name != null) {
                talkPlayer.reset()
                talkPlayer.setDataSource(getTalking(id))
                talkPlayer.prepare()
                talkPlayer.start()
            }
        }catch (e:Exception)
        {
            Log.e("error","No_RESOURCE:$e")
        }

    }
    private fun getTalking(music:String):String{
        return "$path/$music.mp3"
    }
    fun setbtn(flag:Boolean){
        if(flag){
            for(i in 0 until btnSelSize)
            {
                btnSel[i].visibility = View.VISIBLE
            }
            topic.isEnabled = flag
        }
        else
        {
            for(i in 0 until btnSelSize)
            {
                btnSel[i].visibility = View.INVISIBLE
            }
            topic.isEnabled = flag
        }

    }
    private fun putdownbtn(index:Int){
        btnSel[index].setImageResource(R.drawable.hitmouse)
        setbtn(false)
        talking(btnId[index])
        if(btnId[index] != exam!!.getanswerId())
        {
            waiting.setImageResource(R.drawable.wrong)
            waiting.visibility = View.VISIBLE
            handler.sendEmptyMessageDelayed(Opt.STOP,1000)
            handler.sendEmptyMessageDelayed(Opt.Update,1000)
        }
        else
        {
            waiting.setImageResource(R.drawable.right)
            waiting.visibility = View.VISIBLE
            handler.sendEmptyMessageDelayed(Opt.STOP,200)
            handler.sendEmptyMessageDelayed(Opt.End,1000)
        }

    }

    override fun onStart() {
        super.onStart()
        handler.sendEmptyMessageDelayed(Opt.Start,1000)
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