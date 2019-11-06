package com.lake.txtreader

////////////////////////////////////////
//
//  保存每一行的信息
//  offest： 此行在文件中的偏移位置
//  lenght： 此长的长度
//
////////////////////////////////////////

class TxtLine(var offset: Int = 0, var lenght: Int = 0)