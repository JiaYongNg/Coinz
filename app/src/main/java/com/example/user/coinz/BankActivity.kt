package com.example.user.coinz

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.InputFilter
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_bank.*


class BankActivity : AppCompatActivity() {

    private val tag = "BankActivity"
    private var dialogBankIn : Dialog? = null
    private var dialogUsername : Dialog? = null
    private var dialogInternet : Dialog? = null

    private var username = ""
    private var accountId = ""

    private var netWorth = 0.0
    private var coinGiven = 0
    private var coinGotten = 0

    private var dolr = 0.0
    private var peny = 0.0
    private var quid = 0.0
    private var shil = 0.0

    private var coinList = ArrayList<CoinInfo>()
    private var collectedCoinList = ArrayList<CoinInfo>()
    private var coinGivenByOthers = ArrayList<CoinInfo>()
    private var selectedCoinList = ArrayList<CoinInfo>()
    private var numberOfBankedInCoins = 0

    private var firestore: FirebaseFirestore? = null
    private var firestoreUsernames: DocumentReference? = null
    private var firestoreUserInfo: DocumentReference? = null
    private var firestoreUserWallet: DocumentReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)

        firestore = FirebaseFirestore.getInstance()
        username = intent.getStringExtra("username")
        accountId = intent.getStringExtra("accountId")
        dolr = intent.getDoubleExtra("DOLR",0.0)
        peny = intent.getDoubleExtra("PENY",0.0)
        quid = intent.getDoubleExtra("QUID",0.0)
        shil = intent.getDoubleExtra("SHIL",0.0)
        firestoreUsernames = firestore?.collection("Users")?.document("UsernamesDatabase")
        firestoreUserInfo = firestore?.collection("Users")?.document(accountId)
        firestoreUserWallet = firestore?.collection("Users")?.document(username)


        //check if selected coins exceed the daily limit of bankable coin before allowing bank in
        bank_in_button.setOnClickListener {
            var giftedCoins = 0
            for(coins in selectedCoinList){
                if(coins.coinGiverName != ""){
                    giftedCoins++
                }
            }
            val totalNumberOfBankedInCoins = numberOfBankedInCoins + selectedCoinList.size - giftedCoins
            if(totalNumberOfBankedInCoins > 25){
                Toast.makeText(applicationContext,"Please reduce the number of selected non-gifted coins by "
                        + (totalNumberOfBankedInCoins - 25).toString(), Toast.LENGTH_SHORT).show()
            }else{
                internetDialog(false){bankIn()}
            }
        }

        give_coin_button.setOnClickListener{
            if(numberOfBankedInCoins != 25){
                Toast.makeText(applicationContext,"you need to have exactly 25 banked in coins to start gifting coins",
                        Toast.LENGTH_SHORT).show()
            }else{
                internetDialog(false){giftCoin()}
            }
        }
        //load achievement and get firestore wallet coins and display collected coins to UI
        loadAchievement()
        getCoins()

    }


    override fun onPause() {
        super.onPause()
        //update achievement
        val achievementData = java.util.HashMap<String, Any>()
        achievementData["Net worth"] = netWorth
        achievementData["Coin giver"] = coinGiven
        achievementData["Coin getter"] = coinGotten
        firestoreUserInfo?.update(achievementData)

    }



    override fun onDestroy() {
        super.onDestroy()
        if(dialogBankIn != null){
            dialogBankIn?.dismiss()
            dialogBankIn = null
        }
        if(dialogUsername != null){
            dialogUsername?.dismiss()
            dialogUsername = null
        }
        if(dialogInternet != null){
            dialogInternet?.dismiss()
            dialogInternet = null
        }
    }

    private fun loadAchievement(){
        firestoreUserInfo?.get()?.addOnSuccessListener {document->
            if(document != null){
                coinGiven = document["Coin giver"].toString().toInt()
                coinGotten = document["Coin getter"].toString().toInt()
                netWorth = document["Net worth"].toString().toDouble()
            }
        }
    }

    private fun giftCoin(){
        firestoreUsernames?.get()?.addOnSuccessListener { document ->
            if (document != null) {

                var usernameValid = false

                //the username is all CAPS
                val usernameEditText = EditText(this)
                usernameEditText.filters = arrayOf(InputFilter.AllCaps(),InputFilter.LengthFilter(16))
                usernameEditText.hint = "Enter username here"
                dialogUsername = AlertDialog.Builder(this)
                        .setTitle("Enter the recipient's username")
                        .setMessage("You are gifting ${selectedCoinList.size} coin(s)")
                        .setView(usernameEditText)
                        .setPositiveButton("Confirm")
                        { _, _ ->
                            val usernameStr = usernameEditText.text.toString()

                            for(i in 1..document.data?.size!!){

                                //no gifting to self
                                if(usernameStr == document["user$i"] && usernameStr != username){
                                    usernameValid = true
                                }
                            }

                            if(usernameValid){
                                firestore?.collection("Users")?.document(usernameStr)
                                    ?.get()?.addOnSuccessListener {document ->
                                        if(document != null){

                                            //add coin to other player's wallet
                                            val walletData = HashMap<String, Any?>()
                                            val numberOfCoins = document.data?.size!!
                                            for(i in 1..selectedCoinList.size){
                                                val coinData = HashMap<String, Any>()
                                                coinData["id"] = ""
                                                coinData["value"] = selectedCoinList[i-1].value
                                                coinData["currency"] = selectedCoinList[i-1].currency
                                                coinData["bankedIn"] = false
                                                coinData["coinGivenToOthers"] = false
                                                coinData["coinGivenByOthers"] = true
                                                coinData["coinGiverName"] = username
                                                walletData["coin${numberOfCoins + i}"] = coinData
                                            }
                                            //update the coins gifted to the other player
                                            firestore?.collection("Users")?.document(usernameStr)?.update(walletData)

                                            //set coinGivenToOthers of selected coins in own wallet to true
                                            for(i in 0..(selectedCoinList.size-1)){
                                                firestoreUserWallet?.update("coin${selectedCoinList[i].coinNumInWallet}.coinGivenToOthers",true)
                                                coinGiven++ //update achievement value
                                            }


                                            //reset selectedCoinList
                                            selectedCoinList = ArrayList()
                                            //reset recycler view
                                            coinList = ArrayList()
                                            coinGivenByOthers = ArrayList()
                                            collectedCoinList = ArrayList()
                                            numberOfBankedInCoins = 0
                                            getCoins()
                                        }

                                }
                            }else{
                                Toast.makeText(applicationContext,"Username invalid",Toast.LENGTH_SHORT).show()
                            }

                        }
                        .setNegativeButton("Cancel"){ _,_->
                        }
                        .show()

            } else {
                //the app should never come here
                Log.e(tag, "No such document")
            }
        }

    }
    private fun bankIn(){
        dialogBankIn = AlertDialog.Builder(this)
                .setTitle("Bank in")
                .setMessage("Are you sure you want to bank in the selected ${selectedCoinList.size} coin(s)?")
                .setPositiveButton("Yes")
                { _, _ ->
                    for(coin in selectedCoinList){
                        firestoreUserWallet?.update("coin${coin.coinNumInWallet}.bankedIn",true)
                                ?.addOnCompleteListener{
                                    //count profit
                                    var multiplier = 0.0
                                    when{
                                        coin.currency == "DOLR" -> multiplier = dolr
                                        coin.currency == "PENY" -> multiplier = peny
                                        coin.currency == "QUID" -> multiplier = quid
                                        coin.currency == "SHIL" -> multiplier = shil
                                    }
                                    netWorth += coin.value*multiplier

                                    if(coin.coinGiverName != ""){
                                        coinGotten++ //update achievement value
                                    }
                                    //remove selected coin
                                    selectedCoinList.remove(coin)
                        }
                    }

                    //reset the recycler view
                    numberOfBankedInCoins = 0
                    coinGivenByOthers = ArrayList()
                    collectedCoinList = ArrayList()
                    coinList = ArrayList()
                    getCoins()
                    Log.d(tag,"banked in coin")
                }
                .setNegativeButton("No")
                { _, _ ->
                    Log.d(tag,"Not banking in coin")
                }
                .show()
    }
    private fun getCoins() {
        firestoreUserWallet?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(tag, "DocumentSnapshot data: " + document.data)
                        Log.d(tag,"number of coins in bank = ${document.data?.size}")
                        if (document.data != null || document.data?.isNotEmpty()!!) {
                            for (i in 1..(document.data?.size!!)) {
                                //coin is used if it is banked in or given to others
                                if(document["coin$i.bankedIn"] == false && document["coin$i.coinGivenToOthers"] == false){

                                    //if coin is given by others and not banked in yet
                                    if(document["coin$i.coinGivenByOthers"] == true){
                                        coinGivenByOthers.add(CoinInfo(document["coin$i.value"].toString().toDouble(),document["coin$i.currency"].toString(),i,document["coin$i.coinGiverName"].toString()))
                                    //self collected coins
                                    }else {
                                        collectedCoinList.add(CoinInfo(document["coin$i.value"].toString().toDouble(), document["coin$i.currency"].toString(), i, ""))

                                    }
                                //count the number of banked in coins
                                }else if (document["coin$i.bankedIn"] == true && document["coin$i.coinGivenToOthers"] == false && document["coin$i.coinGivenByOthers"] == false){
                                    numberOfBankedInCoins++
                                }
                            }
                            val updateText = "$numberOfBankedInCoins coin(s)\nbanked in"
                            number_of_coins_text.text = updateText

                            coinGivenByOthers.sort()
                            collectedCoinList.sort()
                            coinList.addAll(coinGivenByOthers)
                            coinList.addAll(collectedCoinList)
                            bank_recycler_view.layoutManager = LinearLayoutManager(this)
                            bank_recycler_view.adapter = BankAdapter(coinList,selectedCoinList)
                        }
                    } else {
                        Log.d("BankActivity", "User wallet no data")
                    }
                }
                ?.addOnFailureListener { exception ->
                    Log.e("BankActivity", "get user wallet failed with ", exception)
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
    class CoinInfo(val value:Double, val currency:String, val coinNumInWallet:Int,val coinGiverName :String):Comparable<CoinInfo> {
        //sort by currency(ascending) then by value of coins(descending)
        override fun compareTo(other: CoinInfo): Int {
            val compareCurrency = currency.compareTo(other.currency)

            //same currency type, then sort value of coin(descending)
            if(compareCurrency == 0){
                return when {
                    value > other.value -> -1
                    value == other.value -> 0
                    else -> 1
                }
            }

            return compareCurrency

        }
    }
}
