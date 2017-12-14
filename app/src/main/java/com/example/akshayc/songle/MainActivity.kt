package com.example.akshayc.songle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Xml
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity(), DownloadCompleteListener {

    override fun downloadComplete(result: String) {}

    private var receiver = NetworkReceiver()
    var networkPref = "any"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var list: List<SongsData> = DbHelper.Instance(this).getAllSongData()

        // Register BroadcastReceiver to track connection changes.
        val ﬁlter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(receiver, ﬁlter)
    }


    fun startActivity(view: View): Unit {
        val intent = Intent(this, ListOfSongs::class.java)
        startActivity(intent)
    }

    fun startPlaying(view: View): Unit {
        if (checkGpsStatus()) {
            val intent = Intent(this, WhichSong::class.java)
            startActivity(intent)
        }
    }

    fun startProgress(view: View): Unit {
        val intent = Intent(this, progress::class.java)
        startActivity(intent)
    }

    fun startReview(view: View): Unit {
        val intent = Intent(this, review_songs::class.java)
        startActivity(intent)
    }

    fun checkGpsStatus(): Boolean {
        var manager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val simpleAlert = AlertDialog.Builder(this).create()
            simpleAlert.setTitle("GPS Disabled")
            simpleAlert.setMessage("Please enable GPS for proper functioning of the application")

            simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", {_, _ -> })
            simpleAlert.show()
            return false
        } else {
            return true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //this.unregisterReceiver(receiver)
    }

    private fun checkIfDbExists(): Boolean {
        val PACKAGE_NAME = applicationContext.packageName
        var checkDB: SQLiteDatabase? = null
        try {
            Log.d("Database is:", "/data/data/$PACKAGE_NAME/databases/${Const.DATABASE_NAME}")
            checkDB = SQLiteDatabase.openDatabase("/data/data/$PACKAGE_NAME/databases/${Const.DATABASE_NAME}", null, SQLiteDatabase.OPEN_READONLY)
            checkDB.close()
        } catch (e: Exception) {
            e.printStackTrace()
            //no db exists
        }
        return checkDB != null
    }

    private inner class NetworkReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val connManager =
                    context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connManager.activeNetworkInfo
            if (networkPref == "wifi" && networkInfo?.type == ConnectivityManager.TYPE_WIFI) {
                //wifi connected
                if (!checkIfDbExists()) {
                    DownloadXmlTask(applicationContext, this@MainActivity).execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
                } else {
                    CheckForTimestampTask(applicationContext, this@MainActivity).execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
                }
            } else if (networkPref == "any" && networkInfo != null) {
                // Have a network connection and permission, so use data
                if (!checkIfDbExists()) {
                    DownloadXmlTask(applicationContext, this@MainActivity).execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
                } else {
                    CheckForTimestampTask(applicationContext, this@MainActivity).execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
                }
            } else {
                // No Wi´Fi and no permission, or no network connection
                Toast.makeText(context, "No Internet connectivity found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

interface DownloadCompleteListener {
    fun downloadComplete(result: String)
}

class CheckForTimestampTask(val context: Context?, private val caller: DownloadCompleteListener) : AsyncTask<String, Void, Array<String>>() {

    val ctx = context
    val interfaceCaller = caller

    val PREFS_FILENAME = "com.example.akshayc.songle.prefs"
    val prefs = context?.getSharedPreferences(PREFS_FILENAME, 0)
    var timeStamp = ""

    override fun onPreExecute() {
        super.onPreExecute()
        if (prefs != null) {
            timeStamp = prefs.getString("TIMESTAMP", "")
        }
    }

    override fun doInBackground(vararg p0: String): Array<String> {
        var resultData = loadXmlFromNetwork(p0[0])
        return resultData
    }

    private fun loadXmlFromNetwork(urlString: String): Array<String> {
        val stream = downloadUrl(urlString)
        var currentTimeStamp = parseXmlAndReturnTimeStamp(stream)
        Log.d("", "loadXmlFromNetwork: " + currentTimeStamp)
        //parse and get timestamp
        if (currentTimeStamp.equals(timeStamp, true)) {
            //same time stamp, no need to parse and update song db
            Log.d("Check Time Stamp", "currentTimeStamp equal: " + currentTimeStamp)
            val resultArray = arrayOf(currentTimeStamp, "Same")
            return resultArray
        } else {
            //different timestamp, parse xml and update db with song data
            Log.d("Check Time Stamp", "currentTimeStamp different: " + currentTimeStamp)
            val resultArray = arrayOf(currentTimeStamp, "Different")
            return resultArray
        }
    }

    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.readTimeout = 10000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"
        conn.doInput = true
        //Starts the query
        conn.connect()
        return conn.inputStream
    }

    private val ns: String? = null
    @Throws(XmlPullParserException::class, IOException::class)
    fun parseXmlAndReturnTimeStamp(input: InputStream): String {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(input, null)
            parser.nextTag()
            return parser.getAttributeValue(0)
        }
    }

    override fun onPostExecute(result: Array<String>) {
        super.onPostExecute(result)
        if (result[1].equals("Different", true)) {
            val editor = prefs!!.edit()
            editor.putString("TIMESTAMP", result[0])
            editor.apply()
            Log.d("Timestamp Post Execute", "PostExec: result is different calling db task ")
            DownloadXmlTask(ctx, interfaceCaller).execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
        } else {
            //result is same
            val editor = prefs!!.edit()
            editor.putString("TIMESTAMP", result[0])
            editor.apply()
            Log.d("Timestamp Post Execute", "PostExec: result is same ")
        }
    }

}


