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
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.kml.KmlLayer
import java.io.*
import java.net.URL


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, DownloadListener, GoogleMap.OnMarkerClickListener {

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
        val message1 = intent.getStringExtra("song")
        val message2 = intent.getStringExtra("map")
        val URL = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${message1}/map${message2}.kml"
        val downloader = DownloadKmlTask(this, this, message1, message2)
        downloader.execute(URL)

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

        /*val snackbar = Snackbar
                .make(findViewById<View>(android.R.id.content), "Word Collected : way ", Snackbar.LENGTH_INDEFINITE)
        snackbar.show()*/
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
    }

    fun getContext():Context{
        return this
    }
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        Log.d(tag,"downloaded stream is "+stream.toString())
        kmllayer = KmlLayer(mMap, stream, this)
        kmllayer!!.addLayerToMap()

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(55.944335, -3.188970)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        try {
            // Visualise current position with a small blue circle
            mMap.isMyLocationEnabled = true
        } catch (se: SecurityException) {
            throw(se)
        }
        // Add ”My location” button to the user interface
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setOnMarkerClickListener(this)
    }
    override fun onMarkerClick(marker: Marker): Boolean {
        val snackbar = Snackbar
                .make(findViewById<View>(android.R.id.content), "Word Collected : ${marker.title} way", Snackbar.LENGTH_INDEFINITE)
        snackbar.show()
        marker.remove()
        progress.count = progress.count + 1
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
    }
}

interface DownloadListener {
    fun downloadComp(result: String)
}

class DownloadKmlTask( val caller: DownloadListener,  val context: Context,
                       val song :String, val map:String) : AsyncTask<String, Void, String>() {
    val tag = "DownloadKmlTask"
    var cont = context
    var sng = song
    var mp = map
    var filename = "Song ${sng}, map ${mp}"
    var file = File(cont.getFilesDir(), filename)

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
        val outputStream: FileOutputStream
        try {
            outputStream = cont.openFileOutput(filename, Context.MODE_PRIVATE)
            outputStream.write(fileString.toString().toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return fileString.toString()
    }

    override fun onPostExecute(result: String?) {
        if (result != null){
            Log.d(tag,"[onPostExecute] calling caller with ${result}")
            caller.downloadComp(result)
        }
    }

}


