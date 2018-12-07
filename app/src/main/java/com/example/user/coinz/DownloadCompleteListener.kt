package com.example.user.coinz

import android.content.Context
import com.google.firebase.firestore.DocumentReference
import com.mapbox.mapboxsdk.maps.MapboxMap

interface DownloadCompleteListener {
    fun downloadComplete(result: String)
    fun addCoinstoMap(map: MapboxMap?,context: Context)
    fun emptyWallet(firestoreUserWallet: DocumentReference?)
    fun loadWalletAddCoins(firestoreUserWallet: DocumentReference?,map:MapboxMap?,context:Context)
}
