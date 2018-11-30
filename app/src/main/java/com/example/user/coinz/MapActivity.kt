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
import java.util.Date
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
import java.time.LocalDate //api26++ no local date but got date test bonus feature

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener,MapboxMap.OnMarkerClickListener,MapboxMap.OnMapClickListener{



    private val tag = "MapActivity"


    private var downloadDate = ""
    // Format: YYYY/MM/DD
    private val preferencesFile = "MyPrefsFile"
    // for storing preferences


    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    private lateinit var originLocation : Location
    private lateinit var permissionsManager : PermissionsManager
    private lateinit var locationEngine : LocationEngine
    private lateinit var locationLayerPlugin : LocationLayerPlugin


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

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
        val aa = JSONObject()
        aa.put("a","1")
        aa.put("aa","2")
        aa.put("aaa","3")
        val bb = JSONObject()
        bb.put("b","1")
        bb.put("bb","2")
        bb.put("bbb","3")
        val aaa = JSONArray()
        aaa.put(4,5)
        aaa.put(aa)
        aaa.put(bb)
        println(aaa.toString())
        println(aaa.getJSONObject(6).get("b"))
        //add coin to wallet in onmarkerclick
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
        locationEngine.removeLocationUpdates()
        locationLayerPlugin.onStop()
        storeCollectedCoinsIntoWallet()
        storeDownloadDate()

    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        locationEngine.deactivate()
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
            //addCoinstoMap()
            downloadJSONMapandAddCoinsToMap(map)

        }
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


    override fun onMarkerClick(coin: Marker): Boolean {
        //add coin to wallet and remove coin from map
        val coinLocation = Location("")
        coinLocation.latitude = coin.position.latitude
        coinLocation.longitude = coin.position.longitude
        val distance = originLocation.distanceTo(coinLocation)

        if(distance <= 25){
            Toast.makeText(applicationContext,"coin collected",Toast.LENGTH_SHORT).show()
            val collectedCoin = JSONObject()
            collectedCoin.put("latitude",coin.position.latitude)
            collectedCoin.put("longitude",coin.position.longitude)
            collectedCoin.put("id",coin.title)
            collectedCoin.put("valueCurrency",coin.snippet)
            DownloadCompleteRunner.collectedCoinsArray.put(collectedCoin)
            map?.removeMarker(coin)
            Log.d(tag,"coin collected to wallet")
        }else{
            Toast.makeText(applicationContext,"you are "+ Math.round(distance).toString()
                    +"metres away from the coin, get within 25metres to collect the coin",Toast.LENGTH_SHORT).show()
            Log.d(tag,"coin too far away")
        }




        return true
    }
    override fun onMapClick(point: LatLng) {
        //destinationMarker = map?.addMarker(MarkerOptions().position(point))
        Log.d(tag,"AAA")
    }


//JSONNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNnn


    private fun storeCollectedCoinsIntoWallet(){
        val wallet = File(applicationContext.filesDir,"wallet.txt") //used when app restarts
        wallet.writeText(DownloadCompleteRunner.collectedCoinsArray.toString())
    }
    private fun storeDownloadDate(){
        Log.d(tag,"[onPause] Storing lastDownloadDate of $downloadDate")
        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes.
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)
        // Apply the edits!
        editor.apply()
    }

    //called during midnight and onMapReady
    //onMapReady -> download JSONMap -> add coin to map
    private fun downloadJSONMapandAddCoinsToMap(map:MapboxMap?){
        //11.59-12am transition and download new map
        val date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy/MM/dd")
        val currentDate = dateFormat.format(date)
        Log.d(tag,"[onStart] currentDate is $currentDate")

        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate","")
        // Write a message to ”logcat” (for debugging purposes)
        //downloadDate = "2018/10/07"  //test to force download map
        Log.d(tag,"[onStart] Recalled lastDownloadDate is $downloadDate")

        //file that stores the map
        if (currentDate!= downloadDate){
            //if dates are diff then download map from server, write geojson file to device and add coins to map
            //in onPostExecute,update downloadDate value
            val asyncDownload = DownloadFileTask(DownloadCompleteRunner,map)
            asyncDownload.execute("http://homepages.inf.ed.ac.uk/stg/coinz/$currentDate/coinzmap.geojson")
            downloadDate = currentDate

        }else{
            //if map already downloaded, load wallet to device, add coins to the map that aren't in the wallet
            DownloadCompleteRunner.loadWallettoDeivce()
            DownloadCompleteRunner.addCoinstoMap(map)
        }
    }





    object DownloadCompleteRunner : DownloadCompleteListener {
        var result : String? = null
        var collectedCoinsArray = JSONArray()                       //initialised by loadWallettoDevice()
                                                                    //and used to store collected coins in wallet in JSON format
        private var numberofCoinsinWallet:Int = 0                   //initialised by loadWallettoDevice()

        var SHIL : Double = 0.0                                     //initialised by addCoinsToMap(map:MapboxMap?)
        var DOLR : Double = 0.0
        var QUID : Double = 0.0
        var PENY : Double = 0.0

        override fun downloadComplete(result: String) {
            this.result = result
        }
        override fun loadWallettoDeivce(){
            val wallet = File("/data/data/com.example.user.coinz/files","wallet.txt") //used when app restarts
            collectedCoinsArray = JSONArray(wallet.readText())
            numberofCoinsinWallet = collectedCoinsArray.length()

        }
        override fun addCoinstoMap(map:MapboxMap?){
            val coinzmap = File("/data/data/com.example.user.coinz/files", "coinzmap.geojson")
            val mapContents = coinzmap.readText()
            val fc = FeatureCollection.fromJson(mapContents + "")

            //initialise rates
            val rates = JSONObject(mapContents).getJSONObject("rates")
            SHIL = rates.getDouble("SHIL")
            DOLR = rates.getDouble("DOLR")
            QUID = rates.getDouble("QUID")
            PENY = rates.getDouble("PENY")


            //indicate how many coins are on the map for debugging purpose
            var coinOnMapNumber = 0
            for(feature in fc.features().orEmpty()) {
                val coordinates = (feature.geometry() as Point).coordinates()
                val coinId = feature?.properties()?.get("id").toString()
                val coinValueCurrency = feature?.properties()?.get("value").toString() + " " +  feature?.properties()?.get("currency") .toString()
                //remove "" from the string
                val id = coinId.replace("\"","")
                val valueCurrency = coinValueCurrency.replace("\"","")


                //check if this coin is already collected before adding it to the map
                val coin = Marker(MarkerOptions()
                        .title(id)
                        .snippet(valueCurrency)
                        .position(LatLng(coordinates[1],coordinates[0])))

                var coinNotOnMap = true

                //if collectedCoins id and marker id not the same then add marker to map
                if(numberofCoinsinWallet != 0) {
                    for (i in 0..(numberofCoinsinWallet - 1)) {
                        if (collectedCoinsArray.getJSONObject(i).getString("id") == coin.title) {
                            coinNotOnMap = false
                        }
                    }
                }
                if(coinNotOnMap || DownloadCompleteRunner.numberofCoinsinWallet == 0) {
                    map?.addMarker(MarkerOptions()
                            .title(id)
                            .snippet(valueCurrency)
                            .position(LatLng(coordinates[1], coordinates[0])))
                    coinOnMapNumber++
                }



            }
            Log.d("MapActivity","coinsOnMap = " + coinOnMapNumber.toString() + ", coinsInWallet "  + numberofCoinsinWallet.toString())
            //Log.d("MapActivity","coinsOnMap = " + coinOnMapNumber.toString())


            //file2.writeText(notCollectedMarkers.get(0).position.latitude.toString())
            //file2.writeText(notCollectedMarkers.toString())


        }

    }
    class DownloadFileTask(private val caller : DownloadCompleteListener,private val map:MapboxMap?) : AsyncTask<String, Void, String>() {
    //class DownloadFileTask(private val caller : DownloadCompleteListener) : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg urls: String): String =
                try {
                    loadFileFromNetwork(urls[0])
                } catch (e: IOException) {
                    "Unable to load content. Check your network connection"
                }


        private fun loadFileFromNetwork(urlString: String): String {
            val stream: InputStream = downloadUrl(urlString)
            // Read input from stream, build result as a string
            val s = Scanner(stream).useDelimiter("\\A")
            val result = if (s.hasNext()) s.next() else ""
            s.close()
            return result
        }//scanner copied from https://stackoverflow.com/questions/309424/how-to-read-convert-an-inputstream-into-a-string-in-java



        // Given a string representation of a URL, sets up a connection and gets an input stream.
        @Throws(IOException::class)
        private fun downloadUrl(urlString: String): InputStream {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            // Also available: HttpsURLConnection
            conn.readTimeout = 10000 // milliseconds
            conn.connectTimeout = 15000 // milliseconds
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect() // Starts the query
            return conn.inputStream
        }
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            caller.downloadComplete(result)

            //write downloaded json file to device
            val file = File("/data/data/com.example.user.coinz/files","coinzmap.geojson")
            file.writeText(DownloadCompleteRunner.result + "")
            //new day empty wallet
            val file2 = File("/data/data/com.example.user.coinz/files","wallet.txt")
            file2.writeText("")

            caller.addCoinstoMap(map)

        }

    } // end class DownloadFileTask



}
