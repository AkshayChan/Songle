package com.example.akshayc.songle

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_which_song.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class WhichSong : AppCompatActivity() {

    var songNoSpinner: Spinner? = null
    var mapNoSpinner: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_which_song)
        songNoSpinner = findViewById(R.id.spinner1)
        mapNoSpinner = findViewById(R.id.spinner2)

        var songNolist = DbHelper.Instance(this).getAllSongData()
        var splist: ArrayList<String>
        splist = arrayListOf()
        for (item in songNolist) {
            splist.add(item.Number)
        }

        var adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, splist)

        songNoSpinner?.adapter = adapter


        var mapNolist: ArrayList<String> = arrayListOf("1", "2", "3", "4", "5")
        var mapNoAdapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, mapNolist)
        mapNoSpinner?.adapter = mapNoAdapter

    }


    fun sendMessage(view: View): Unit {
        checkOrAskForRuntimePermission()
    }

    fun callMapActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        val message1 = spinner1.selectedItem.toString().trim()
        val message2 = spinner2.selectedItem.toString().trim()
        Log.d("Spinner", message1 + " : " + message2)

        intent.putExtra("song", message1)
        intent.putExtra("map", message2)
        startActivity(intent)
        finish()
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }


    fun checkOrAskForRuntimePermission() {
        // val permissionCheck = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)

        var hasMapPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (hasMapPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel("You need to allow Fine location for map functionality",
                        DialogInterface.OnClickListener { _, _ ->
                            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                    1)
                        })
                //return@setOnClickListener
            }
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            //permission granted
            callMapActivity()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT)
                        .show()
                callMapActivity()
            } else {
                // Permission Denied
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT)
                        .show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}