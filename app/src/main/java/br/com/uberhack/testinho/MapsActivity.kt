package br.com.uberhack.testinho

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.com.uberhack.testinho.route.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.biker_found.view.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, RoutingListener {
    private lateinit var googleMap: GoogleMap
    private var isMapReady = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationListener = LocationListener()
    private val polylines = arrayListOf<Polyline>()
    private var lastLocation: Location? = null
    private val COLORS = intArrayOf(
        R.color.colorPrimary,
        R.color.colorPrimaryDark,
        R.color.colorAccent
    )

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest()
            .setExpirationDuration(10 * 60 * 1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationListener, Looper.myLooper())

        imageMobilityApp1.setOnClickListener(::onAppChoose)
        imageMobilityApp2.setOnClickListener(::onAppChoose)
        imageMobilityApp3.setOnClickListener(::onAppChoose)

        confirmTripButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            overlay.visibility = View.VISIBLE
            searchingDriverTextView.visibility = View.VISIBLE
            val r = Runnable {
                progressBar.visibility = View.GONE
                searchingDriverTextView.visibility = View.GONE

                AlertDialog.Builder(this@MapsActivity)
                    .setView(layoutInflater.inflate(R.layout.driver_found, null))
                    .setPositiveButton("OK") { _, _ ->
                        startActivityForResult(Intent(this@MapsActivity, ItineraryActivity::class.java), RESULT_RETURN)
                    }
                    .show()
            }
            Handler().postDelayed(r, 1000)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_RETURN -> {
                progressBar.visibility = View.VISIBLE
                searchingDriverTextView.visibility = View.VISIBLE
                searchingDriverTextView.text =
                    "Você está quase chegando no seu ponto.\nEstamos rastreando suas opções de mototaxi."
                val r = Runnable {
                    progressBar.visibility = View.GONE
                    searchingDriverTextView.visibility = View.GONE

                    val view = layoutInflater.inflate(R.layout.biker_found, null)
                    view.contactBikerButton.setOnClickListener {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("whatsapp://send?text=Hello World!&phone=+5527996099682\">")
                            )
                        )
                    }
                    AlertDialog.Builder(this@MapsActivity)
                        .setView(view)
                        .show()
                }
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage("+5527996099682", null, "Temos uma nova viagem para você!", null, null)
                Handler().postDelayed(r, 1000)
            }
        }
    }

    private fun onAppChoose(v: View) {
        imageMobilityApp1.borderWidth = 0
        imageMobilityApp2.borderWidth = 0
        imageMobilityApp3.borderWidth = 0

        (v as CircleImageView).borderWidth = 10
        confirmTripButton.setTextColor(Color.parseColor("#F26101"))
        confirmTripButton.isClickable = true
    }

    fun buildRoute(location: LatLng) {
        val waypoint = LatLng(-22.9946962, -43.2346334)
        val destination = LatLng(-22.9953358, -43.2429147)
        val routing = Routing.Builder()
            .travelMode(AbstractRouting.TravelMode.DRIVING)
            .withListener(this)
            .waypoints(location, waypoint, destination)
            .key(getString(R.string.google_maps_key))
            .build()
        routing.execute()
    }

    fun moveCameraWithMyLocation(location: Location) {
        if (isMapReady) {
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude), 15f
                )
            )
        }
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
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("MapsActivity", "------- onMapReady -------")
        this.googleMap = googleMap
        googleMap.isMyLocationEnabled = true

        isMapReady = true
    }

    override fun onRoutingFailure(e: RouteException?) {
        Log.e("MapsActivity", e?.message, e)
    }

    override fun onRoutingStart() {
    }

    override fun onRoutingSuccess(route: MutableList<Route>, shortestRouteIndex: Int) {
        if (polylines.size > 0) {
            for (poly in polylines) {
                poly.remove()
            }
        }
        polylines.clear()

        //add route(s) to the map.
        for (i in 0 until route.size) {

            val polyOptions = PolylineOptions()
            polyOptions.color(resources.getColor(COLORS[i]))
            polyOptions.width((10 + i * 3).toFloat())
            polyOptions.addAll(route[i].points)
            val polyline = googleMap.addPolyline(polyOptions)
            polylines.add(polyline)
        }

        val markerStart = MarkerOptions()
        lastLocation?.let {
            markerStart.position(LatLng(it.latitude, it.longitude))
            markerStart.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
            googleMap.addMarker(markerStart)
            return@let
        }

        val markerWaypoint = MarkerOptions()
        lastLocation?.let {
            markerWaypoint.position(LatLng(-22.9946962, -43.2346334))
            markerWaypoint.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
            googleMap.addMarker(markerWaypoint)
            return@let
        }
        val markerEnd = MarkerOptions()
        markerEnd.position(LatLng(-22.9953358, -43.2429147))
        markerEnd.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
        googleMap.addMarker(markerEnd)

        val bounds = LatLngBounds.Builder().run {
            include(markerStart.position)
            include(markerWaypoint.position)
            include(markerEnd.position)
            build()
        }
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 20)
        googleMap.animateCamera(cameraUpdate)
        val r = Runnable {
            progressBar.visibility = View.GONE
            overlay.visibility = View.GONE
        }
        Handler().postDelayed(r, 500)
    }

    override fun onRoutingCancelled() {

    }

    inner class LocationListener : LocationCallback() {
        override fun onLocationResult(location: LocationResult?) {
            location?.lastLocation?.let {
                this@MapsActivity.lastLocation = it
                moveCameraWithMyLocation(it)
                buildRoute(LatLng(it.latitude, it.longitude))
            }
        }
    }

    companion object {
        const val RESULT_RETURN = 99
    }
}
