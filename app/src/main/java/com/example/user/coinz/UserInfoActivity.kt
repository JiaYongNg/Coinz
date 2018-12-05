package com.example.user.coinz

import android.app.Dialog
import android.content.res.ColorStateList
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_info.*

class UserInfoActivity : AppCompatActivity() {

    private val tag = "BankActivity"
    private var dialogBuyBooster : Dialog? = null

    private var username = ""
    private var accountId = ""

    private var firestore: FirebaseFirestore? = null
    private var firestoreUserInfo: DocumentReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        firestore = FirebaseFirestore.getInstance()
        username = intent.getStringExtra("username")
        accountId = intent.getStringExtra("accountId")
        firestoreUserInfo = firestore?.collection("Users")?.document(accountId)
        //booster image onclick
        //settooltip
        getUserInfo()


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
                setAchievementProperty(net_worth_medal,net_worth_text,netWorth,10000,50000,100000)
                setAchievementProperty(coin_giver_medal,coin_giver_text,coinGiver,100,500,1000)
                setAchievementProperty(coin_getter_medal,coin_getter_text,coinGetter,100,500,1000)
                setAchievementProperty(coin_collector_medal,coin_collector_text,totalCoinCollected,100,500,1000)
                setAchievementProperty(single_day_coin_collector_medal,single_day_coin_collector_text,coinCollectedToday,10,25,50)

                //boosters in inventory
                val booster1Text = "x${document["Booster 1"]}"
                val booster2Text = "x${document["Booster 2"]}"
                val booster3Text = "x${document["Booster 3"]}"
                val booster4Text = "x${document["Booster 4"]}"
                booster_1_text.text = booster1Text
                booster_2_text.text = booster2Text
                booster_3_text.text = booster3Text
                booster_4_text.text = booster4Text

            }

        }
    }
    //sets up the text and medal colour of the achiements
    private fun setAchievementProperty(medalImage: ImageView,achievementText:TextView,
                                       achievementVal:Int,bronzeVal:Int,silverVal: Int,goldVal: Int){
        when {
            achievementVal >= goldVal -> {
                ImageViewCompat.setImageTintList(medalImage, ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.Gold)))
                achievementText.text = achievementVal.toString()
            }

            achievementVal >= silverVal -> {
                ImageViewCompat.setImageTintList(medalImage, ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.Silver)))
                val displayText = "$achievementVal/$goldVal"
                achievementText.text = displayText
            }

            achievementVal >= bronzeVal -> {
                ImageViewCompat.setImageTintList(medalImage, ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.Bronze)))
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
    //username
    //achievement
//    1. Collect # coins.
//    2. Give other people # spare change.
//    3. Get # spare change from other people.
//    4. Collect # coins in a single day.
//    5. Have a net worth of # in GOLD.
    //inventory x25 tap to use
    //4 boosters
    //254,203,50
    //189,207,211
    //238,132,72

}
