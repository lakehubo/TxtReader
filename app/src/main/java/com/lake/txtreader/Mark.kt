package com.lake.txtreader

import java.io.*

//////////////////////////
// Mark类主要是对书签项进行封装
/////////////////////////
class Mark {

    var chapter = 0 //当前章节（当格式为TXT时，此处为0）
    var offset = 0 //当前偏移
    var data = ByteArray(24) //从当前偏移开始的24字节（校验用）

    constructor()

    /**
     * 新建一个书签项
     * @param chapter int 章节信息
     * @param offset int 相对于章节的偏移量
     * @param data byte[] 章节数据
     */
    constructor(chapter: Int, offset: Int, data: ByteArray?) {
        this.chapter = chapter
        this.offset = offset
        var i = 0
        while (data != null && i < 24 && i < data.size) {
            this.data[i] = data[i]
            i++
        }
    }

    /**
     * 将字节数组转换为书签项
     * @param data byte[] 字节组数
     * @return Mark 返回书签项对象
     */
    fun getMark(data: ByteArray): Mark {
        val mark = Mark()
        try {
            val bais = ByteArrayInputStream(
                data
            )
            val dis = DataInputStream(bais)
            mark.chapter = dis.readInt()
            mark.offset = dis.readInt()
            dis.read(mark.data)
            dis.close()
            bais.close()
        } catch (ex: IOException) {
        }

        return mark
    }

    /**
     * 以字节数组形式返回当前书签，数组长度应该为32
     * @return byte[]
     */
    fun getByte(): ByteArray? {
        var abyte0: ByteArray?
        try {
            val baos = ByteArrayOutputStream()
            val dos = DataOutputStream(baos)
            dos.writeInt(chapter)
            dos.writeInt(offset)
            dos.write(data)
            abyte0 = baos.toByteArray()
            dos.close()
            baos.close()
        } catch (ex: IOException) {
            abyte0 = null
        }

        return abyte0
    }

}