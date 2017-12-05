package com.example.akshayc.songle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class review_songs : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_songs)

        var number = arrayOf("Bohemian Rhapsody", "Smells Like Teen Spirit", "Perfect Day", "Hallelujah")
        val adapter1 = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, number)

        val listView = findViewById<ListView>(R.id.songlist) as ListView
        listView.adapter = adapter1
    }
}
