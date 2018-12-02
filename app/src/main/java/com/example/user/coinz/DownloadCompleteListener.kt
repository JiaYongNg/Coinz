package com.example.user.coinz

import com.google.firebase.firestore.DocumentReference
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.maps.MapboxMap
import java.util.ArrayList

interface DownloadCompleteListener {
    fun downloadComplete(result: String)
    fun addCoinstoMap(map: MapboxMap?)
    fun emptyWallet(firestoreUserWallet: DocumentReference?)
    fun loadWalletAddCoins(firestoreUserWallet: DocumentReference?,map:MapboxMap?)
}
