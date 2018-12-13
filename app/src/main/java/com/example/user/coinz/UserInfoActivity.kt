package com.example.user.coinz

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_info.*
import java.text.SimpleDateFormat
import java.util.Calendar

class UserInfoActivity : AppCompatActivity() {

    private val tag = "UserInfoActivity"
    private var dialogActiveBooster : Dialog? = null
    private var dialogInactiveBooster : Dialog? = null
    private var dialogInternet : Dialog? = null

    private var username = ""
    private var accountId = ""

    @SuppressLint("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    private var firestore: FirebaseFirestore? = null
    private var firestoreUserInfo: DocumentReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        firestore = FirebaseFirestore.getInstance()
        username = intent.getStringExtra("username")
        accountId = intent.getStringExtra("accountId")
        firestoreUserInfo = firestore?.collection("Users")?.document(accountId)

        val displayText = "tap medals to get more info on them\nInventory\nuse boosters to increase collected coin's value by 1.5x for a limited time"
        explanation_text.text = displayText
        getUserInfo()


    }


    override fun onDestroy() {
        super.onDestroy()
        if(dialogActiveBooster!= null){
            dialogActiveBooster?.dismiss()
            dialogActiveBooster = null
        }
        if(dialogInactiveBooster!= null){
            dialogInactiveBooster?.dismiss()
            dialogInactiveBooster = null
        }
        if(dialogInternet!= null){
            dialogInternet?.dismiss()
            dialogInternet = null
        }
    }

    private fun getUserInfo(){

        firestoreUserInfo?.get()?.addOnSuccessListener {document->
            if(document != null){
                Username_text.text = username

                val netWorth = Math.round(document["Net worth"].toString().toDouble()).toInt()
                val coinGiver = document["Coin giver"].toString().toInt()
                val coinGetter = document["Coin getter"].toString().toInt()
                val totalCoinCollected = document["Collect # coins"].toString().toInt()
                val coinCollectedToday = document["Collect # coins in a day"].toString().toInt()

                //achievements
                setAchievementProperty(net_worth_medal,"Net Worth",
                        net_worth_text,netWorth,10000,50000,100000)

                setAchievementProperty(coin_giver_medal,"Give coins",
                        coin_giver_text,coinGiver,100,500,1000)

                setAchievementProperty(coin_getter_medal,"Bank in coins given by other players",
                        coin_getter_text,coinGetter,100,500,1000)

                setAchievementProperty(coin_collector_medal,"Collect coins on the map",
                        coin_collector_text,totalCoinCollected,100,500,1000)

                setAchievementProperty(single_day_coin_collector_medal,"Collect coins on the map in a single day",
                        single_day_coin_collector_text,coinCollectedToday,10,25,50)

                //boosters in inventory
                val booster1Text = "x${document["Booster 1"]}"
                val booster2Text = "x${document["Booster 2"]}"
                val booster3Text = "x${document["Booster 3"]}"
                val booster4Text = "x${document["Booster 4"]}"
                booster_1_text.text = booster1Text
                booster_2_text.text = booster2Text
                booster_3_text.text = booster3Text
                booster_4_text.text = booster4Text

                val boosterActiveUntil = document["Booster active until"].toString()
                setBoosterPorperty(booster_1_image, boosterActiveUntil,document["Booster 1"].toString().toInt(),1)
                setBoosterPorperty(booster_2_image, boosterActiveUntil,document["Booster 2"].toString().toInt(),2)
                setBoosterPorperty(booster_3_image, boosterActiveUntil,document["Booster 3"].toString().toInt(),3)
                setBoosterPorperty(booster_4_image, boosterActiveUntil,document["Booster 4"].toString().toInt(),4)
            }
        }
    }

    //sets up the text and medal colour of the achievements
    private fun setAchievementProperty(medalImage: ImageView,medalInfo:String,achievementText:TextView,
                                       achievementVal:Int,bronzeVal:Int,silverVal: Int,goldVal: Int){

        medalImage.setOnClickListener { view->
            Snackbar.make(view, medalInfo, Snackbar.LENGTH_LONG).show()
        }

        when {
            achievementVal >= goldVal -> {
                ImageViewCompat.setImageTintList(medalImage,
                        ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.Gold)))

                achievementText.text = achievementVal.toString()
            }

