package com.nick.base.util

import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.SPUtils
import com.xyz.base.utils.L
import java.io.File

class LRUFileCache(val maxEntries: Int) {
    private lateinit var mBasePath: String

    private val cache = object : LinkedHashMap<String, File>(maxEntries + 1, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, File>?): Boolean {
            return if (size > maxEntries) {
                deleteFile(eldest?.value)
                true
            } else {
                false
            }
        }
    }

    fun initializeCache(basePath: String) {
        mBasePath = basePath
        val planFile = File("$mBasePath/plan")
        planFile.listFiles()?.forEach {
            L.i("planFile: ${it.absolutePath}")
        }

        val cachedFilesStr = SPUtils.getInstance().getString(KEY_CACHE_LIST)
        cachedFilesStr?.split(",")?.forEach { filePath ->
            L.i("initializeCache: $filePath")
            File(filePath).takeIf { it.exists() }?.let { file ->
                cache[filePath] = file
            }
        }
    }

    fun cacheFile(file: File) {
        L.i("cacheFile ${file.absolutePath}")
        cache[file.absolutePath] = file
        saveCacheState()
    }

    private fun deleteFile(zipFile: File?){
        FileUtils.delete(zipFile)
        //删掉已解压的目录
        FileUtils.deleteAllInDir("$$mBasePath/plan/${zipFile?.name}")
    }

    private fun saveCacheState() {
        SPUtils.getInstance().put(KEY_CACHE_LIST, cache.keys.joinToString(","))
    }

    companion object {
        private const val PREFS_NAME = "LRUFileCachePrefs"
        private const val KEY_CACHE_LIST = "CacheFileList"
    }

    // 其他需要的方法...
}