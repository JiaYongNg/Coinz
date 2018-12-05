package com.example.user.coinz


import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
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
import android.support.v7.app.AppCompatActivity
import android.os.AsyncTask
import android.util.Log



import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner
import java.util.Calendar
import java.text.SimpleDateFormat
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.InputFilter
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUserMetadata
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

import org.json.JSONArray
import org.json.JSONObject

class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener,MapboxMap.OnMarkerClickListener,MapboxMap.OnMapClickListener{



    private val tag = "MapActivity"


    private var downloadDate = ""
    // Format: YYYY/MM/DD
    private val preferencesFile = "MyPrefsFile"
    // for storing preferences
    private var username : String? = ""
    private var walletDate : String = ""
    private var coinNumberForWallet = 1
    private var walletData = HashMap<String, Any?>()

    private var totalCoinCollected = 0 //reset this after adding coin to firestore wallet
    private var coinCollectedToday = 0
    private var coinCollectedTodayAchievement = 0

    private var dialogUsername : Dialog? = null
    private var dialogLogOut : Dialog? = null

    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    private lateinit var originLocation : Location
    private lateinit var permissionsManager : PermissionsManager
    private lateinit var locationEngine : LocationEngine
    private lateinit var locationLayerPlugin : LocationLayerPlugin




    private var firestore: FirebaseFirestore? = null
    private var firestoreUsernames: DocumentReference? = null       //List of used Usernames
    private var firestoreUserInfo: DocumentReference? = null        //userInfo is created by using account UID as a reference
    private var firestoreUserWallet: DocumentReference? = null      //userWallet is created by using Username,
                                                                    //which the player chooses on the first login
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        Mapbox.getInstance(applicationContext, getString(R.string.access_token))
        mapView = findViewById(R.id.mapboxMapView)
        mapView?.onCreate(savedInstanceState)



        login_button_login.setOnClickListener{
            logOut()
        }

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore?.firestoreSettings = settings
        firestoreUsernames = firestore?.collection("Users")?.document("Usernames")
        firestoreUserInfo = firestore?.collection("Users")?.document(mAuth?.currentUser?.uid!!)

        btn2_button.setOnClickListener { _->bankActivity()}
        already_have_account_text_view.setOnClickListener { _->shopActivity()}

