package com.example.akshayc.songle

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import kotlinx.android.synthetic.main.activity_correct_song.*


class CorrectSong : AppCompatActivity() {

    var songNo = ""
    var songName: TextView? = null
    var artistName: TextView? = null
    var songUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correct_song)
        songNo = intent.getStringExtra("SONG_NO")

        var songData = DbHelper.Instance(this).getSingleSongData(songNo)
        songUrl = songData.Link

        artistName = findViewById(R.id.artistName)
        artistName?.text = songData.Artist

        songName = findViewById(R.id.songName)
        songName?.text = songData.Title
    }

    fun onClickLink(view: View) :Unit {
        val uri = Uri.parse(songUrl)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
