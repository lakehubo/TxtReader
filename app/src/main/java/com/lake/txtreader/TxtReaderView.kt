package com.lake.txtreader

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*

class TxtReaderView : View {

    var fileName = ""//文件名
    lateinit var fsa: TextFileReader//文件读工具
    lateinit var bg: Bitmap//背景图

    val GBWidth = 16
    val AsciiWidth = 8

    var txtLengt = 0//当前文件文件的长度
    var lineOfPage = 0//当前视图情况下可以显示的行数
    var viewWidth: Int = 0//可视区域的高与宽
    var viewHeight: Int = 0 //可视区域的高与宽
    lateinit var bufferImage: Bitmap //缓冲区图像
    var currentLine = 0 //当前起始行
    private var currentOffset = 0 //当前视图内首行的偏移
    //private int bufferStartOffset = 0; //当前缓冲区的开始在文件中的位置
    var mylines = Vector<TxtLine>() //保存每行的起始偏移
    private val BufferLenght = 65536 //64K的缓冲长度
    private var bufferTxt = ByteArray(0)//文件缓冲
    private var bufferbuffer = ByteArray(0) //保存缓冲区尾部数据的缓冲
    private var eof = false //是否是文档的最后一页
    private var bof = true //是否是文档的最前一页

    constructor(ctx: Context, filename: String) : this(ctx) {
        fileName = filename
        initView()
    }

