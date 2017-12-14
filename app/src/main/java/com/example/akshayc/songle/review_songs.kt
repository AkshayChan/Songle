package com.example.akshayc.songle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class review_songs : AppCompatActivity() {

    var listView: ListView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_songs)

        listView = findViewById(R.id.songlist)

        var strlist: ArrayList<String> = arrayListOf()

        var list = DbHelper.Instance(this).getAllSongsDataAsPerSongGuessed("true")
        for (l in list) {
            strlist.add(l.Title)
        }

        var adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, strlist)

        listView?.adapter = adapter

    }
}
