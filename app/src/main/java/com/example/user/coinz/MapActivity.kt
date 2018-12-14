package com.example.user.coinz

import android.app.Dialog
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

import kotlinx.android.synthetic.main.activity_map.*
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.os.AsyncTask
import android.util.Log

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import android.content.Intent
import android.graphics.*
import android.location.Location
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.InputFilter
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.mapbox.mapboxsdk.annotations.IconFactory


import org.json.JSONObject
import java.lang.ref.WeakReference
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import java.util.concurrent.TimeUnit

class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener,MapboxMap.OnMarkerClickListener{



    private val tag = "MapActivity"

    private var downloadDate = ""
    // Format: YYYY/MM/DD
    private val preferencesFile = "MyPrefsFile"
    // for storing preferences

    //used when collecting coin
    private var username : String? = ""
    private var walletDate : String = ""
    private var coinIndexForWallet = 1
    private var walletData = HashMap<String, Any?>()

    //used for achievements
    private var totalCoinCollected = 0
    private var coinCollectedToday = 0
    private var coinCollectedTodayAchievement = 0

    //used for daily login values
    private var loginStreak = 0
    private var coinWithDoubleVal = 0
    private var booster4Quantity = 0

    //used to determine if booster is active
    private var boosterTimer:CountDownTimer? = null
    private var boosterActive = false

    //midnight checker
    private var midnightTimer:CountDownTimer? = null
    private var midnightDialog : Dialog? = null

    //check for internet
    private var dialogInternet : Dialog? = null

    //used to determine a situation where home or back button is pressed
    //and calls updateUserInfo method in onPause when this var is false
    private var switchToAnotherActivity = false

    private var dialogUsername : Dialog? = null
    private var dialogLogOut : Dialog? = null

    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    private var originLocation : Location? = null
    private lateinit var permissionsManager : PermissionsManager
    private var locationEngine : LocationEngine?= null
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



        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore?.firestoreSettings = settings

        firestoreUsernames = firestore?.collection("Users")?.document("UsernamesDatabase")
        firestoreUserInfo = firestore?.collection("Users")?.document(mAuth?.currentUser?.uid!!)

        //sets up the four buttons on the Map activity
        log_out_button.setOnClickListener {
            logOut()
        }

        bank_button.setOnClickListener{
            bank_button.setBackgroundColor(Color.GREEN)
            updateUserInfo { bankActivity() }
        }

        shop_button.setOnClickListener {
            shop_button.setBackgroundColor(Color.GREEN)
            updateUserInfo { shopActivity() }
        }

        my_profile_button.setOnClickListener {
            my_profile_button.setBackgroundColor(Color.GREEN)
            updateUserInfo { userInfoActivity() }
        }

