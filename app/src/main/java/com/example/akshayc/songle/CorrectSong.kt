package com.example.akshayc.songle

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.VideoView


class CorrectSong : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correct_song)

    }

    fun onClickLink(view: View) :Unit {
        val uri = Uri.parse("https://youtu.be/fJ9rUzIMcZQ")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
