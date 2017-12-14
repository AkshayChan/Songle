package com.example.akshayc.songle

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.kml.KmlLayer
import com.google.maps.android.kml.KmlPlacemark
import com.google.maps.android.kml.KmlPoint
import java.io.*
import java.net.URL


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, DownloadListener, GoogleMap.OnMarkerClickListener {

    var myLat: Double? = 0.0
    var myLong: Double? = 0.0
    var songNo = ""
    var mapNo = ""
    var lyricsUrl = ""

    val tag = "MapsActivity"
    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient

    val permissionsRequestAccessFineLocation = 1
    var mLocationPermissionGranted = false
    // getLastLocation can return null, so we need the type ”Location?”
    private var mLastLocation: Location? = null
    var kmllayer:KmlLayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        val intent = intent
        songNo = intent.getStringExtra("song")
        mapNo = intent.getStringExtra("map")
        lyricsUrl = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/$songNo/lyrics.txt"


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Create an instance of GoogleAPIClient.
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    var stream: InputStream? = null

    override fun downloadComp(str : String) {
        Log.d("got string", str)
        var kstream = str.byteInputStream()
        stream = kstream

        if (stream != null) {
            Log.d(tag, "downloaded stream is " + stream.toString())
            kmllayer = KmlLayer(mMap, stream, this)
            kmllayer!!.addLayerToMap()
            moveCameraToKml(kmllayer!!)
        } else {
            Toast.makeText(this@MapsActivity, "null stream", Toast.LENGTH_SHORT).show()
        }
    }

    fun getContext():Context{
        return this
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            val URL = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${songNo}/map${mapNo}.kml"
            val downloader = DownloadKmlTask(this, this, songNo, mapNo)
            downloader.execute(URL)
            // Visualise current position with a small blue circle
            mMap.isMyLocationEnabled = true
        } catch (se: SecurityException) {
            throw(se)
        }
        // Add ”My location” button to the user interface
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setOnMarkerClickListener(this)
    }

    private fun moveCameraToKml(kmlLayer: KmlLayer) {
        try {
            var container = kmlLayer.containers.iterator().next()
            var placeMark: KmlPlacemark = container.placemarks.iterator().next()
            var builder: LatLngBounds.Builder = LatLngBounds.builder()
            var point: KmlPoint = placeMark.geometry as KmlPoint
            var latlng = LatLng(point.geometryObject.latitude, point.geometryObject.longitude)
            builder.include(latlng)
            var width = resources.displayMetrics.widthPixels
            var height = resources.displayMetrics.heightPixels
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, 0))
            mMap.setMaxZoomPreference(20f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //returns distance in metres using the Haversine algorithm

    private fun distance(lat1: Double, lon1: Double, lat2: Double?, lon2: Double?): Double {
        val theta = lon1 - lon2!!
        var dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2!!)) + (Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta)))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist *= 60.0 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    fun round(value: Double, places: Int): Double {
        var value = value
        if (places < 0) throw IllegalArgumentException()

        val factor = Math.pow(10.0, places.toDouble()).toLong()
        value = value * factor
        val tmp = Math.round(value)
        return tmp.toDouble() / factor
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        var latitude = marker.position.latitude
        var longitude = marker.position.longitude

        var listOfLineAndWordNo: List<String> = marker.title.split(":")
        var lineNumberHint = listOfLineAndWordNo[0]
        var wordNumberHint = listOfLineAndWordNo[1]

        var distance = distance(latitude, longitude, myLat, myLong)
        var distanceInMeters = distance * 1000

        var formatedDistanceInMeters = round(distanceInMeters, 3)


        //if distance is less than 10 meters, collect hint, else show distance alert
        if (formatedDistanceInMeters <= 10) {

            //DownloadLyricsFileAndReturnWordTask(this@MapsActivity, songNo, lineNumberHint.toInt(), wordNumberHint.toInt())
            val snackbar = Snackbar
                    .make(findViewById<View>(android.R.id.content), "Word Collected : ${marker.title}" + " lat: " + latitude + " long: " + longitude, Snackbar.LENGTH_INDEFINITE)
            snackbar.show()
            marker.remove()
            DownloadLyricsFileAndReturnWordTask(this@MapsActivity, songNo, lineNumberHint.toInt(), wordNumberHint.toInt()).execute(lyricsUrl)
        } else {
            val simpleAlert = AlertDialog.Builder(this@MapsActivity).create()
            simpleAlert.setTitle("Distance")
            simpleAlert.setMessage("You are: $formatedDistanceInMeters meters away from this placemark, it can not be collected if you are more than 10 meters away")
            simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", { dialogInterface, i ->
            })
            simpleAlert.show()
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()

    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun createLocationRequest() {
        // Set the parameters for the location request
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 5000 // preferably every 5 seconds
        mLocationRequest.fastestInterval = 1000 // at most every second
        mLocationRequest.priority =
                LocationRequest.PRIORITY_HIGH_ACCURACY
        // Can we access the user’s current location?
        val permissionCheck = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onConnected(connectionHint: Bundle?) {
        try {
            createLocationRequest()
        } catch (ise: IllegalStateException) {
            println("[$tag] [onConnected] IllegalStateException thrown")
        }
        // Can we access the user’s current location?
        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            val api = LocationServices.FusedLocationApi
            mLastLocation = api.getLastLocation(mGoogleApiClient)
            // Caution: getLastLocation can return null
            if (mLastLocation == null) {
                println("[$tag] Warning: mLastLocation is null")
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    permissionsRequestAccessFineLocation)

        }
    }

    override fun onConnectionSuspended(p0: Int) {
        println("Connection has been terminated")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        println("Connection failed, please check your data")
    }

    override fun onLocationChanged(p0: Location?) {
        println("Location has been changed")
        Log.d("Location Latitude", "" + p0?.latitude)
        Log.d("Location Longitude", "" + p0?.longitude)
        myLat = p0?.latitude
        myLong = p0?.longitude
    }
}

