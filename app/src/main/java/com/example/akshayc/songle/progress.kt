package com.example.akshayc.songle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class progress : AppCompatActivity() {

    companion object {
        var count: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        val tView = findViewById<TextView>(R.id.songText) as TextView
        println(count)
        //tView.setText(count)
    }

}
