package com.example.user.coinz

import android.app.Dialog
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_shop.*
import android.text.InputFilter



class ShopActivity : AppCompatActivity() {

    private val tag = "BankActivity"
    private var dialogBuyBooster : Dialog? = null
    private var dialogInternet : Dialog? = null

    private var username = ""
    private var accountId = ""
    private var selectedBoosterNumber = 0

    private var firestore: FirebaseFirestore? = null
    private var firestoreUserInfo: DocumentReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)
        firestore = FirebaseFirestore.getInstance()
        username = intent.getStringExtra("username")
        accountId = intent.getStringExtra("accountId")
        firestoreUserInfo = firestore?.collection("Users")?.document(accountId)

        buy_button.setOnClickListener {
            if(selectedBoosterNumber != 0){
                internetDialog(false){ buyBooster()}
            }else{
                Toast.makeText(this,"Please select a booster to buy",Toast.LENGTH_SHORT).show()
            }
        }

        booster_1_image.setOnClickListener { selectBooster(1) }
        booster_2_image.setOnClickListener { selectBooster(2) }
        booster_3_image.setOnClickListener { selectBooster(3) }
        booster_4_image.setOnClickListener { selectBooster(4) }
    }


    override fun onDestroy() {
        super.onDestroy()
        if(dialogBuyBooster != null){
            dialogBuyBooster?.dismiss()
            dialogBuyBooster = null
        }
        if(dialogInternet != null){
            dialogInternet?.dismiss()
            dialogInternet = null
        }
    }

    private fun selectBooster(boosterNum:Int){
        booster_1_image.setBackgroundColor(Color.WHITE)
        booster_2_image.setBackgroundColor(Color.WHITE)
        booster_3_image.setBackgroundColor(Color.WHITE)
        booster_4_image.setBackgroundColor(Color.WHITE)

        when (boosterNum) {
            1 -> booster_1_image.setBackgroundColor(Color.GREEN)
            2 -> booster_2_image.setBackgroundColor(Color.GREEN)
            3 -> booster_3_image.setBackgroundColor(Color.GREEN)
            4 -> booster_4_image.setBackgroundColor(Color.GREEN)
        }
        selectedBoosterNumber = boosterNum

    }

    private fun buyBooster(){
        firestoreUserInfo?.get()?.addOnSuccessListener { document ->
            if (document != null) {
                val netWorth = document["Net worth"].toString().toDouble()
                val costOfBooster = when (selectedBoosterNumber) {
                    1 -> 200
                    2 -> 380
                    3 -> 540
                    else -> 680
                }

                //the quantity is an int
                val quantityEditText = EditText(this)
                quantityEditText.inputType = InputType.TYPE_CLASS_NUMBER
                quantityEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(4))
                quantityEditText.hint = "Enter quantity here"

                dialogBuyBooster = AlertDialog.Builder(this)
                        .setTitle("How many $costOfBooster gold booster $selectedBoosterNumber do you wish to buy")
                        .setMessage("You currently have ${Math.round(netWorth)} gold")
                        .setView(quantityEditText)
                        .setPositiveButton("Confirm") { _, _ ->

                            if (quantityEditText.text.isNotEmpty()){
                                val quantity = quantityEditText.text.toString().toInt()


                                val totalCost = quantity * costOfBooster
                                //purchase successful
                                //update booster quantity in inventory and reduce net worth
                                if (totalCost <= netWorth) {
                                    val storageQuantity = document["Booster $selectedBoosterNumber"].toString().toInt()

                                    firestoreUserInfo?.update("Net worth", (netWorth - totalCost))
                                            ?.addOnSuccessListener {
                                                firestoreUserInfo?.update("Booster $selectedBoosterNumber", (storageQuantity + quantity))
                                                Toast.makeText(this, "Your purchase of $quantity booster $selectedBoosterNumber is successful",
                                                        Toast.LENGTH_LONG).show()
                                            }

                                } else {
                                    //purchase failed
                                    Toast.makeText(this, "Purchase failed,you were short of " +
                                            "${Math.round(totalCost - netWorth)} gold to make the purchase", Toast.LENGTH_LONG).show()
                                }
                            }else{
                                Log.d(tag, "quantity field is empty")
                            }
                        }.show()

            } else {
                //the app should never come here
                Log.e(tag, "No such document")
            }
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
