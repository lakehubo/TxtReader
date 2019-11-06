package com.lake.txtreader

import java.io.File
import java.io.FileInputStream
import java.io.IOException


class TextFileReader {

    private var location = ""
    private lateinit var fileIns: FileInputStream
    private lateinit var file: File
    var offset = 0
    val DIR_INFO = 0
    val RD_INFO = 1
    val NRD_INFO = 2
    val SkipBuffer = 16384
    val BUFFER = 16384

    constructor(s: String) {
        location = s
        offset = 0
        open()
    }

    /**
     * 打开文件
     */
    fun open() {
        try {
            file = File(location)
            fileIns = FileInputStream(file)
        } catch (e: Exception) {
            println("open file error")
        }
    }

    /**
     * 读取文件
     */
    fun read(len: Int): ByteArray? {
        if (file.isDirectory)
            return null
        val abyte0 = ByteArray(len)
        val l = file.length()
        var len = len
        if (offset + len > l) {
            len = (l - offset).toInt()
        }
        if (BUFFER > len) {//一次性读
            try {
                fileIns.read(abyte0)
                offset += len;
            } catch (e: IOException) {
                return null
            }
        } else {//分段读
            var datapos = 0
            val times = len / BUFFER
            var i = 0
            while (i < times) {
                val buffer = ByteArray(BUFFER)
                try {
                    fileIns.read(buffer)
                    System.arraycopy(buffer, 0, abyte0, datapos, BUFFER)
                    datapos += BUFFER
                    offset += BUFFER
                } catch (e: IOException) {
                    return null
                }
                i++
            }
            val rest = len - datapos
            val buffer = ByteArray(rest)
            try {
                fileIns.read(buffer)
                System.arraycopy(buffer, 0, abyte0, datapos, rest)
                offset += rest
            } catch (e: IOException) {
            }
        }
        System.gc()
        return abyte0
    }

    /**
     * 跳过若干位
     */
    fun skip(len: Int) {
        if (file.isDirectory)
            return
        offset += len
        if (len < 0) {
            close()
            open()
            fastSkip(offset)
            return
        }
    }

    /**
     * 快速skip，由于MOTO的skip效率远远低于read，因此将skip改为read
     */
    fun fastSkip(len: Int) {
        if (len <= 0)
            return
        var b: ByteArray?
        if (SkipBuffer > len) { //一次性读入
            b = ByteArray(len)
            try {
                fileIns.read(b)
            } catch (ex: IOException) {
            }

        } else { //分段读
            val times = len / SkipBuffer
            for (i in 0 until times) {
                b = ByteArray(SkipBuffer)
                try {
                    fileIns.read(b)
                } catch (ex1: IOException) {
                }

            }
            val rest = len - SkipBuffer * times
            b = ByteArray(rest)
            try {
                fileIns.read(b)
            } catch (ex2: IOException) {
            }

        }
        b = null
        System.gc()
    }

    /**
     * 定位到文档偏移处
     * @param i int 偏移的位置
     */
    fun locate(i: Int) {
        if (i < 0 || i > file.length() - 1) {
            return
        }

        val len = i - offset
        skip(len)
    }

    /**
     * 文件大小
     */
    fun fileSize(): Long {
        var l = -1L

        try {
            l = file.length()
        } catch (ex: IOException) {
            return l
        }

        return l
    }

    /**
     * 关闭读写
     */
    fun close() {
        try {
            if (!file.isDirectory) {
                fileIns.close()
            }
        } catch (e: Exception) {

        }
    }

}