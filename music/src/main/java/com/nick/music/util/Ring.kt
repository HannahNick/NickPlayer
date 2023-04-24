package com.nick.music.util

class Ring<T> {
    private val items = mutableListOf<T>()
    private var currentIndex = 0

    fun addAll(list: List<T>){
        items.addAll(list)
    }

    fun removeAll(){
        items.clear()
        currentIndex = 0
    }

    fun add(item: T) {
        items.add(item)
    }

    fun remove(index: Int): T? {
        if (index < 0 || index >= items.size) {
            return null
        }

        if (index < currentIndex) {
            currentIndex--
        } else if (index == currentIndex) {
            currentIndex = 0
        }

        return items.removeAt(index)
    }

    fun getCurrent(): T? {
        return if (items.isNotEmpty()) {
            items[currentIndex]
        } else {
            null
        }
    }

    fun next(): T? {
        return if (items.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % items.size
            items[currentIndex]
        } else {
            null
        }
    }
}