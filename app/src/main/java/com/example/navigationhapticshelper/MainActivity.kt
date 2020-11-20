package com.example.navigationhapticshelper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.location.DefaultLocationProvider
import com.mapbox.search.ui.view.SearchBottomSheetView


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    private var currentLongitude : Double = 0.0
    private var currentLatitude : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapboxSearchSdk.initialize(
            this.application,
            getString(R.string.mapbox_access_token_app),
            DefaultLocationProvider(this.application)
        )

        setContentView(R.layout.search_view)

        val searchBottomSheetView = findViewById<SearchBottomSheetView>(R.id.search_view)
        searchBottomSheetView.initializeSearch(
            savedInstanceState,
            SearchBottomSheetView.Configuration()
        )

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)

            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
            setCurrentLocationVariables()
        }



        searchBottomSheetView.addOnSearchResultClickListener {
            val intent = Intent(this, TurnByTurnHaptics::class.java)
            intent.putExtra(LATITUDE, it.coordinate?.latitude())
            intent.putExtra(LONGITUDE, it.coordinate?.longitude())
            intent.putExtra(LATITUDE_CURRENT, currentLatitude)
            intent.putExtra(LONGITUDE_CURRENT, currentLongitude)
            startActivity(intent)
        }

    }

    @SuppressLint("MissingPermission")
    private fun setCurrentLocationVariables() {

        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                currentLatitude = location!!.latitude
                currentLongitude = location.longitude
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) ===
                                PackageManager.PERMISSION_GRANTED)
                    ) {
                        setCurrentLocationVariables()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    companion object {
        const val LATITUDE = "com.example.myapplication.MESSAGE.LAT"
        const val LONGITUDE = "com.example.myapplication.MESSAGE.LONG"
        const val LATITUDE_CURRENT = "com.example.myapplication.MESSAGE.LAT_CUR"
        const val LONGITUDE_CURRENT = "com.example.myapplication.MESSAGE.LONG_CUR"

    }
}