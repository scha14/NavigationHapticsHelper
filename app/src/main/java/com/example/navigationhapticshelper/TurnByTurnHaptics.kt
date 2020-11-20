package com.example.navigationhapticshelper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.navigation.base.internal.extensions.applyDefaultParams
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback
import com.mapbox.navigation.ui.NavigationViewOptions
import com.mapbox.navigation.ui.OnNavigationReadyCallback
import com.mapbox.navigation.ui.listeners.NavigationListener
import com.mapbox.navigation.ui.listeners.SpeechAnnouncementListener
import com.mapbox.navigation.ui.map.NavigationMapboxMap
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class TurnByTurnHaptics : AppCompatActivity(), OnNavigationReadyCallback, NavigationListener {

    private lateinit var navigationMapBoxMap: NavigationMapboxMap
    private lateinit var mapBoxNavigation: MapboxNavigation
    private lateinit var route: DirectionsRoute

    private var currentLatitude: Double? = 0.0
    private var currentLongitude: Double? = 0.0
    private var destinationLatitude: Double? = 0.0
    private var destinationLongitude: Double? = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_access_token_app))
        getOriginAndDestinationFromIntent(intent)
        getRoute(intent)
        setContentView(R.layout.activity_main)
        navigationView.onCreate(savedInstanceState)

    }

    override fun onNavigationReady(isRunning: Boolean) {
        if (!isRunning && !::navigationMapBoxMap.isInitialized) {
            if (navigationView.retrieveNavigationMapboxMap() != null) {
                this.navigationMapBoxMap = navigationView.retrieveNavigationMapboxMap()!!
                navigationView.retrieveMapboxNavigation()?.let { this.mapBoxNavigation = it }
                val optionsBuilder = NavigationViewOptions.builder(this)
                optionsBuilder.navigationListener(this)
                optionsBuilder.directionsRoute(route)
                optionsBuilder.shouldSimulateRoute(true)
                optionsBuilder.speechAnnouncementListener(speechAnnouncementListener)
                navigationView.startNavigation(optionsBuilder.build())
            }
        }
    }

    private val speechAnnouncementListener =
        SpeechAnnouncementListener { announcement ->
            val announce = announcement?.announcement().toString().toLowerCase()
            if (announce.startsWith("in")) {
            } else {
                if (announce.startsWith("turn right") || announce.contains("sharp right")) {
                    sendHapticTurnSignalToRaspPi("right")
                } else if (announce.startsWith("turn left") || announce.contains("sharp left")) {
                    sendHapticTurnSignalToRaspPi("left")
                } else if (announce.contains("destination is")) {
                    sendHapticTurnSignalToRaspPi("arrive")
                }
            }
            announcement!!
        }

    private fun sendHapticTurnSignalToRaspPi(turn: String) {
        val okHttpClient = OkHttpClient()
        var url = "http://" + getString(R.string.pi_ip_address)+ "/?"
        when {
            turn == "right" -> {
                url += "led1=1"
            }
            turn == "left" -> {
                url += "led2=1"
            }
            turn == "arrive" -> {
                url += "led3=1"
            }
        }


        val request = Request.Builder()
            .url(url)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("resultloghere", "reach 1$e")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("resultloghere", "reach 2$response")
            }
        })
    }


    private val requestroutesCallback = object : RoutesRequestCallback {

        override fun onRoutesReady(routes: List<DirectionsRoute>) {
            if (routes.isEmpty()) {
                return
            }
            route = routes[0]
            navigationView.initialize(this@TurnByTurnHaptics)
        }

        override fun onRoutesRequestFailure(throwable: Throwable, routeOptions: RouteOptions) {

        }

        override fun onRoutesRequestCanceled(routeOptions: RouteOptions) {

        }
    }

    private fun getOriginAndDestinationFromIntent(intent: Intent): List<Point> {
        currentLongitude = intent.getDoubleExtra(MainActivity.LONGITUDE_CURRENT, 0.0)
        currentLatitude = intent.getDoubleExtra(MainActivity.LATITUDE_CURRENT, 0.0)
        destinationLatitude = intent.getDoubleExtra(MainActivity.LATITUDE, 0.0)
        destinationLongitude = intent.getDoubleExtra(MainActivity.LONGITUDE, 0.0)

        return listOf(
            Point.fromLngLat(currentLongitude!!, currentLatitude!!), Point.fromLngLat(
                destinationLongitude!!, destinationLatitude!!
            )
        )
    }

    private fun getRoute(intent: Intent) {
        val navigationOptions =
            MapboxNavigation.defaultNavigationOptionsBuilder(
                this,
                getString(R.string.mapbox_access_token_app)
            )
                .build()
        val mapboxNavigation = MapboxNavigation(navigationOptions)

        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultParams()
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .accessToken(getString(R.string.mapbox_access_token_app))
                .bannerInstructions(true)
                .coordinates(
                    getOriginAndDestinationFromIntent(intent)
                )
                .build(), requestroutesCallback
        )
    }


    override fun onNavigationRunning() {
// Empty because not needed in this example
    }

    override fun onNavigationFinished() {
        finish()
    }

    override fun onCancelNavigation() {
        navigationView.stopNavigation()
        finish()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        navigationView.onLowMemory()
    }

    override fun onStart() {
        super.onStart()
        navigationView.onStart()
    }

    override fun onResume() {
        super.onResume()
        navigationView.onResume()
    }

    override fun onStop() {
        super.onStop()
        navigationView.onStop()

    }

    override fun onPause() {
        super.onPause()
        navigationView.onPause()
    }

    override fun onDestroy() {
        navigationView.onDestroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!navigationView.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        navigationView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        navigationView.onRestoreInstanceState(savedInstanceState)
    }
}



