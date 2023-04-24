package com.nick.music.util

/**
 * 音乐数据环,用于记录随机播放数据
 */
class MusicPlayNode<T>(data: T){
    //第一个
    var mFirstNode:Node<T> = Node(data)
    //最后一个
    var mLastNode:Node<T> = Node(data)
    //当前节点
    var mCurrentNode: Node<T> = Node(data)
    //总结点数
    var size: Int = 1

    /**
     * 添加数据节点并以最新的节点为当前节点
     */
    fun addAndSkipLast(newData: T){
        val newNode = Node(newData)
        newNode.last = mCurrentNode
        mCurrentNode.next = newNode
        mLastNode = newNode
        mCurrentNode = newNode
        size++
    }

    fun contains(data: T):Boolean{
        var tempNode:Node<T>? = mFirstNode
        while (tempNode?.next!=null){
            if (tempNode.data == data){
                return true
            }
            tempNode = tempNode.next
        }
        return false
    }

    fun getCurrentNodeData(): T{
        return mCurrentNode.data
    }

    fun nextData(): T{
        val nextNode = mCurrentNode.next
        //如果当前是最后一个节点就跳到第一个节点
        if (nextNode==null){
            mCurrentNode = mFirstNode
        }
        return mFirstNode.data
    }

    fun lastData(): T{
        val lastNode = mCurrentNode.last
        if (lastNode==null){
            mCurrentNode = mLastNode
        }
        return mLastNode.data
    }

    fun reset(data: T){
        val root = Node(data)
        mFirstNode = root
        mLastNode = root
        mCurrentNode = root
        size = 1
    }


    class Node<T>(
        var data: T,
        //上一个
        var last:Node<T>? = null,
        //下一个
        var next:Node<T>? = null,
    )
}