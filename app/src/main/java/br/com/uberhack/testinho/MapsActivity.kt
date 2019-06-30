package br.com.uberhack.testinho

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.uberhack.testinho.route.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, RoutingListener {
    private lateinit var googleMap: GoogleMap
    private var isMapReady = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationListener = LocationListener()

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
    }

    fun onAppChoose(v: View) {
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
            .build();
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
        Toast.makeText(this, "RoutingFailure", Toast.LENGTH_SHORT).show()
    }

    override fun onRoutingStart() {
        Toast.makeText(this, "RoutingStart", Toast.LENGTH_SHORT).show()
    }

    override fun onRoutingSuccess(route: MutableList<Route>?, shortestRouteIndex: Int) {
        Toast.makeText(this, "RoutingSuccess", Toast.LENGTH_SHORT).show()
    }

    override fun onRoutingCancelled() {
        Toast.makeText(this, "RoutingCancelled", Toast.LENGTH_SHORT).show()
    }

    inner class LocationListener : LocationCallback() {
        override fun onLocationResult(location: LocationResult?) {
            location?.lastLocation?.let {
                moveCameraWithMyLocation(it)
            }
        }
    }
}
