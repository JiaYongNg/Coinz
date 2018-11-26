package com.example.user.coinz

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.google.gson.JsonObject
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions

import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode

import kotlinx.android.synthetic.main.activity_map.* //for fab,toolbar
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.os.AsyncTask
import android.util.Log



import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner
import java.util.Calendar
import java.text.DateFormat
import java.text.SimpleDateFormat
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.PersistableBundle
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.VectorSource
import kotlinx.android.synthetic.main.content_main.*
import java.time.LocalDate //api26++ no local date but got date test bonus feature

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener,MapboxMap.OnMarkerClickListener,MapboxMap.OnMapClickListener {



    private val tag = "MapActivity"
    private var numberofCoinsinWallet : Int = 0
    private val collectedCoins : ArrayList<Marker> = ArrayList()


    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    private lateinit var originLocation : Location
    private lateinit var permissionsManager : PermissionsManager
    private lateinit var locationEngine : LocationEngine
    private lateinit var locationLayerPlugin : LocationLayerPlugin


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        loadWallettoDevice()



        Mapbox.getInstance(applicationContext, getString(R.string.access_token))
        mapView = findViewById(R.id.mapboxMapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)



    }

    @SuppressWarnings("MissingPermission")
    override fun onStart(){
        super.onStart()
        mapView?.onStart()
        //val latlng = LatLng(originLocation.latitude, originLocation.longitude)


    }

    override fun onResume()
    {
        super.onResume()
        mapView?.onResume()
    }


    override fun onPause()
    {
        super.onPause()
        mapView?.onPause()
    }
    override fun onStop()
    {
        super.onStop()
        mapView?.onStop()
        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        locationEngine?.deactivate()
    }
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (outState != null){
            mapView?.onSaveInstanceState(outState)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
    //MAPBOXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapboxMap is null")
        } else {
            map = mapboxMap
            // Set user interface options
            map?.uiSettings?.isCompassEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true
            // Make location information available
            enableLocation()

            //map?.addOnMapClickListener(this)
            map?.setOnMarkerClickListener(this)
            addCoinstoMap()
        }
    }
    //get distance from curlocation to selected marker in metres
    private fun distancetoCoin(originLat:Double, originLon:Double,
                                 coinLat:Double, coinLon:Double)
                                 :Double{

        val p = 0.017453292519943295;    // Math.PI / 180
        val a = 0.5 - Math.cos((coinLat - originLat) * p)/2 +
                Math.cos(originLat * p) * Math.cos(coinLat * p) *
                (1 - Math.cos((coinLon - originLon) * p))/2

        return 12742 * Math.asin(Math.sqrt(a)) * 1000 // 2 * R; R = 6371 km
    }

    private fun circle(){

        val vectorSource = VectorSource("source-id", "mapbox://mapbox.2opop9hr")
        map?.addSource(vectorSource)

        val circleLayer = CircleLayer("layer-id", "source-id")
        circleLayer.sourceLayer = "museum-cusco"
        circleLayer.setProperties(
                PropertyFactory.visibility(Property.VISIBLE),
                PropertyFactory.circleRadius(8f),
                PropertyFactory.circleColor(Color.argb(1, 55, 148, 179))
        )

    }
    private fun loadWallettoDevice(){
        val wallet = File(applicationContext.filesDir,"wallet.txt") //used when app restarts
        val walletContent = wallet.readText()
        val walletData = walletContent.split("\n")
        Log.d(tag,"#walletData after game is started = " + walletData.size.toString())
        val coinNumber = walletData.size/4
        numberofCoinsinWallet = coinNumber
        for(i in 0..(coinNumber-1)){
            val coin = Marker(MarkerOptions()
                    .title(walletData[2 + i * 4])
                    .snippet(walletData[3 + i * 4])
                    .position(LatLng(walletData[0 + i * 4].toDouble(),walletData[1 + i * 4].toDouble())))
            collectedCoins.add(coin)
        }
    }
    private fun addCoinstoMap(){
        val coinzmap = File(applicationContext.filesDir, "coinzmap.geojson")
        val mapContents = coinzmap.readText()
        val fc = FeatureCollection.fromJson(mapContents)
        val file2 = File("/data/data/com.example.user.coinz/files","fc.txt")
        val file3 = File("/data/data/com.example.user.coinz/files","fc2.txt")
        var coinOnMapNumber = 0
        for(feature in fc.features().orEmpty()) {
            val coordinates = (feature.geometry() as Point).coordinates()
            val coinId = feature?.properties()?.get("id").toString()
            val coinValueCurrency = feature?.properties()?.get("value") .toString() + " " +  feature?.properties()?.get("currency") .toString()
            //remove "" from the string
            val id = coinId.replace("\"","")
            val valueCurrency = coinValueCurrency.replace("\"","")

            //check if this marker is already collected before adding it to the map
            val coin = Marker(MarkerOptions()
                    .title(id)
                    .snippet(valueCurrency)
                    .position(LatLng(coordinates[1],coordinates[0])))
            //if collectedCoins id and marker id not the same then add marker to map
            if(numberofCoinsinWallet == 0) {
                map?.addMarker(MarkerOptions()
                        .title(id)
                        .snippet(valueCurrency)
                        .position(LatLng(coordinates[1], coordinates[0])))
                coinOnMapNumber++
            }else{
                var coinNotOnMap = true
                for(i in 0..(numberofCoinsinWallet-1)) {
                    if (collectedCoins[i].title  == coin.title) {
                        coinNotOnMap = false
                    }
                }
                if(coinNotOnMap) {
                    map?.addMarker(MarkerOptions()
                            .title(id)
                            .snippet(valueCurrency)
                            .position(LatLng(coordinates[1], coordinates[0])))
                    coinOnMapNumber++
                }
            }
        }
        Log.d(tag,"coinsOnMap = " + coinOnMapNumber.toString() + ", coinsInWallet "  + numberofCoinsinWallet.toString())


        //file2.writeText(notCollectedMarkers.get(0).position.latitude.toString())
        //file2.writeText(notCollectedMarkers.toString())


    }
    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d(tag, "Permissions are granted")
            initialiseLocationEngine()
            initialiseLocationLayer()
        } else {
            Log.d(tag, "Permissions are not granted")
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine.apply {
            interval = 5000 // preferably every 5 seconds
            fastestInterval = 1000 // at most every second
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }
        val lastLocation = locationEngine.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        } else {
            locationEngine.addLocationEngineListener(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationLayer() {
        if (mapView == null) { Log.d(tag, "mapView is null") }
        else {
            if (map == null) { Log.d(tag, "map is null") }
            else {
                locationLayerPlugin = LocationLayerPlugin(mapView!!,map!!, locationEngine)
                locationLayerPlugin.apply {
                    setLocationLayerEnabled(true)
                    cameraMode = CameraMode.TRACKING
                    renderMode = RenderMode.NORMAL
                }
            }
        }
    }

    private fun setCameraPosition(location: Location) {
        val latlng = LatLng(location.latitude, location.longitude)
        map?.animateCamera(CameraUpdateFactory.newLatLng(latlng))
    }


    override fun onLocationChanged(location: Location?) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null")
        } else {
            originLocation = location
            setCameraPosition(originLocation)
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        Log.d(tag, "[onConnected] requesting location updates")
        locationEngine.requestLocationUpdates()
    }

    override fun onExplanationNeeded(permissionsToExplain : MutableList<String>?) {
        Log.d(tag, "Permissions: $permissionsToExplain")
        // Present popup message or dialog
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(tag, "[onPermissionResult] granted == $granted")
        if (granted) {
            enableLocation()
        } else {
            // Open a dialogue with the user
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val distance = distancetoCoin(originLocation.latitude,originLocation.longitude,
                marker.position.latitude,marker.position.longitude)
        val wallet = File(applicationContext.filesDir,"wallet.txt") //used when app restarts
        if(distance <= 25){
            Toast.makeText(applicationContext,"coin collected",Toast.LENGTH_SHORT).show()
            wallet.appendText(marker.position.latitude.toString() + '\n' + marker.position.longitude.toString()
                    + '\n' + marker.title +'\n' + marker.snippet + '\n') //store daily collected coin

            collectedCoins.add(marker) //realtime coin collection tracking
            map?.removeMarker(marker)
            Log.d(tag, marker.title + marker.snippet)
            //addmarkertowallet
        }else{
            Toast.makeText(applicationContext,"you are "+ Math.round(distance).toString()
                    +"metres away from the coin, get within 25metres to collect the coin",Toast.LENGTH_SHORT).show()
        }

        Log.d(tag,"coin collected to wallet")


        return true
    }
    override fun onMapClick(point: LatLng) {
        //destinationMarker = map?.addMarker(MarkerOptions().position(point))
        Log.d(tag,"AAA")
    }
}
