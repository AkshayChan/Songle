package com.example.akshayc.songle

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Xml
import android.widget.ArrayAdapter
import android.widget.ListView
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


data class Song(val number: String, val artist: String, val title: String, val link: String)

interface DownloadCompleteListener {
    fun downloadComplete(list : List<Song>)
}

class ListOfSongs : AppCompatActivity(), DownloadCompleteListener {

    var slist = mutableListOf<Song>()
    var number = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {

        DownloadXmlTask(this).execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.txt")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_songs)

        val adapter1 = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, number)

        val listView = findViewById<ListView>(R.id.listOf) as ListView
        listView.adapter = adapter1
    }

    override fun downloadComplete (list : List<Song>) {
        Log.d("Hello there", "Yaay works")
        slist = list.toMutableList()
        for (song in slist) {
            number.add( "Song ${song.number}")
        }
    }

}


class DownloadXmlTask(private val caller : DownloadCompleteListener):
        AsyncTask<String, Void, String>() {

    var solist = emptyList<Song>()

    override fun doInBackground(vararg urls: String): String {
        return try {
            loadXmlFromNetwork(urls[0])
        } catch (e: IOException) {
            "Unable to load content. Check your network connection"
        } catch (e: XmlPullParserException) {
            "Error parsing XML"
        }
    }

    private fun loadXmlFromNetwork(urlString: String): String {
        val stream = downloadUrl(urlString)
        solist = parse(stream)
        return stream.toString()
    }

    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        // Also available: HttpsURLConnection
        conn.readTimeout = 10000 // milliseconds
        conn.connectTimeout = 15000 // milliseconds
        conn.requestMethod = "GET"
        conn.doInput = true
        // Starts the query
        conn.connect()
        return conn.inputStream
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        caller.downloadComplete(solist)
    }

    private val ns: String? = null
    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input : InputStream): List<Song> {
        Log.d("Hello", "getting $input here")
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                    false)
            parser.setInput(input, null)
            parser.nextTag()
            return readSongs(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSongs(parser: XmlPullParser): List<Song> {
        val songs = ArrayList<Song>()
        parser.require(XmlPullParser.START_TAG, ns, "Songs")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for a song tag
            if (parser.name == "Song") {
                songs.add(readSong(parser))
            }
        }
        return songs
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSong(parser: XmlPullParser): Song {
        parser.require(XmlPullParser.START_TAG, ns,"Song")
        var number = ""
        var artist = ""
        var title = ""
        var link = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when(parser.name){
                "Number" -> number = readNumber(parser)
                "Artist" -> artist = readArtist(parser)
                "Title" -> title = readTitle(parser)
                "Link" -> link = readLink(parser)
            }
        }
        return Song(number, artist, title, link)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readNumber(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Number")
        val number = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Number")
        return number
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readArtist(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Artist")
        val artist = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Artist")
        return artist
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Title")
        return title
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLink(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Link")
        val link = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Link")
        return link
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }
}





