package com.example.akshayc.songle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_word_list.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_word_list.*
import android.support.design.widget.Snackbar
import kotlinx.android.synthetic.main.activity_progress.*


class WordList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        var number = arrayOf("35:1 Scaramouche,", "22:1 Carry", "19:2 ooh,", "40:1 Galileo", "11:1 Any", "53:1 Never")
        val adapter1 = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, number)

        val listView = findViewById<ListView>(R.id.wordlist) as ListView
        listView.adapter = adapter1

    }

    fun onGuess (view:View) {
        val textView = findViewById<TextView>(R.id.editText3) as TextView
        Log.d("Hello", textView.text.toString())
        if (textView.text.toString().equals("Bohemian Rhapsody")) {
            val intent = Intent(this, CorrectSong::class.java)
            startActivity(intent)
        }
        else {
            val text = "Incorrect Song!"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(this, text, duration)
            toast.show()
        }
    }

    fun onHint (view:View) {
        val snackbar1 = Snackbar
                .make(findViewById<View>(android.R.id.content), "The first line is: \"Is this the real life?\"", Snackbar.LENGTH_LONG)
        snackbar1.show()

        val text = "The artist is Queen"
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(this, text, duration)
        toast.show()

    }

    fun onShowSong (view:View) {
        val snackbar2 = Snackbar
                .make(findViewById<View>(android.R.id.content), "The song is Bohemian Rhapsody by Queen", Snackbar.LENGTH_LONG)
        snackbar2.show()
    }
}