interface DownloadListener {
    fun downloadComp(result: String)
}

class DownloadLyricsFileAndReturnWordTask(val context: Context, val song: String, val lineNo: Int,
                                          val wordNumber: Int) : AsyncTask<String, Void, String>() {

    var lineToUse = ""
    //var cont = context
    //var sng = song
    // var mp = map
    //var filename = "Song ${sng}_lyrics"
    //var file = File(cont.getFilesDir(), filename)


    override fun doInBackground(vararg p0: String?): String {
        try {
            Log.d("URL obtained is", p0[0])
            val url = URL(p0[0])
            val input = BufferedReader(InputStreamReader(url.openStream()))

            //-1 because we need to use the line we are given through the number given
            for (i in 1..lineNo - 1) {
                input.readLine()
            }
            lineToUse = input.readLine()

            var wordList: List<String> = lineToUse.split(" ")
            Log.d("Wordlist", wordList.toString())
            Log.d("Hello", "${wordNumber-1}")
            var wordHint = wordList[wordNumber - 1]

            var hintsData = HintsData()
            hintsData.wordHint = wordHint
            hintsData.songNumber = song
            hintsData.wordNumber = "" + wordNumber
            hintsData.lineNumber = "" + lineNo

            var inst = DbHelper.Instance(context)
            inst.insertIntoHintsValue(hintsData)
            //DbHelper.Instance(context).insertIntoHintsValue(hintsData)

            } catch (e: Exception) {
                Log.e("Error: ", e.message)
        }

        /*val outputStream: FileOutputStream
        try {
            outputStream = cont.openFileOutput(filename, Context.MODE_PRIVATE)
            outputStream.write(fileString.toString().toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }*/

        return lineToUse
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        var wordList: List<String> = result!!.split(" ")
        //n-1 because indexing starts from 0 not 1
        Log.d("WORDTAG", wordList[wordNumber - 1])
    }
}

class DownloadKmlTask( val caller: DownloadListener,  val context: Context,
                       val song :String, val map:String) : AsyncTask<String, Void, String>() {
    val tag = "DownloadKmlTask"
    var cont = context
    var sng = song
    var mp = map
    var filename = "Song ${sng}, map ${mp}"
    var file = File(cont.filesDir, filename)

    override fun doInBackground(vararg f_url: String): String? {
        val fileString = StringBuilder()
        try
        {
            Log.d("URL obtained is", f_url[0])
            val url = URL(f_url[0])
            val input = BufferedReader(InputStreamReader(url.openStream()))
            // Output stream
            var line: String? = input.readLine()
            while (line != null){
                fileString.append(line)
                line = input.readLine()
            }
        }
        catch (e:Exception) {
            Log.e("Error: ", e.message)
        }

        /*val outputStream: FileOutputStream
        try {
            outputStream = cont.openFileOutput(filename, Context.MODE_PRIVATE)
            outputStream.write(fileString.toString().toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }*/

        return fileString.toString()
    }

    override fun onPostExecute(result: String?) {
        if (result != null){
            Log.d(tag,"[onPostExecute] calling caller with ${result}")
            caller.downloadComp(result)
        }
    }

}


