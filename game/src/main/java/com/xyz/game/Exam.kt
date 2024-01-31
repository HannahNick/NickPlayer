package com.xyz.game

import java.io.Serializable

class Exam(ques:Map<String,String?>,private val items:ArrayList<Item>): Serializable {
    private val title:String? = ques["title"]        //这是题目
    private val voice:String? = ques["voice"]        //这是答案的读法
    private val picture:String? = ques["picture"]    //这是答案相关的的图片
    private val questionId:String? = ques["questionId"]
    private val answerId:String? = ques["answer"]     //这是答案相关的图片
    private var itemradom = items
    private lateinit var answer:String
    val number = items.size
    init {
        for(i in 0 until number){
            if(items[i].id == answerId)
                answer = items[i].title
        }
    }
    fun gettitle()=title
    fun getvoice()=voice
    fun getpicture()=picture
    fun getanswerId()=answerId
    fun getanswer()=answer
    fun getquestionId()=questionId
    fun getitems():ArrayList<Item>{
        return itemradom
    }
    fun radomitem()
    {
        itemradom = ArrayList()
        val indexs = ArrayList<Int>()
        for(i in (0 until number)){
            indexs.add(i)
        }
        while(itemradom.size < number)
        {
            val index = indexs.random()
            itemradom.add(items[index])
            indexs.remove(index)
        }
    }


}