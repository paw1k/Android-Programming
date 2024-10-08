package edu.cs371m.fcgooglemaps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import edu.cs371m.fcgooglemaps.databinding.ActivityMainBinding
import edu.cs371m.fcgooglemaps.databinding.ContentMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*


class MainActivity
    : AppCompatActivity(),
    OnMapReadyCallback
{
    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private var locationPermissionGranted = false
    private lateinit var binding: ContentMainBinding

    private suspend fun processAddresses(addresses: List<Address>) {
        // XXX Write me.  Note: suspend fun, so withContext is wise.  move the camera
        withContext(Dispatchers.Main) {
            addresses.firstOrNull()?.let {
                val location = LatLng(it.latitude, it.longitude)
                map.addMarker(MarkerOptions().position(location))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f))
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)
        binding = activityMainBinding.contentMain

        checkGooglePlayServices()
        requestPermission()
        // Unfortunately, adding a map requires using the supportFragmentManager
        // https://developers.google.com/maps/documentation/android-sdk/map#to_add_a_map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFrag) as SupportMapFragment
        // XXX Write me.  Load the map (async) and create Geocoder
        geocoder = Geocoder(this, Locale.getDefault())
        mapFragment.getMapAsync(this)

        binding.goBut.setOnClickListener {
            val locationName = binding.mapET.text.toString()
            Log.d("Geocoding ", locationName)
            // call getFromLocationName on a background thread
            if (Build.VERSION.SDK_INT >= 33) {
                // Use this function, with a lambda for the Geocoder.GeocodeListener
                // https://developer.android.com/reference/android/location/Geocoder#getFromLocationName(java.lang.String,%20int,%20android.location.Geocoder.GeocodeListener)
                // MainScope().launch is your friend.
                // XXX Write me
                MainScope().launch {
                    val addresses = withContext(Dispatchers.IO) {
                        try {
                            geocoder.getFromLocationName(locationName, 1)
                        } catch (e: IOException) {
                            listOf<Address>()
                        }
                    }
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            val address = addresses.first()
                            val latLng = LatLng(address.latitude, address.longitude)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
                        } else {
                            Toast.makeText(this@MainActivity, "Address not found", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                // Use the deprecated API
                // https://developer.android.com/reference/android/location/Geocoder#getFromLocationName(java.lang.String,%20int)
                // Launch the coroutine first and call getFromLocationName inside the coroutine
                // XXX Write me
                MainScope().launch {
                    val addresses = withContext(Dispatchers.IO) {
                        geocoder.getFromLocationName(locationName, 1)
                    }
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            val address = addresses.first()
                            val latLng = LatLng(address.latitude, address.longitude)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
                        } else {
                            Toast.makeText(this@MainActivity, "Address not found", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        // This code is correct, but it assumes an EditText in your layout
        // called mapET and a go button called goBut
        binding.mapET.setOnEditorActionListener { /*v*/_, actionId, event ->
            // If user has pressed enter, or if they hit the soft keyboard "send" button
            // (which sends DONE because of the XML)
            if ((event != null
                        &&(event.action == KeyEvent.ACTION_DOWN)
                        &&(event.keyCode == KeyEvent.KEYCODE_ENTER))
                || (actionId == EditorInfo.IME_ACTION_DONE)) {
                hideKeyboard()
                binding.goBut.callOnClick()
            }
            false
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
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if( locationPermissionGranted ) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // We put this here to satisfy the compiler, which wants to know we checked
                // permissions before setting isMyLocationEnabled
                return
            }
            // XXX Write me.
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
        }

        // XXX Write me.
        // Start the map at the Harry Ransom center
        val initialLocation = geocoder.getFromLocationName("Harry Ransom center", 1)?.get(0)
        if (initialLocation != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(initialLocation.latitude, initialLocation.longitude), 15.0f))
        }
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.setOnMapClickListener { latLng ->
            val marker = map.addMarker(MarkerOptions().position(latLng))
            val latitude = String.format("%.3f", latLng.latitude)
            val longitude = String.format("%.3f", latLng.longitude)
            marker?.title = "$latitude, $longitude"
            marker?.showInfoWindow()
        }
        map.setOnMapLongClickListener {
            map.clear()
        }
        binding.clearBut.setOnClickListener {
            map.clear()
        }
    }

    // Everything below here is correct

    // An Android nightmare
    // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    // https://stackoverflow.com/questions/7789514/how-to-get-activitys-windowtoken-without-view
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }

    private fun checkGooglePlayServices() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode =
            googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 257)?.show()
            } else {
                Log.i(javaClass.simpleName,
                    "This device must install Google Play Services.")
                finish()
            }
        }
    }

    private fun requestPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    locationPermissionGranted = true
                } else -> {
                Toast.makeText(this,
                    "Unable to show location - permission required",
                    Toast.LENGTH_LONG).show()
            }
            }
        }
        locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }
}

