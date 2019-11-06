package com.lake.txtreader

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text_view.text = "测试测试测试"
        text_view.setTextColor(Color.BLUE)

        setName("测试2测试2测试2")
    }

    private fun setName(s: String) {
        text_view2.text = s
    }

    override fun onResume() {
        super.onResume()
    }
}
