package com.example.user.coinz

import android.content.Context
import android.content.res.Resources
import com.google.firebase.firestore.DocumentReference
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.storage.Resource
import java.util.ArrayList

interface DownloadCompleteListener {
    fun downloadComplete(result: String)
    fun addCoinstoMap(map: MapboxMap?,context: Context)
    fun emptyWallet(firestoreUserWallet: DocumentReference?)
    fun loadWalletAddCoins(firestoreUserWallet: DocumentReference?,map:MapboxMap?,context:Context)
}