            achievementVal >= silverVal -> {
                ImageViewCompat.setImageTintList(medalImage,
                        ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.Silver)))

                val displayText = "$achievementVal/$goldVal"
                achievementText.text = displayText
            }

            achievementVal >= bronzeVal -> {
                ImageViewCompat.setImageTintList(medalImage,
                        ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.Bronze)))

                val displayText = "$achievementVal/$silverVal"
                achievementText.text = displayText
            }

            else-> {
                //medal remains black
                val displayText = "$achievementVal/$bronzeVal"
                achievementText.text = displayText
            }
        }

    }



    private fun setBoosterPorperty(boosterImage:ImageView, boosterActiveUntil:String,boosterQuantity:Int,boosterNum:Int){
        //onclick check if there is booster in inventory
        //check if there is active boost and ask if player wants to override it
        //yes, then reduce the amount of booster
        //calculate boosterActiveUntil and upload it to firestore, onComplete switch to Mapactivity
        //boosterActiveUntil will be used in the MapActivity to check if booster is active

        boosterImage.setOnClickListener {view->
            internetDialog(false){checkBoosterDialog(boosterActiveUntil,boosterQuantity,boosterNum,view)}

        }

    }
    private fun checkBoosterDialog(boosterActiveUntil:String,boosterQuantity:Int,boosterNum:Int,view: View){
        if(boosterQuantity>0) {
            val timeDiff =
                    //not a newly created account
                    if (boosterActiveUntil != "") {
                        //check if there is a booster active
                        val now = Calendar.getInstance().time
                        val currentTime = dateFormat.format(now)

                        val boosterTimeUntil2 = dateFormat.parse(boosterActiveUntil)
                        val currentTime2 = dateFormat.parse(currentTime)
                        (boosterTimeUntil2.time - currentTime2.time)
                    } else {
                        0
                    }

            if (timeDiff > 0) {
                //dialog there is a booster active, are you sure you want to override
                dialogActiveBooster = AlertDialog.Builder(this)
                        .setTitle("There is an active booster")
                        .setMessage("Are you sure you want to override the duration of that booster with booster $boosterNum?")
                        .setPositiveButton("OK") { _, _ ->

                            activateBooster(boosterNum,boosterQuantity)

                        }.setNegativeButton("NO"){ _, _ ->

                        }
                        .show()

            }else{
                //dialog: are you sure you want to use the booster?
                dialogInactiveBooster = AlertDialog.Builder(this)
                        .setTitle("Activate booster")
                        .setMessage("Are you sure you want to use booster $boosterNum now?")
                        .setPositiveButton("OK") { _, _ ->

                            activateBooster(boosterNum,boosterQuantity)

                        }.setNegativeButton("NO"){ _, _ ->

                        }
                        .show()
            }
        }else{
            Snackbar.make(view, "You do not have any booster $boosterNum", Snackbar.LENGTH_SHORT).show()
        }
    }
    private fun activateBooster(boosterNum: Int,boosterQuantity: Int){
        //get the time now again as this time is calculated from onClick of positive button
        val now = Calendar.getInstance().time
        val currentTime = dateFormat.format(now)
        val currentTime2 = dateFormat.parse(currentTime)

        //add the current time and duration of booster to get "Booster active until" time
        val boosterActiveTime = dateFormat.format(currentTime2.time + boosterNum * 300000)

        val boosterData = HashMap<String, Any?>()
        boosterData["Booster $boosterNum"] = (boosterQuantity-1)
        boosterData["Booster active until"] = boosterActiveTime

        firestoreUserInfo?.update(boosterData)?.addOnSuccessListener {
            Log.d(tag,"Booster $boosterNum used and is active until $boosterActiveTime")
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //show dialog when there is no internet,used when UI buttons are pressed
    private fun internetDialog(reconnect:Boolean,buttonMethod:()->Unit){
        MapActivity.InternetCheck { internet ->
            Log.d("Connection", "Is connection enabled? $internet")
            if (!internet) {
                dialogInternet = AlertDialog.Builder(this)
                        .setTitle("No internet connection, your action was NOT saved")
                        .setMessage("Please turn on internet connection before proceeding")
                        .setCancelable(false)
                        .setPositiveButton("OK") { _, _ ->

                            internetDialog(true,buttonMethod)

                        }.setNegativeButton("Quit game") { _, _ ->
                            Log.d(tag, "no internet connection -> game exits")
                            finishAffinity()

                        }
                        .show()
            }else{
                if(reconnect){
                    //restart the activity if this is a successful reconnection attempt
                    Toast.makeText(this, "connection successful", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                    overridePendingTransition(0, 0)
                }else{
                    buttonMethod()
                }
            }
        }

    }

}
