package com.lake.txtreader

import android.content.SharedPreferences

class Record {
    private var name = ""
    private lateinit var rs: SharedPreferences
    private var open = false

    constructor(name: String) {
        this.name = name
    }

    fun open(): Boolean {
        open = true
        try {
            rs = SharedPreferences()
        } catch (ex: RecordStoreException) {
            open = false
        }

        return open
    }

    fun getNumRecords(): Int {
        var num = -1
        if (open) {
            try {
                num = rs!!.getNumRecords()
            } catch (ex: RecordStoreNotOpenException) {
                num = -1
            }

        }
        return num
    }

    fun getNextRecordID(): Int {
        var num = -1
        try {
            num = rs!!.getNextRecordID()
        } catch (ex: RecordStoreNotOpenException) {
            num = -1
        } catch (ex: RecordStoreException) {
            num = -1
        }

        return num
    }

    fun addRecord(b: ByteArray?): Boolean {
        var flag = true
        if (b == null) {
            return false
        }

        try {
            rs!!.addRecord(b, 0, b.size)
        } catch (ex: RecordStoreNotOpenException) {
            flag = false
        } catch (ex: RecordStoreException) {
            flag = false
        }

        return flag
    }

    fun setRecord(i: Int, b: ByteArray?): Boolean {
        var flag = true
        if (b == null) {
            return false
        }

        try {
            rs!!.setRecord(i, b, 0, b.size)
        } catch (ex: RecordStoreNotOpenException) {
            flag = false
        } catch (ex: RecordStoreException) {
            flag = false
        }

        return flag
    }

    fun getRecord(i: Int): ByteArray? {
        var b: ByteArray? = null
        try {
            b = rs!!.getRecord(i)
        } catch (ex: InvalidRecordIDException) {
            b = null
        } catch (ex: RecordStoreNotOpenException) {
            b = null
        } catch (ex: RecordStoreException) {
            b = null
        }

        return b
    }

    fun close() {
        try {
            rs!!.closeRecordStore()
        } catch (ex: RecordStoreNotOpenException) {
        } catch (ex: RecordStoreException) {
        }

    }

    fun deleteRecordStore(name: String) {
        try {
            RecordStore.deleteRecordStore(name)
        } catch (ex: RecordStoreException) {
        }

    }
}