        firstLoginCheck()
    }


    private fun userInfoActivity(){
        switchToAnotherActivity = true
        Log.d(tag,"switching to userInfoActivity")
        val intent = Intent(this, UserInfoActivity::class.java)
        intent.putExtra("username",username)
        intent.putExtra("accountId",mAuth?.currentUser?.uid!!)
        startActivity(intent)
    }

    private fun shopActivity(){
        switchToAnotherActivity = true
        Log.d(tag,"switching to shopActivity")
        val intent = Intent(this, ShopActivity::class.java)
        intent.putExtra("username",username)
        intent.putExtra("accountId",mAuth?.currentUser?.uid!!)
        startActivity(intent)
    }

    private fun bankActivity(){
        switchToAnotherActivity = true
        Log.d(tag,"switching to BankActivity")
        val intent = Intent(this, BankActivity::class.java)
        intent.putExtra("username",username)
        intent.putExtra("accountId",mAuth?.currentUser?.uid!!)
        intent.putExtra("DOLR",DownloadCompleteRunner.dolr)
        intent.putExtra("PENY",DownloadCompleteRunner.peny)
        intent.putExtra("QUID",DownloadCompleteRunner.quid)
        intent.putExtra("SHIL",DownloadCompleteRunner.shil)
        startActivity(intent)
    }

    //used when switching activity or logging out or quitting the game
    private fun updateUserInfo(switchActivity:() -> Unit){
        //check if there is internet connection
        internetDialog(false)

        //disable all UI buttons until local wallet data is uploaded to firestore
        //because we need updated wallet data to be displayed in the next Activity's UI
        //and we don't want the player to spam the UI buttons while they are waiting
        //which could cause multiple Activity instance of the same type to be created
        my_profile_button.isClickable = false
        bank_button.isClickable = false
        shop_button.isClickable = false
        log_out_button.isClickable = false
        firestoreUserWallet?.update(walletData)?.addOnSuccessListener {
            totalCoinCollected += walletData.size
            coinCollectedToday += walletData.size

            //update achievement
            val achievementData = HashMap<String, Any?>()
            achievementData["Collect # coins"] = totalCoinCollected
            achievementData["Collect # coins in a day tracker"] = coinCollectedToday
            achievementData["Coin with double value"] = coinWithDoubleVal

            achievementData["Collect # coins in a day"] =
                    if(coinCollectedToday > coinCollectedTodayAchievement)
                        coinCollectedToday
                    else
                        coinCollectedTodayAchievement

            firestoreUserInfo?.update(achievementData)?.addOnCompleteListener {
                //wait for update to complete before executing switchActivity()
                Log.d(tag,"successfully uploaded ${walletData.size} collected coins in this session")
                firestoreUserWallet?.get()?.addOnSuccessListener {document->
                    if(document != null){
                        DownloadCompleteRunner.numberOfCoinsinWallet = document.data?.size!!
                    }
                }

                my_profile_button.isClickable = true
                bank_button.isClickable = true
                shop_button.isClickable = true
                log_out_button.isClickable = true

                //reset walletData
                walletData = HashMap()
                coinIndexForWallet = 1
                switchActivity()
            }

        }
    }

    //creates a log Out dialog
    private fun logOut(){
        dialogLogOut = AlertDialog.Builder(this)
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes")
            { _, _ ->
                Log.d(tag,"logging out")
                updateUserInfo { performLogOut() }
            }

            .setNegativeButton("No")
            { _, _ ->
                Log.d(tag,"Not logging out")
            }
            .show()
    }

    private fun performLogOut(){
        FirebaseAuth.getInstance().signOut()
        Log.d(tag,"logging out")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    //check if this is user's first login, else get user's username from database
    private fun firstLoginCheck(){
        firestoreUserInfo?.get()?.addOnSuccessListener {document ->
            if(document != null){
               if(document.get("Username") == null ){
                   promptUsername()

               }else {
                   getFireStoreUsername(document)
               }
            }
        }
    }

    //get username and achievement stats of existing player
    private fun getFireStoreUsername(document:DocumentSnapshot){

        username = document.get("Username").toString()
        walletDate = document.get("walletDate").toString()
        coinCollectedToday = document.get("Collect # coins in a day tracker").toString().toInt()
        coinCollectedTodayAchievement = document.get("Collect # coins in a day").toString().toInt()
        totalCoinCollected = document.get("Collect # coins").toString().toInt()
        coinWithDoubleVal = document.get("Coin with double value").toString().toInt()
        loginStreak = document.get("Login streak").toString().toInt()
        booster4Quantity = document.get("Booster 4").toString().toInt()

        firestoreUserWallet = firestore?.collection("Users")?.document(username!!)
        Log.d(tag, "logged in user's username is $username")

        //need username to check if booster is active
        checkBoosterActive()

        //getMapAsync is in firstLoginCheck because
        //I need to know what the username is to load the correct wallet
        //and to exclude adding the corresponding coins on the map
        mapView?.getMapAsync(this)
    }

    //prompt new player to write their username
    private fun promptUsername(){
        firestoreUsernames?.get()?.addOnSuccessListener { document ->
            if (document != null) {

                var usernameTaken = false
                var usernameInvalid = false

                //the username is all CAPS
                val usernameEditText = EditText(this)
                usernameEditText.filters = arrayOf(InputFilter.AllCaps() ,InputFilter.LengthFilter(16))
                usernameEditText.hint = "Enter username"
                dialogUsername = AlertDialog.Builder(this)
                    .setTitle("Add username")
                    .setMessage("Username must be alphanumeric and be 16 characters or less")
                    .setView(usernameEditText)
                    .setCancelable(false)
                    .setPositiveButton("Confirm") { _, _ ->
                        val usernameStr = usernameEditText.text.toString()

                        for(i in 1..document.data?.size!!){

                            if(usernameStr == document.get("user$i")){
                                usernameTaken = true
                            }
                        }
                        //check if username is alphanumeric and less than 16 characters
                        if(!usernameStr.matches(Regex("[A-Za-z0-9]+")) || usernameStr.length > 16){
                            usernameInvalid = true
                        }
                        //username is not valid
                        if(usernameTaken || usernameInvalid){
                            Toast.makeText(this,"username taken or invalid",Toast.LENGTH_SHORT).show()
                            Log.d(tag,"username taken or invalid")
                            promptUsername()

                        }else{
                            //add username to the username database
                            firestoreUsernames?.update("user"+(document.data?.size!! + 1).toString(),usernameStr)

                            //create a link from the account UID to the username and initialise all data
                            val usernameData = HashMap<String, Any?>()
                            usernameData["Username"] = usernameStr
                            usernameData["walletDate"] = ""
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
                            usernameData["Coin with double value"] = 0
                            usernameData["Booster active until"] = ""

                            firestoreUserInfo?.set(usernameData)
                            username = usernameStr

                            //create user wallet document
                            val emptyData = HashMap<String, Any?>()
                            firestoreUserWallet = firestore?.collection("Users")?.document(username!!)
                            firestoreUserWallet?.set(emptyData)

                            mapView?.getMapAsync(this)
                            Toast.makeText(this,"username registered",Toast.LENGTH_SHORT).show()
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


    private fun checkBoosterActive(){
        firestoreUserInfo?.get()?.addOnSuccessListener {document->
            if(document!= null) {
                val boosterTimeUntil = document["Booster active until"].toString()
                //check that it is not a newly created account
                if (boosterTimeUntil != "") {
                    val now = Calendar.getInstance().time
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    val currentTime = dateFormat.format(now)
                    val currentTime2 = dateFormat.parse(currentTime)

                    val boosterTimeUntil2 = dateFormat.parse(boosterTimeUntil)
                    val timeDiff = (boosterTimeUntil2.time - currentTime2.time)
                    if(timeDiff>0){
                        startBoosterTimer(timeDiff)
                    }
                }
            }
        }
    }

    private fun startBoosterTimer(millisInFuture:Long){
        boosterActive = true
        val timeFormat = "%02d:%02d"

        boosterTimer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                booster_timer_text.text = ("Booster active for \n"+String.format
                (timeFormat,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) ,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                )
                        )
            }
            override fun onFinish(){
                Toast.makeText(applicationContext,"Booster has expired",Toast.LENGTH_LONG).show()
                boosterActive = false
                booster_timer_text.text = ""
            }
        }.start()
    }



    //counts down to midnight
    private fun midnightCountDown(){
        val now = Calendar.getInstance().time
        val millisInDay = 86400000
        val millisTillMidnight = millisInDay - (now.time % millisInDay)


        val timeFormat = "%02d:%02d:%02d"
        val timeTillMidnight = (""+String.format
            (timeFormat,
                TimeUnit.MILLISECONDS.toHours(millisTillMidnight),
                TimeUnit.MILLISECONDS.toMinutes(millisTillMidnight) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millisTillMidnight)),
                TimeUnit.MILLISECONDS.toSeconds(millisTillMidnight) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millisTillMidnight))
            )
        )

        Log.d(tag,"there is $timeTillMidnight till midnight")

        midnightTimer = object : CountDownTimer(millisTillMidnight, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }
            override fun onFinish(){
                midnightDialog()

            }
        }.start()
    }

    //restarts activity at 12am
    private fun midnightDialog(){
        midnightDialog = AlertDialog.Builder(this)
                .setTitle("It is 12am now")
                .setMessage("The app will restart to load today's new map")
                .setCancelable(false)
                .setPositiveButton("Confirm") { _, _ ->
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    overridePendingTransition(0, 0)
                }.show()
    }

    //cancel timer
    private fun cancelTimer() {
        if(boosterTimer!=null) {
            booster_timer_text.text = ""
            boosterActive = false
            boosterTimer?.cancel()
        }
        if(midnightTimer!=null) {
            midnightTimer?.cancel()
        }
    }

    private fun cancelDialog(){
        if (dialogUsername != null) {
            dialogUsername?.dismiss()
            dialogUsername = null
        }
        if(dialogLogOut != null){
            dialogLogOut?.dismiss()
            dialogLogOut = null
        }
        if(midnightDialog != null){
            midnightDialog?.dismiss()
            midnightDialog = null
        }
        if(dialogInternet != null){
            dialogInternet?.dismiss()
            dialogInternet = null
        }
    }
    @SuppressWarnings("MissingPermission")
    override fun onStart(){
        super.onStart()
        mapView?.onStart()

        if (locationEngine != null) {

            try {
                locationEngine?.requestLocationUpdates()
            } catch (ignored: SecurityException) {
            }

            locationEngine?.addLocationEngineListener(this)
        }

    }

    override fun onResume()
    {
        super.onResume()
        mapView?.onResume()

        //used when switching activities
        if(username != ""){
            checkBoosterActive()
        }
        //countdown to midnight
        midnightCountDown()

        //check if there is internet connection
        internetDialog(false)

        //reset button colors when coming back to this activity
        bank_button.setBackgroundColor(android.R.drawable.btn_default)
        my_profile_button.setBackgroundColor(android.R.drawable.btn_default)
        shop_button.setBackgroundColor(android.R.drawable.btn_default)
        log_out_button.setBackgroundColor(android.R.drawable.btn_default)
    }


    override fun onPause()
    {
        super.onPause()
        mapView?.onPause()
        //if home or back button is pressed and there are coins that needs to be updated
        if(mAuth?.currentUser != null && coinIndexForWallet != 1 && !switchToAnotherActivity) {
            updateUserInfo{}
        }else{
            //reset this value
            switchToAnotherActivity = false
        }
        cancelDialog()

    }

    override fun onStop()
    {

        super.onStop()
        mapView?.onStop()

        if(locationEngine != null){
            locationEngine?.removeLocationEngineListener(this)
            locationEngine?.removeLocationUpdates()
            locationLayerPlugin.onStop()
        }
        storeDownloadDate()
        cancelTimer()

    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        if(locationEngine != null) {
            locationEngine?.deactivate()
        }
        cancelDialog()

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

            map?.setOnMarkerClickListener(this)
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
        locationEngine?.apply {
            interval = 5000 // preferably every 5 seconds
            fastestInterval = 1000 // at most every second
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }
        val lastLocation = locationEngine?.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        } else {
            locationEngine?.addLocationEngineListener(this)
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
                lifecycle.addObserver(locationLayerPlugin)
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
            setCameraPosition(originLocation!!)
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        Log.d(tag, "[onConnected] requesting location updates")
        locationEngine?.requestLocationUpdates()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onExplanationNeeded(permissionsToExplain : MutableList<String>?) {
        Log.d(tag, "Permissions: $permissionsToExplain")
        // Present popup message or dialog
        Toast.makeText(this,"Coinz needs location access to be playable",Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(tag, "[onPermissionResult] granted == $granted")
        if (granted) {
            enableLocation()
        } else {
            // Open a dialogue with the user
            Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    override fun onMarkerClick(coin: Marker): Boolean {
        //add coin to wallet and remove coin from map
        val coinLocation = Location("")
        coinLocation.latitude = coin.position.latitude
        coinLocation.longitude = coin.position.longitude
        if(originLocation != null) {
            val distance = originLocation?.distanceTo(coinLocation)!!

            if (distance <= 25) {
                //if(distance <= 10000){//for testing
                Toast.makeText(this, "coin collected", Toast.LENGTH_SHORT).show()

                val date = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("yyyy/MM/dd")
                val currentDate = dateFormat.format(date)


                //if this is the first collected coin, then update the userInfo's
                //walletDate with the current date
                //check login streak when collecting the first coin of the day as well
                if (DownloadCompleteRunner.numberOfCoinsinWallet == 0 && walletData.size == 0) {
                    //check that this is not a newly created account before calculating date difference
                    if (walletDate != "") {
                        //perform date calculation to update login streak
                        val walletDate2 = dateFormat.parse(walletDate)
                        val currentDate2 = dateFormat.parse(currentDate)
                        val dateDiff = (currentDate2.time - walletDate2.time) / (1000 * 60 * 60 * 24)

                        //login streak increases
                        //login streak rewards are given here
                        if (dateDiff.toInt() == 1) {
                            if (loginStreak in 0..5) {
                                loginStreak++
                                coinWithDoubleVal = loginStreak
                                Toast.makeText(this, "Login streak is $loginStreak, " +
                                        "this coin and the next ${coinWithDoubleVal - 1} coin(s) you collect will have doubled value", Toast.LENGTH_LONG).show()
                            } else {
                                //day 7 gives a booster
                                booster4Quantity++
                                firestoreUserInfo?.update("Booster 4", booster4Quantity)
                                Toast.makeText(this, "You gained a booster 4 for achieving 7 day login streak", Toast.LENGTH_LONG).show()
                                //reset login streak at day 7
                                loginStreak = 0
                            }

                        }//no consecutive login, login streak drops back to 1
                        else {
                            Toast.makeText(this, "Login streak is 1, the coin you just collected has doubled value", Toast.LENGTH_LONG).show()
                            loginStreak = 1
                            coinWithDoubleVal = 1
                        }
                    } else {
                        //user's first login to an account
                        Toast.makeText(this, "Welcome to the game!", Toast.LENGTH_LONG).show()
                        loginStreak = 1
                        coinWithDoubleVal = 1

                    }
                    firestoreUserInfo?.update("Login streak", loginStreak)
                    firestoreUserInfo?.update("walletDate", currentDate)
                    walletDate = currentDate
                }

                //collected coins are stored in a hash map
                val coinData = HashMap<String, Any>()
                val valueCurrency = coin.snippet.split(" ")
                coinData["id"] = coin.title

                var coinValue = valueCurrency[0].toDouble()
                Log.d(tag, "coin value before multiplier = $coinValue")
                //login bonus
                if (coinWithDoubleVal > 0) {
                    coinValue *= 2
                    coinWithDoubleVal--
                }
                //booster
                if (boosterActive) {
                    coinValue *= 1.5
                }
                Log.d(tag, "coin value after multipler = $coinValue")

                coinData["value"] = coinValue
                coinData["currency"] = valueCurrency[1]
                coinData["bankedIn"] = false
                coinData["coinGivenToOthers"] = false
                coinData["coinGivenByOthers"] = false
                coinData["coinGiverName"] = ""

                walletData["coin${(DownloadCompleteRunner.numberOfCoinsinWallet + coinIndexForWallet)}"] = coinData
                coinIndexForWallet++
                Log.d(tag, "collected coin's data is " + coinData.toString())
                map?.removeMarker(coin)


            } else {
                Toast.makeText(this, "you are " + Math.round(distance).toString()
                        + "metres away from the coin, get within 25metres to collect the coin", Toast.LENGTH_SHORT).show()
                Log.d(tag, "coin too far away")
            }
        }else{
            Toast.makeText(this,"turn on location service to collect the coin",Toast.LENGTH_LONG).show()
        }
        return true
    }


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
        Log.d(tag,"Recalled lastDownloadDate is $downloadDate")

        if (downloadDate != currentDate){
            //if dates are diff then download map from server, write geojson file to device and
            //add coins to map/empty wallet in onPostExecute,update downloadDate value
            val asyncDownload = DownloadFileTask(DownloadCompleteRunner,map,firestoreUserWallet,walletDate,currentDate,WeakReference(applicationContext))
            asyncDownload.execute("http://homepages.inf.ed.ac.uk/stg/coinz/$currentDate/coinzmap.geojson")
            downloadDate = currentDate

        }else{
            //if map already downloaded, load wallet to device, add coins to the map that aren't in the wallet
            if(walletDate != currentDate){
                DownloadCompleteRunner.emptyWallet(firestoreUserWallet)
                DownloadCompleteRunner.addCoinstoMap(map,applicationContext)

            }else{
                DownloadCompleteRunner.loadWalletAddCoins(firestoreUserWallet,map,applicationContext)
            }
        }
    }




    object DownloadCompleteRunner : DownloadCompleteListener {

        var result : String? = null
        private var walletCoinId = ArrayList<String>()               //initialised by loadWallettoDevice(), store wallet's coins' id

        var numberOfCoinsinWallet = 0                                //initialised by loadWallettoDevice(),modified during updateUserInfo()

        var shil : Double = 0.0                                      //initialised by addCoinsToMap()
        var dolr : Double = 0.0
        var quid : Double = 0.0
        var peny : Double = 0.0

        override fun downloadComplete(result: String) {
            this.result = result
        }
        override fun emptyWallet(firestoreUserWallet: DocumentReference?){
            val emptyData = HashMap<String, Any?>()
            firestoreUserWallet?.set(emptyData)
            //reset these 2 fields every time this method is called to flush out old data
            walletCoinId = ArrayList()
            numberOfCoinsinWallet = 0
        }
        //load wallet to device and addCoinstoMap at onSuccess
        override fun loadWalletAddCoins(firestoreUserWallet: DocumentReference?,map:MapboxMap?,context: Context){
            //reset this array every time this method is called
            walletCoinId = ArrayList()

            firestoreUserWallet?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null) {
                        numberOfCoinsinWallet = document.data?.size!!
                        if(numberOfCoinsinWallet > 0) {
                            for (i in 1..(document.data?.size!!)) {
                                walletCoinId.add(document["coin$i.id"].toString())
                            }
                        }
                        addCoinstoMap(map,context)
                    }else {
                        Log.d("MapActivity", "No such document")
                    }
                }
                ?.addOnFailureListener { exception ->
                    Log.e("MapActivity", "get user wallet failed with ", exception)
                }

        }
        override fun addCoinstoMap(map:MapboxMap?,context:Context){
            val coinzMap = File(context.filesDir, "coinzmap.geojson")
            val mapContents = coinzMap.readText()
            result = mapContents
            val fc = FeatureCollection.fromJson(mapContents)

            //initialise rates
            val rates = JSONObject(mapContents).getJSONObject("rates")
            shil = rates.getDouble("SHIL")
            dolr = rates.getDouble("DOLR")
            quid = rates.getDouble("QUID")
            peny = rates.getDouble("PENY")


            var coinOnMapNumber = 0
            for(feature in fc.features().orEmpty()) {
                val coordinates = (feature.geometry() as Point).coordinates()
                val coinId = feature?.properties()?.get("id").toString()
                val coinValueCurrency = feature?.properties()?.get("value").toString() + " " +  feature?.properties()?.get("currency") .toString()
                val coinSymbol = feature?.properties()?.get("marker-symbol").toString()
                val coinColor = feature?.properties()?.get("marker-color").toString()

                //remove "" from the string
                val id = coinId.replace("\"","")
                val valueCurrency = coinValueCurrency.replace("\"","")
                val symbol = coinSymbol.replace("\"","").toInt()
                val color = coinColor.replace("\"","").replace("#","")
                val colorVal = Integer.parseInt(color,16)


                //check if this coin is already collected before adding it to the map
                var coinNotOnMap = true
                //if collectedCoins id and marker id not the same then add marker to map
                if(numberOfCoinsinWallet != 0) {
                    for (i in 0..(numberOfCoinsinWallet - 1)) {
                        if (walletCoinId[i] == id) {
                            coinNotOnMap = false
                        }
                    }
                }
                if(coinNotOnMap || numberOfCoinsinWallet == 0) {
                    //set up marker icons
                    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
                        val drawable = ContextCompat.getDrawable(context, drawableId)

                        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
                                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)

                        return bitmap
                    }
                    //set up icon colors
                    fun tintImage(background: Bitmap,number:Bitmap, color: Int): Bitmap {
                        val backgroundPaint = Paint()
                        backgroundPaint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
                        val bitmapResult = Bitmap.createBitmap(background.width, background.height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmapResult)
                        canvas.drawBitmap(background, 0f, 0f, backgroundPaint)


                        val numberPaint = Paint()
                        if(color == Color.YELLOW || color == Color.GREEN){
                            numberPaint.colorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
                        }else{
                            numberPaint.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        }
                        canvas.drawBitmap(number,0f,0f,numberPaint)


                        return bitmapResult
                    }


                    var markerNumber:Int = R.drawable.marker_1
                    when (symbol) {
                        2 -> markerNumber = R.drawable.marker_2
                        3 -> markerNumber = R.drawable.marker_3
                        4 -> markerNumber = R.drawable.marker_4
                        5 -> markerNumber = R.drawable.marker_5
                        6 -> markerNumber = R.drawable.marker_6
                        7 -> markerNumber = R.drawable.marker_7
                        8 -> markerNumber = R.drawable.marker_8
                        9 -> markerNumber = R.drawable.marker_9
                        10 -> markerNumber = R.drawable.marker_10
                    }
                    var backgroundColor = 0
                    //red       = PENY
                    //green     = DOLR
                    //yellow    = QUID
                    //blue      = SHIL
                    when(colorVal){
                        255 -> backgroundColor = Color.BLUE
                        32768 -> backgroundColor = Color.GREEN
                        16711680 -> backgroundColor = Color.RED
                        16768768 -> backgroundColor = Color.YELLOW
                    }

                    val iconFactory = IconFactory.getInstance(context)
                    val background = getBitmapFromVectorDrawable(context,R.drawable.marker_background)
                    val number = getBitmapFromVectorDrawable(context,markerNumber)

                    val marker = tintImage(background,number,backgroundColor)
                    val icon = iconFactory.fromBitmap(marker)



                    //add coins to map
                    map?.addMarker(MarkerOptions()
                            .title(id)
                            .snippet(valueCurrency)
                            .icon(icon)
                            .position(LatLng(coordinates[1], coordinates[0])))

                    //count the number of coins added to the map for debugging
                    coinOnMapNumber++
                }

            }
            //total sums up to 50, assuming no gifted coins in wallet
            Log.d("MapActivity","coinsOnMap = " + coinOnMapNumber.toString()
                    + ", coinsInWallet "  + numberOfCoinsinWallet.toString())
        }

    }
    class DownloadFileTask(private val caller : DownloadCompleteListener, private val map:MapboxMap?,
                           private val firestoreUserWallet: DocumentReference?, private val walletDate:String,
                           private val currentDate:String, private val context: WeakReference<Context>) : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg urls: String): String =
                try {
                    loadFileFromNetwork(urls[0])
                } catch (e: IOException) {
                    Log.e("MapActivity","Unable to load content. Check your network connection")
                    ""
                }


        private fun loadFileFromNetwork(urlString: String): String {
            val stream: InputStream = downloadUrl(urlString)
            // Read input from stream, build result as a string
            val s = Scanner(stream).useDelimiter("\\A")
            val result = if (s.hasNext()) s.next() else ""
            s.close()
            return result
        }



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
            //if there is internet connection, then downloaded geojson map is valid, so add it to internal storage
            if(result.isNotEmpty()) {
                caller.downloadComplete(result)

                //write downloaded json file to device
                val file = File("/data/data/com.example.user.coinz/files", "coinzmap.geojson")
                file.writeText(DownloadCompleteRunner.result!!)

                //before adding coins to the map, check if the wallet's coins are collected today or not
                //if collected today, load the wallet to device, else empty the wallet as coins only last
                //until the end of the day
                if (walletDate != currentDate) {
                    caller.emptyWallet(firestoreUserWallet)
                    caller.addCoinstoMap(map, context.get()!!)
                } else {
                    caller.loadWalletAddCoins(firestoreUserWallet, map, context.get()!!)
                }
            }else{
                Log.e("MapActivity","geojson map download failed")
            }

        }

    } // end class DownloadFileTask



    //check if there is internet connection
    internal class InternetCheck(private val onInternetChecked: (Boolean) -> Unit) :
            AsyncTask<Void, Void, Boolean>() {
        init {
            execute()
        }

        override fun doInBackground(vararg voids: Void): Boolean {
            return try {
                val sock = Socket()
                sock.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                sock.close()
                true
            } catch (e: IOException) {
                false
            }

        }

        override fun onPostExecute(internet: Boolean) {
            onInternetChecked(internet)

        }
    }

    //show dialog when there is no internet, used when UI buttons are pressed
    private fun internetDialog(reconnect:Boolean){
        InternetCheck{internet->
            Log.d("Connection", "Is connection enabled? $internet")
            if(!internet){
                dialogInternet = AlertDialog.Builder(this)
                        .setTitle("No internet connection, your progress was saved")
                        .setMessage("Please turn on internet connection before proceeding")
                        .setCancelable(false)
                        .setPositiveButton("OK") { _, _ ->

                            internetDialog(true)

                        }.setNegativeButton("Quit game"){ _, _ ->
                            Log.d(tag,"no internet connection -> game exits")
                            finish()

                        }
                        .show()
            }else{
                if(reconnect){
                    Toast.makeText(this,"connection successful",Toast.LENGTH_SHORT).show()

                    if(DownloadCompleteRunner.result.isNullOrEmpty()){
                        //redownload map if geojson map download failed
                        downloadJSONMapandAddCoinsToMap(map)
                    }

                }

            }
        }

    }

}