    constructor(ctx: Context) : this(ctx, null)
    constructor(ctx: Context, attrs: AttributeSet?) : this(ctx, attrs, 0)
    constructor(ctx: Context, attrs: AttributeSet?, def: Int) : super(ctx, attrs, def)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
    }

    fun initView() {
        fsa = TextFileReader(fileName)
        txtLengt = fsa.fileSize().toInt()
        bg = BitmapFactory.decodeResource(resources, R.mipmap.txtviewbg)
        fsa.skip(currentOffset)

        readNextBuffer()
        analysisBuffer()
        prepareNextImage()
//        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)

        var line = 0
        var yOff = 0
        var temp = currentLine
        val start = (mylines.elementAt(0) as TxtLine).offset

        while (temp < mylines.size && line < lineOfPage) {
            val tl = mylines.elementAt(temp) as TxtLine
            var len = 0
            if (temp != mylines.size - 1) {
                len = (mylines.elementAt(temp + 1) as TxtLine).offset - tl.offset
            } else {
                len = start + bufferTxt.size - tl.offset
            }
            val b = ByteArray(len)
            System.arraycopy(bufferTxt, tl.offset - start, b, 0, len)

            val img = buildImage(b)
            var rectF = RectF(0f, 0f, viewWidth.toFloat(), yOff.toFloat())
            canvas.drawBitmap(img, null, rectF, Paint())
            yOff += FontHeight + lineSpace
            line++
            temp++
        }
    }

    private fun drawBackground(canvas: Canvas) {
        val rectF = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        canvas.drawBitmap(bg, null, rectF, Paint())
    }

    fun buildImage(b: ByteArray): Bitmap? {
        if (b == null) {
            return null
        }
        val width = AsciiWidth * b.size
        val image = ByteArray(FontHeight * width)
        var x = 0
        var i = 0
        while (i < b.size - 1) {
            if (b[i].toInt() and 0xff > 0x7F) { //汉字
                var skip = 0
                skip = drawGBK(b[i], b[i + 1], image, x, width)
                if (skip == 1) {
                    x += AsciiWidth
                } else {
                    x += GBWidth
                }
                i += skip
            } else { //英文
                drawAscii(b[i], image, x, width)
                x += AsciiWidth
                i++
            }
        }
        if (i == b.size - 1) { //最后一个是英文
            drawAscii(b[b.size - 1], image, x, width)
        }
        var bitmap = BitmapFactory.decodeByteArray(image, width, FontHeight)
        return bitmap;
    }

    var GBPerByte = 32
    var AsciiOffset = 4096
    private lateinit var charset: ByteArray
    val verify = intArrayOf(128, 64, 32, 16, 8, 4, 2, 1)
    private val colors = IntArray(24)
    val ALPHA = -0x1000000

    private fun drawGBK(bt1: Byte, bt2: Byte, image: ByteArray, x: Int, width: Int): Int {
        val q1 = bt1.toInt() and 0xff
        var q2 = bt2.toInt() and 0xff
        var offset = (q1 - 0x81) * 190 * GBPerByte
        if (q2 >= 0x80) {
            q2 -= 0x41
        } else {
            q2 -= 0x40
        }
        offset += q2 * GBPerByte

        offset += AsciiOffset
        if (offset > charset.size - 1) { //超过范围，处理下一个
            return 1
        }
        var pos = x
        if (this.GBWidth > 16) {
            for (h in 0 until FontHeight) {
                var b = charset[offset++]
                for (w in 0..7) {
                    if (b.toInt() and verify[w] === verify[w]) {
                        image[pos + w] = (ALPHA or colors[h]).toByte()
                    } else {
                        image[pos + w] = 0
                    }
                }
                b = charset[offset++]
                for (w in 0..7) {
                    if (b.toInt() and verify[w] === verify[w]) {
                        image[pos + 8 + w] = (ALPHA or colors[h]).toByte()
                    } else {
                        image[pos + 8 + w] = 0
                    }
                }
                b = charset[offset++]
                for (w in 0 until GBWidth - 16) {
                    if (b.toInt() and verify[w] === verify[w]) {
                        image[pos + 16 + w] = (ALPHA or colors[h]).toByte()
                    } else {
                        image[pos + 16 + w] = 0
                    }
                }

                pos += width
            }
        } else {
            for (h in 0 until FontHeight) {
                var b = charset[offset++]
                for (w in 0..7) {
                    if (b.toInt() and verify[w] === verify[w]) {
                        image[pos + w] = (ALPHA or colors[h]).toByte()
                    } else {
                        image[pos + w] = 0
                    }
                }
                b = charset[offset++]
                for (w in 0 until GBWidth - 8) {
                    if (b.toInt() and verify[w] === verify[w]) {
                        image[pos + 8 + w] = (ALPHA or colors[h]).toByte()
                    } else {
                        image[pos + 8 + w] = 0
                    }
                }
                pos += width
            }
        }
        return 2
    }

    var AsciiPerByte = 16

    //ascii显示代码
    private fun drawAscii(bt: Byte, image: ByteArray, x: Int, width: Int) {
        val index = bt.toInt() and 0xff
        if (index < 33 || index > 126) {
            return
        }
        var offset = index * AsciiPerByte
        var pos = x
        if (this.AsciiWidth > 8) { //一字节显示不下
            for (h in 0 until FontHeight) {
                var b = charset[offset++]
                for (w in 0..7) {
                    if (b.toInt() and verify[w] === verify[w]) {
                        image[pos + w] = (ALPHA or colors[h]).toByte()
                    } else {
                        image[pos + w] = 0
                    }
                }

                b = charset[offset++]
                for (w in 0 until AsciiWidth - 8) {
                    if (b.toInt() and verify[w] === verify[w]) {
                        image[pos + 8 + w] = (ALPHA or colors[h]).toByte()
                    } else {
                        image[pos + 8 + w] = 0
                    }
                }
                pos += width
            }
        } else {
            for (h in 0 until FontHeight) {
                val b = charset[offset++]
                for (w in 0 until AsciiWidth) {
                    if (b.toInt() and verify[w] === verify[w]) {
                        image[pos + w] = (ALPHA or colors[h]).toByte()
                    } else {
                        image[pos + w] = 0
                    }
                }
                pos += width
            }
        }
    }

    /**
     * 读取下一页的缓存
     */
    fun readNextBuffer() {
        var start = 0
        if (null != bufferbuffer) { //查看缓冲区尾部数据的缓冲中是否有数据
            start = bufferbuffer.size
        }
        var b = fsa.read(BufferLenght)
        if (b!!.size != BufferLenght) { //已读到了文章的尾部
            eof = true
            bufferTxt = ByteArray(b.size + start)
            //将缓冲区尾部数据的缓冲中的数据COPY至缓冲区中
            System.arraycopy(bufferbuffer, 0, bufferTxt, 0, start)
            System.arraycopy(b, 0, bufferTxt, start, b.size)
            bufferbuffer = ByteArray(0) //缓冲的缓冲为空
            b = null
            System.gc()
            return
        }

        //删除缓冲区中最后一个回车以后的字符
        eof = false
        var end = b.size
        if (end > 0) {
            while (end > 1) {
                val v = b[end - 1].toInt() and 0xff
                if (v == 10) { //\n 换行
                    break
                }
                end--
            }
        }

        if (0 == end) { //遇上了严重的错误，居然16K数据中没有一个回车符，不合情理
            System.exit(-1)
        }

        bufferTxt = ByteArray(end + start)
        val rest = BufferLenght - end
        System.arraycopy(bufferbuffer, 0, bufferTxt, 0, start)
        System.arraycopy(b, 0, bufferTxt, start, end)
        bufferbuffer = ByteArray(rest)
        System.arraycopy(b, end, bufferbuffer, 0, rest)
        b = null
        System.gc()
    }

    @Synchronized
    fun analysisBuffer() {

        if (null == bufferTxt) {
            return
        }
        mylines.removeAllElements()
        var c = 0
        var w = 0
        var l = 0
        //Vector test = new Vector();
        //mylines.addElement(new Integer(currentOffset));
        while (c < bufferTxt.size - 1) {
            val b = bufferTxt[c].toInt() and 0xff
            if (b == 10) { //\r 换行
                c++
                l++
                //mylines.addElement(new Integer(c + currentOffset));
                mylines.addElement(TxtLine(currentOffset + c - l, l))
                w = 0
                l = 0
                continue
            }
            //字符处理
            if (b > 0x7f) { //汉字
                if (w + GBWidth > viewWidth) { //当前行无法显示完整
                    //mylines.addElement(new Integer(c + currentOffset));
                    mylines.addElement(TxtLine(currentOffset + c - l, l))
                    w = 0
                    l = 0
                    continue
                } else {
                    c += 2
                    l += 2
                    w += GBWidth
                }
            } else { //英文
                if (w + AsciiWidth > viewWidth) { //当前行无法显示完整
                    //mylines.addElement(new Integer(c + currentOffset));
                    mylines.addElement(TxtLine(currentOffset + c - l, l))
                    w = 0
                    l = 0
                    continue
                } else {
                    c++
                    l++
                    w += AsciiWidth
                }
            }
        }
        mylines.addElement(TxtLine(currentOffset + c - l, l))
        currentLine = 0
        //定位到正确的行
        if (currentOffset != 0) {
            var i = 0
            while (i < mylines.size) {
                if ((mylines.elementAt(i) as TxtLine).offset >= currentOffset) {
                    break
                }
                i++
            }
            i--
            currentLine = if (i < 0) 0 else i
        }
    }

    var FontHeight = 16
    var lineSpace = 0
    /**
     * 准备下一页
     */
    fun prepareNextImage() {
        if (currentLine == mylines.size) {
            currentOffset = (mylines.elementAt(0) as TxtLine).offset + bufferTxt.size
        } else {
            currentOffset = (mylines.elementAt(currentLine) as TxtLine).offset
        }
        bof = (mylines.elementAt(0) as TxtLine).offset == 0
        //判断当前缓冲区的内容是否够显示
        if (currentLine + lineOfPage >= mylines.size && !eof) {
            val offset = currentOffset - (mylines.elementAt(0) as TxtLine).offset
            val rest = bufferTxt.size - offset
            var b: ByteArray? = ByteArray(rest + bufferbuffer.size)
            System.arraycopy(bufferTxt, offset, b!!, 0, rest)
            System.arraycopy(bufferbuffer, 0, b, rest, bufferbuffer.size)
            bufferbuffer = ByteArray(b.size)
            System.arraycopy(b, 0, bufferbuffer, 0, b.size)
            b = null
            System.gc()
            readNextBuffer()
            analysisBuffer()
            bof = false
        }
    }
}