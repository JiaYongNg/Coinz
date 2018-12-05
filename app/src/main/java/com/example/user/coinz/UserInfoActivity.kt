package com.example.user.coinz

import android.app.Dialog
import android.content.res.ColorStateList
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
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
        ImageViewCompat.setImageTintList(coin_getter_medal, ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.Gold)))

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
