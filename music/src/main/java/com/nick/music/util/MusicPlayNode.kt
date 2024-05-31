package com.nick.music.util

/**
 * 音乐数据环,用于记录随机播放数据
 */
class MusicPlayNode<T> {
    //第一个
    private var mFirstNode: Node<T>? = null

    //最后一个
    private var mLastNode: Node<T>? = null

    //当前节点
    private var mCurrentNode: Node<T>? = null

    //总结点数
    var size: Int = 0

    private fun initRoot(newData: T){
        val rootNode = Node(newData)
        mFirstNode = rootNode
        mLastNode = rootNode
        mCurrentNode = rootNode
        rootNode.last = mFirstNode
        rootNode.next = mLastNode
        size++
    }

    /**
     * 添加数据节点并以最新的节点为当前节点
     */
    fun addAndSkipLast(newData: T) {
        add(newData)
        mCurrentNode = mLastNode
    }

    fun add(newData: T) {
        if (mCurrentNode==null){
            initRoot(newData)
            return
        }

        val newNode = Node(newData)
        newNode.last = mLastNode
        newNode.next = mFirstNode
        mLastNode!!.next = newNode
        mLastNode = newNode
        size++
    }

    fun contains(data: T): Boolean {
        val firstNode: Node<T>? = mCurrentNode
        var tempNode: Node<T>? = mCurrentNode

        while (tempNode != null) {
            if (tempNode.data == data) {
                return true
            }
            tempNode = tempNode.next
            if (tempNode == firstNode){
                break
            }
        }
        return false
    }

    fun setCurrentNode(data: T) {
        if (mCurrentNode?.data == data) {
            return
        }

        var tempNode: Node<T>? = mFirstNode
        while (tempNode != null) {
            if (tempNode.data == data){
                mCurrentNode = tempNode
                return
            }
            tempNode = tempNode.next
        }

    }

    fun getCurrentNodeData(): T {
        return mCurrentNode!!.data
    }

    fun nextData(justNextInfo: Boolean = false): T {
        val nextNode = mCurrentNode!!.next
        if (justNextInfo){
            return nextNode!!.data
        }
        //如果当前是最后一个节点就跳到第一个节点
        mCurrentNode = nextNode ?: mFirstNode
        return mCurrentNode!!.data
    }

    fun lastData(): T {
        val lastNode = mCurrentNode!!.last
        //如果当前是第一个节点就跳到最后一个节点
        mCurrentNode = lastNode ?: mLastNode
        return mCurrentNode!!.data
    }

    fun reset() {
        mFirstNode = null
        mLastNode = null
        mCurrentNode = null
        size = 0
    }

    /**
     * 以当前节点为起点生成有序数组
     */
    fun convertList(): List<T> {
        val newList = ArrayList<T>()
        val firstNode: Node<T>? = mCurrentNode
        var tempNode: Node<T>? = mCurrentNode
        while (tempNode != null) {
            newList.add(tempNode.data)
            tempNode = tempNode.next
            if (tempNode == firstNode){//遍历已回到起点
                break
            }
        }
        return newList
    }


    class Node<T>(
        var data: T,
        //上一个
        var last: Node<T>? = null,
        //下一个
        var next: Node<T>? = null,
    )
}