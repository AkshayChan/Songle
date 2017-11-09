package com.example.akshayc.songle

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_which_song.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class WhichSong : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_which_song)
    }

    fun sendMessage(view: View): Unit {
        val intent = Intent(this, MapsActivity::class.java)
        val message1 = editText.text.toString()
        val message2 = editText2.text.toString() // editText defined in content main.xml
        intent.putExtra("song", message1)
        intent.putExtra("map", message2)
        startActivity(intent)
    }

}