        firstLoginCheck()
    }
    private fun userInfoActivity(){
        val intent = Intent(this, UserInfoActivity::class.java)
        intent.putExtra("username",username)
        intent.putExtra("accountId",mAuth?.currentUser?.uid!!)
        startActivity(intent)
    }
    private fun shopActivity(){
        val intent = Intent(this, ShopActivity::class.java)
        intent.putExtra("username",username)
        intent.putExtra("accountId",mAuth?.currentUser?.uid!!)
        startActivity(intent)
    }
    private fun bankActivity(){
        val intent = Intent(this, BankActivity::class.java)
        intent.putExtra("username",username)
        intent.putExtra("accountId",mAuth?.currentUser?.uid!!)
        intent.putExtra("DOLR",DownloadCompleteRunner.dolr)
        intent.putExtra("PENY",DownloadCompleteRunner.peny)
        intent.putExtra("QUID",DownloadCompleteRunner.quid)
        intent.putExtra("SHIL",DownloadCompleteRunner.shil)
        startActivity(intent)
    }
    //creates a log Out dialog
    private fun logOut(){
        dialogLogOut = AlertDialog.Builder(this)
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes")
            { _, _ ->
                //store collected Coins to wallet before sign out
                firestoreUserWallet?.update(walletData)?.addOnCompleteListener{
                    totalCoinCollected += walletData.size
                    coinCollectedToday += walletData.size
                    //update achievement
                    firestoreUserInfo?.update("Collect # coins",totalCoinCollected)?.addOnCompleteListener{
                        firestoreUserInfo?.update("Collect # coins in a day tracker",coinCollectedToday)?.addOnCompleteListener {
                            if(coinCollectedToday > coinCollectedTodayAchievement){
                                firestoreUserInfo?.update("Collect # coins in a day",coinCollectedToday)?.addOnCompleteListener {
                                    FirebaseAuth.getInstance().signOut()
                                    Log.d(tag,"logging out")
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }else{
                                FirebaseAuth.getInstance().signOut()
                                Log.d(tag,"logging out")
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }

                    }



                }

            }
            .setNegativeButton("No")
            { _, _ ->
                Log.d(tag,"Not logging out")
            }
            .show()
    }
    //check if this is user's first login, else get user's username from database
    private fun firstLoginCheck(){
        firestoreUserInfo?.get()?.addOnSuccessListener {document ->
            if(document != null){
               if(document.get("Username") == null ){
                   promptUsername()

               }else {
                   getFireStoreUsername()
               }
            }
        }
    }
    private fun getFireStoreUsername(){
        firestoreUserInfo?.get()
            ?.addOnSuccessListener { document ->
                if (document != null) {
                    username = document.get("Username").toString()
                    walletDate = document.get("walletDate").toString()
                    coinCollectedToday = document.get("Collect # coins in a day tracker").toString().toInt()
                    coinCollectedTodayAchievement = document.get("Collect # coins in a day").toString().toInt()
                    totalCoinCollected = document.get("Collect # coins").toString().toInt()

                    firestoreUserWallet = firestore?.collection("Users")?.document(username!!)
                    Log.d(tag, "logged in user's username is $username")

                    //getMapAsync is in firstLoginCheck because
                    //I need to know what the username is to load the correct wallet
                    //and to exclude adding the corresponding coins on the map
                    mapView?.getMapAsync(this)

                } else {
                    Log.d(tag, "Document not found")
                }
            }
            ?.addOnFailureListener { exception ->
                Log.e(tag, "get username failed with ", exception)
            }
    }

    //prompt player to write their username
    private fun promptUsername(){
        firestoreUsernames?.get()?.addOnSuccessListener { document ->
            if (document != null) {

                var usernameTaken = false
                var usernameInvalid = false

                //the username is all CAPS
                val usernameEditText = EditText(this)
                usernameEditText.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
                usernameEditText.hint = "Enter username"
                dialogUsername = AlertDialog.Builder(this)
                    .setTitle("Add username")
                    .setMessage("Username must be alphanumeric")
                    .setView(usernameEditText)
                    .setCancelable(false)
                    .setPositiveButton("Confirm") { _, _ ->
                        val usernameStr = usernameEditText.text.toString()

                        for(i in 1..document.data?.size!!){

                            if(usernameStr == document.get("user$i")){
                                usernameTaken = true
                            }
                        }
                        //check if username is alphanumeric
                        if(!usernameStr.matches(Regex("[A-Za-z0-9]+"))){
                            usernameInvalid = true
                        }
                        //username is not valid
                        if(usernameTaken || usernameInvalid){
                            Toast.makeText(applicationContext,"username taken or invalid",Toast.LENGTH_SHORT).show()
                            Log.d(tag,"username taken or invalid")
                            promptUsername()

                        }else{
                            //add username to the username database
                            firestoreUsernames?.update("user"+(document.data?.size!! + 1).toString(),usernameStr)

                            //create a link from the account UID to the username and initialise all data
                            val usernameData = HashMap<String, Any?>()
                            usernameData["Username"] = usernameStr
                            usernameData["Net worth"] = 0.0
                            usernameData["Booster 1"] = 0
                            usernameData["Booster 2"] = 0
                            usernameData["Booster 3"] = 0
                            usernameData["Booster 4"] = 0
                            usernameData["Collect # coins"] = 0
                            usernameData["Collect # coins in a day"] = 0 //achievement value
                            usernameData["Collect # coins in a day tracker"] = 0 //hidden value to keep track of how many coins collected today
                            usernameData["Coin giver"] = 0
                            usernameData["Coin getter"] = 0
                            usernameData["Login streak"] = 0

                            firestoreUserInfo?.set(usernameData)
                            username = usernameStr

                            //create user wallet document
                            val emptyData = HashMap<String, Any?>()
                            firestoreUserWallet = firestore?.collection("Users")?.document(username!!)
                            firestoreUserWallet?.set(emptyData)

                            mapView?.getMapAsync(this)
                            Toast.makeText(applicationContext,"username registered",Toast.LENGTH_SHORT).show()
                            Log.d(tag,"username registered")
                        }

                    }
                    .show()

            } else {
                //the app should never come here
                Log.e(tag, "No such document")
            }
        }

    }



    @SuppressWarnings("MissingPermission")
    override fun onStart(){
        super.onStart()
        //firstLoginCheck()
        mapView?.onStart()

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
        println("RRRRRRRRRRR"+walletData.size)
        //if user did not log out, store collected coin to wallet and update achievement value
        if(mAuth?.currentUser != null && coinNumberForWallet != 1){
            totalCoinCollected += walletData.size
            coinCollectedToday += walletData.size
            println("PPPPPPPPPPPPP"+totalCoinCollected)
            println(coinCollectedToday)
            println(walletData.size)
            firestoreUserWallet?.update(walletData)?.addOnSuccessListener {

                firestoreUserInfo?.update("Collect # coins",totalCoinCollected)
                firestoreUserInfo?.update("Collect # coins in a day tracker",coinCollectedToday)

                if(coinCollectedToday > coinCollectedTodayAchievement){
                    //update achievement
                    firestoreUserInfo?.update("Collect # coins in a day",coinCollectedToday)
                }
                Log.d(tag,"successfully uploaded ${walletData.size} collected coins in this session")}
                println("TTTTTTTTTTT"+DownloadCompleteRunner.numberOfCoinsinWallet)
                DownloadCompleteRunner.numberOfCoinsinWallet += walletData.size
                println("UUUUUUUUUUU"+DownloadCompleteRunner.numberOfCoinsinWallet)

            //reset walletData
                walletData = HashMap()
                coinNumberForWallet = 1


        }

    }
    override fun onStop()
    {

        super.onStop()
        mapView?.onStop()
        if(map!= null){
            locationEngine.removeLocationUpdates()
            locationLayerPlugin.onStop()
        }
        storeDownloadDate()

    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        if(map != null) {
            locationEngine.deactivate()
        }
        if (dialogUsername != null) {
            dialogUsername?.dismiss()
            dialogUsername = null
        }
        if(dialogLogOut != null){
            dialogLogOut?.dismiss()
            dialogLogOut = null
        }
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

        //if(distance <= 25){
        if(distance <= 1000){//for testing
            Toast.makeText(applicationContext,"coin collected",Toast.LENGTH_SHORT).show()

            //if this is the first collected coin, then update the userInfo
            //walletDate with the current date
            if(DownloadCompleteRunner.numberOfCoinsinWallet == 0){
                val date = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("yyyy/MM/dd")
                val currentDate = dateFormat.format(date)
                firestoreUserInfo?.update("walletDate",currentDate)
            }



            val coinData = HashMap<String, Any>()
            val valueCurrency = coin.snippet.split(" ")
            coinData["id"] = coin.title
            coinData["value"] = valueCurrency[0].toDouble()
            coinData["currency"] = valueCurrency[1]
            coinData["bankedIn"] = false
            coinData["coinGivenToOthers"] = false
            coinData["coinGivenByOthers"] = false
            coinData["coinGiverName"] = ""
            walletData["coin${(DownloadCompleteRunner.numberOfCoinsinWallet + coinNumberForWallet)}" ] = coinData
            println("QQQQQQQ"+walletData.size)
            coinNumberForWallet++
            Log.d(tag,"collected coin's data is "+coinData.toString())
            map?.removeMarker(coin)


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

    private fun storeDownloadDate(){
        Log.d(tag,"[onStop] Storing lastDownloadDate of $downloadDate")
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
        //3 dates to consider:
        //walletDate : (empties wallet if currentDate != walletDate) as coins only last until the end of the day
        //if currentDate == walletDate then just loadWallettoDevice
        //downloadDate : (redownload geojson map if currentDate != downloadDate)
        //currentDate :  shows current date
        val date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy/MM/dd")
        val currentDate = dateFormat.format(date)
        Log.d(tag,"currentDate is $currentDate")

        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use "" as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate","")
        // Write a message to ”logcat” (for debugging purposes)
        //downloadDate = "2018/10/07"  //test to force download map
        Log.d(tag,"Recalled lastDownloadDate is $downloadDate")

        //file that stores the map
        if (downloadDate != currentDate){
            //if dates are diff then download map from server, write geojson file to device and
            //add coins to map/empty wallet in onPostExecute,update downloadDate value
            val asyncDownload = DownloadFileTask(DownloadCompleteRunner,map,firestoreUserWallet,walletDate,currentDate)
            asyncDownload.execute("http://homepages.inf.ed.ac.uk/stg/coinz/$currentDate/coinzmap.geojson")
            downloadDate = currentDate


        }else{
            //if map already downloaded, load wallet to device, add coins to the map that aren't in the wallet
            if(walletDate != currentDate){
                DownloadCompleteRunner.emptyWallet(firestoreUserWallet)
                DownloadCompleteRunner.addCoinstoMap(map)
            }else{
                DownloadCompleteRunner.loadWalletAddCoins(firestoreUserWallet,map)
            }
        }
    }





    object DownloadCompleteRunner : DownloadCompleteListener {
        var result : String? = null
        private var walletCoinId = ArrayList<String>()                      //initialised by loadWallettoDevice(), store wallet's coins' id

        var numberOfCoinsinWallet = 0                                  //initialised by loadWallettoDevice()

        var shil : Double = 0.0                                     //initialised by addCoinsToMap(map:MapboxMap?)
        var dolr : Double = 0.0
        var quid : Double = 0.0
        var peny : Double = 0.0

        override fun downloadComplete(result: String) {
            this.result = result
        }
        override fun emptyWallet(firestoreUserWallet: DocumentReference?){
            val emptyData = HashMap<String, Any?>()
            firestoreUserWallet?.set(emptyData)
            //reset this array every time this method is called
            walletCoinId = ArrayList()
            numberOfCoinsinWallet = 0
        }
        //load wallet to device and addCoinstoMap at onSuccess
        override fun loadWalletAddCoins(firestoreUserWallet: DocumentReference?,map:MapboxMap?){
            //reset this array every time this method is called
            walletCoinId = ArrayList()

            firestoreUserWallet?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d("MapActivity", "DocumentSnapshot data: " + document.data)
                        numberOfCoinsinWallet = document.data?.size!!
                        if(numberOfCoinsinWallet > 0) {
                            for (i in 1..(document.data?.size!!)) {
                                walletCoinId.add(document["coin$i.id"].toString())
                            }
                        }
                        addCoinstoMap(map)
                    }else {
                        Log.d("MapActivity", "No such document")
                    }
                }
                ?.addOnFailureListener { exception ->
                    Log.e("MapActivity", "get user wallet failed with ", exception)
                }


        }
        override fun addCoinstoMap(map:MapboxMap?){
            val coinzMap = File("/data/data/com.example.user.coinz/files", "coinzmap.geojson")
            val mapContents = coinzMap.readText()
            val fc = FeatureCollection.fromJson(mapContents)

            //initialise rates
            val rates = JSONObject(mapContents).getJSONObject("rates")
            shil = rates.getDouble("SHIL")
            dolr = rates.getDouble("DOLR")
            quid = rates.getDouble("QUID")
            peny = rates.getDouble("PENY")


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
                if(numberOfCoinsinWallet != 0) {
                    for (i in 0..(numberOfCoinsinWallet - 1)) {
                        if (walletCoinId[i] == coin.title) {
                            coinNotOnMap = false
                        }
                    }
                }
                if(coinNotOnMap || numberOfCoinsinWallet == 0) {

                    map?.addMarker(MarkerOptions()
                            .title(id)
                            .snippet(valueCurrency)
                            .position(LatLng(coordinates[1], coordinates[0])))
                    coinOnMapNumber++
                }

            }
            Log.d("MapActivity","coinsOnMap = " + coinOnMapNumber.toString()
                    + ", coinsInWallet "  + numberOfCoinsinWallet.toString())
        }

    }
    class DownloadFileTask(private val caller : DownloadCompleteListener,private val map:MapboxMap?,
                           private val firestoreUserWallet: DocumentReference?,private val walletDate:String,
                           private val currentDate:String) : AsyncTask<String, Void, String>() {
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
            file.writeText(DownloadCompleteRunner.result!!)

            //before adding coins to the map, check if the wallet's coins are collected today or not
            //if collected today, load the wallet to device, else empty the wallet as coins only last
            //until the end of the day
            if(walletDate != currentDate){
                caller.emptyWallet(firestoreUserWallet)
                caller.addCoinstoMap(map)
            }else{
                caller.loadWalletAddCoins(firestoreUserWallet,map)
            }

        }

    } // end class DownloadFileTask

}