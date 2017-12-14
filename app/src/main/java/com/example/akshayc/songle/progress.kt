package com.example.akshayc.songle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class progress : AppCompatActivity() {
    var wordsCollected: TextView? = null
    var songsGuessed: TextView? = null
    var averageScore: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        wordsCollected = findViewById(R.id.wordText)
        songsGuessed = findViewById(R.id.songText)
        averageScore = findViewById(R.id.scoreText)

        var list = DbHelper.Instance(this).getAllHintsData()
        wordsCollected?.text = "" + list.size

        var listOfSongsGuessed = DbHelper.Instance(this).getAllSongsDataAsPerSongGuessed("true")
        songsGuessed?.text = "" + listOfSongsGuessed.size

        var listOfSongsShown = DbHelper.Instance(this).getAllSongsDataAsPerSongGuessed("shown")

        averageScore?.text = "" + getAverageScore(listOfSongsGuessed.size.toDouble(), listOfSongsShown.size.toDouble(), list.size.toDouble())
    }

    fun getAverageScore(correctGuessCount: Double, songShown: Double, wordsCollected: Double): Double {
        //correctly guessed*7 – songs shown*3)/net words collected
        var correctGuessMultiplied = correctGuessCount * 7
        var songShownMultiplied = songShown * 3
        var difference = correctGuessMultiplied - songShownMultiplied
        var averageScore: Double = (difference / wordsCollected)
        return round(averageScore, 3)
    }

    fun round(value: Double, places: Int): Double {
        var value = value
        if (places < 0) throw IllegalArgumentException()

        val factor = Math.pow(10.0, places.toDouble()).toLong()
        value = value * factor
        val tmp = Math.round(value)

        if ((tmp.toDouble() / factor) <= 0) {
            return 0.0
        } else {
            return tmp.toDouble() / factor
        }
    }
}