class DownloadXmlTask(val context: Context?, private val caller: DownloadCompleteListener) : AsyncTask<String, Void, String>() {


    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg p0: String): String {
        return try {
            loadXmlFromNetwork(p0[0], context)
        } catch (e: IOException) {
            "Unable to load content. Check your network connection"
        } catch (e: XmlPullParserException) {
            "Error parsing XML"
        }
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        Log.d("Xml Post Execute", "result from Download XML: " + result)
        caller.downloadComplete(result)
    }

    private fun loadXmlFromNetwork(urlString: String, context: Context?): String {
        val stream = downloadUrl(urlString)
        val result = StringBuilder()
        var songDataList = parse(stream)
        DbHelper.Instance(context!!).insertMultipleSongValues(songDataList)
        return result.toString()
    }

    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.readTimeout = 10000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"
        conn.doInput = true
        //Starts the query
        conn.connect()
        return conn.inputStream
    }

    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input: InputStream): List<SongsData> {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(input, null)
            parser.nextTag()
            return readSongData(parser)
        }

    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readSongData(parser: XmlPullParser): List<SongsData> {
        val songs = ArrayList<SongsData>()
        parser.require(XmlPullParser.START_TAG, ns, "Songs")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            //start by looking for the Song tag
            if (parser.name == "Song") {
                songs.add(readSong(parser))
            } else {
                skip(parser)
            }
        }
        return songs
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSong(parser: XmlPullParser): SongsData {
        parser.require(XmlPullParser.START_TAG, ns, "Song")
        var number = ""
        var title = ""
        var artist = ""
        var link = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "Number" -> number = readNumber(parser)
                "Title" -> title = readTitle(parser)
                "Artist" -> artist = readArtist(parser)
                "Link" -> link = readLink(parser)
                else -> skip(parser)
            }
        }
        return SongsData(number, title, artist, link, "false")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readNumber(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Number")
        val number = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Number")
        return number
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Title")
        return title
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readArtist(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Artist")
        val artist = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Artist")
        return artist
    }

    @Throws(XmlPullParserException::class, IOException::class)
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

    @Throws(IOException::class, XmlPullParserException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}


