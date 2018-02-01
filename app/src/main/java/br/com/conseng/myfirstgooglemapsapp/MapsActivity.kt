package br.com.conseng.myfirstgooglemapsapp

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val defaultLatitude: Double = 18.55
    private val defaultLongitude: Double = 73.94
    private val defaultRadius: Int = 10000
    private val defaultType = "Hospitality"

//    private var hitApi:HitApi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        mMap = googleMap
        /*hitApi = */HitApi(this, defaultLatitude, defaultLongitude, defaultRadius, defaultType).execute()
    }

    /**
     * Store the position of one location.
     * @property [context] the application context.
     * @property [latitude] the current location latitude.
     * @property [longitude] the current location longitude
     * @property [radius] the radius distance (meters) to search for the specific place [type].
     * @property [type] identify the type of the place to search around.
     */
    private inner class HitApi(private val context: Context,
                               var latitude: Double, var longitude: Double,
                               var radius: Int, var type: String) : AsyncTask<Void, Void, String>() {

        /**
         * This method will be called to publish updates on the UI thread.         *
         * @param [params] The parameters of the task.         *
         * @return A result, defined by the subclass of this task.
         */
        override fun doInBackground(vararg params: Void?): String {
            val result = GooglePlaceApis().getPlacesJson(context, latitude, longitude, radius, type)
            return result
        }

        /**
         * Runs on the UI thread after [doInBackground].
         * @param [result] the result of the operation computed by [doInBackground].
         */
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val gson = GsonBuilder().create()
            val root = gson.fromJson(result, PlacesRootClass::class.java)
            if (root.status.equals("REQUEST_DENIED")) {
                Toast.makeText(context, "ERROR: Access denied!", Toast.LENGTH_LONG).show()
                println("ERROR: ${root.error_message}")
            } else {
                addMarkers(root)
            }
        }
    }

    fun addMarkers(root: PlacesRootClass) {
        for (result : QuerySearchNearbyResult in root.results) {
            val p = LatLng(result.geometry.location.lat, result.geometry.location.lng)
            mMap.addMarker(MarkerOptions().position(p).title(result.name))
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(defaultLatitude, defaultLongitude)))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
    }
}
