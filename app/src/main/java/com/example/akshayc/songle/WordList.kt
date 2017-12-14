package com.example.akshayc.songle

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.support.v7.app.AlertDialog
import android.widget.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL


class WordList : AppCompatActivity() {

    var songNo = ""
    var listView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        songNo = intent.getStringExtra("SONG_NO")

        //Getting the list of words collected for the song from the hint database using the song no received
        var hintList = DbHelper.Instance(this@WordList).getAllHintsDataAsPerSongNo(songNo)
        var wordHintList: ArrayList<String> = arrayListOf()
        for (hint in hintList) {
            wordHintList.add(hint.wordHint)
        }

        val adapter1 = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, wordHintList)


        listView = findViewById(R.id.wordlist)
        listView?.adapter = adapter1


    }

    fun onGuess(view: View) {

        var songData = DbHelper.Instance(this).getSingleSongData(songNo)

        val edittext = findViewById<EditText>(R.id.editText3) as EditText

        if (edittext.text.toString().equals(songData.Title, true)) {
            Toast.makeText(this, "Hurray, Correct guess", Toast.LENGTH_SHORT).show()
            DbHelper.Instance(this).updateSongGuessedStatusAsPerSongNo(songNo, "true")

            var intent: Intent
            intent = Intent(this@WordList, CorrectSong::class.java)
            intent.putExtra("SONG_NO", songNo)
            startActivity(intent)
        } else {
            val text = "Incorrect Song!"
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(this, text, duration)
            toast.show()
        }
    }

    fun onHint(view: View) {
        DownloadLyricsFileAndReturnHint(this, songNo).execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/$songNo/lyrics.txt")
    }

    fun onShowSong(view: View) {
        DbHelper.Instance(this).updateSongGuessedStatusAsPerSongNo(songNo, "shown")
        var intent: Intent
        intent = Intent(this, CorrectSong::class.java)
        intent.putExtra("SONG_NO", songNo)
        startActivity(intent)
    }
}

class DownloadLyricsFileAndReturnHint(val context: Context,
                                              val songNo: String) : AsyncTask<String, Void, String>() {
    lateinit var pd: ProgressDialog
    var lineToUse = ""
    var cont = context
    var artistName = ""

    override fun onPreExecute() {
        super.onPreExecute()
        pd = ProgressDialog(context)
        pd.setCancelable(false)
        pd.setMessage("Please wait")
        pd.show()

    }

    override fun doInBackground(vararg p0: String?): String {
        try {
            artistName = DbHelper.Instance(context).getArtistNameAsPerSongNo(songNo)
            val url = URL(p0[0])
            val input = BufferedReader(InputStreamReader(url.openStream()))
            //Just the first line
            lineToUse = input.readLine()

            } catch (e: Exception) {
                Log.e("Error: ", e.message)
            }

            return lineToUse
        }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        pd.dismiss()
        val simpleAlert = AlertDialog.Builder(context).create()
        simpleAlert.setTitle("Hint")
        simpleAlert.setMessage("First Line of Song is: $lineToUse" + System.getProperty("line.separator") + "Artist name is: $artistName")
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", { dialogInterface, i ->
        })
        simpleAlert.show()
        }
    }
