package com.lake.txtreader

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import java.util.*

class TxtReaderView : View {

    var srcW = 0
    var srcH = 0
    var fileName = ""//文件名
    lateinit var fsa: TextFileReader//文件读工具
    lateinit var bg: Bitmap

    var txtLengt = 0//当前文件文件的长度
    var lineOfPage = 0//当前视图情况下可以显示的行数
    var viewWidth: Int = 0//可视区域的高与宽
    var viewHeight: Int = 0 //可视区域的高与宽
    var bufferImage: Bitmap? = null //缓冲区图像
    var currentLine = 0 //当前起始行
    private val currentOffset = 0 //当前视图内首行的偏移
    //private int bufferStartOffset = 0; //当前缓冲区的开始在文件中的位置
    var mylines = Vector<TxtLine>() //保存每行的起始偏移
    private val BufferLenght = 65536 //64K的缓冲长度
    private val bufferTxt: ByteArray? = null //文件缓冲
    private val bufferbuffer = ByteArray(0) //保存缓冲区尾部数据的缓冲
    private val eof = false //是否是文档的最后一页
    private val bof = true //是否是文档的最前一页

    constructor(ctx: Context, filename: String) : this(ctx) {
        fileName = filename
    }

    constructor(ctx: Context) : this(ctx, null)
    constructor(ctx: Context, attrs: AttributeSet?) : this(ctx, attrs, 0)
    constructor(ctx: Context, attrs: AttributeSet?, def: Int) : super(ctx, attrs, def) {
        initView()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
    }

    fun initView() {
        fsa = TextFileReader(fileName)
        txtLengt = fsa.fileSize().toInt()

    }


}