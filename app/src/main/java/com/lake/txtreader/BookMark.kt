package com.lake.txtreader

import java.io.*

////////
// BookMark类主要是对应具体文件的书签，一个BookMark类可以有无限个书签
// 但一个BookMark至少得有一个书签项，否则就是不合法的BookMark
//////////

class BookMark {

    private var filename = "" //文件名
    private var fileLength = 0 //文件长度
    private lateinit var marks: Array<Mark?> //所有的书签
    private var valid = false //是否是合法的书签
    private lateinit var bmData: ByteArray //当前BookMark的byte[]形式

    private lateinit var bmdata: ByteArray

    constructor()

    /**
     * 将字节数组转换为BookMark对象
     * @param data byte[]
     * @return boolean
     */
    fun decodeBookMark(data: ByteArray): Boolean {
        valid = true
        bmData = data

        try {
            val bais = ByteArrayInputStream(data)
            val dis = DataInputStream(bais)
            filename = dis.readUTF()
            fileLength = dis.readInt()
            bmdata = ByteArray(dis.readInt())
            dis.readFully(bmdata)
            dis.close()
            bais.close()
            decodeMark(bmdata)
            System.gc()
        } catch (ex: IOException) {
            valid = false
        }

        return valid
    }

    /**
     * 将当前BookMark对象转换为字节数组
     * @param filename String
     * @param lenght int
     * @param marks Mark[]
     * @return byte[]
     */
    fun write(filename: String, lenght: Int, marks: Array<Mark>): ByteArray? {
        var b: ByteArray?
        try {
            val baos = ByteArrayOutputStream()
            val dos = DataOutputStream(baos)
            dos.writeUTF(filename)
            dos.writeInt(lenght)
            dos.writeInt(marks.size * 32)
            for (i in marks.indices) {
                dos.write(marks[i].getByte())
                //System.out.println(marks[i].getByte().length);
            }
            b = baos.toByteArray()
            dos.close()
            baos.close()
        } catch (ex: IOException) {
            b = null
        }

        if (b != null) {
            bmData = ByteArray(b.size)
            System.arraycopy(b, 0, bmData, 0, b.size)
        }
        return b
    }

    /**
     * 将字节数组转换为书签项
     * @param data byte[]
     */
    private fun decodeMark(data: ByteArray) {
        try {
            val bais = ByteArrayInputStream(data)
            val dis = DataInputStream(bais)
            val l = dis.available()
            if (l % 32 != 0) {
                dis.close()
                bais.close()
                valid = false
                return
            }
            val n = l / 32
            marks = arrayOfNulls(n)
            for (i in 0 until n) {
                val ch = dis.readInt()
                val off = dis.readInt()
                val b = ByteArray(24)
                dis.read(b)
                marks[i] = Mark(ch, off, b)
            }
            dis.close()
            bais.close()
        } catch (ex: IOException) {
        }
    }

    fun getFilename(): String {
        return this.filename
    }

    fun getFileLenght(): Int {
        return this.fileLength
    }

    fun getMarks(): Array<Mark?>? {
        return this.marks
    }

    fun getBytes(): ByteArray? {
        return bmData
    }

}