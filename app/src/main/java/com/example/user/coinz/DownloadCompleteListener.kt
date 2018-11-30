package com.example.user.coinz

import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.maps.MapboxMap
import java.util.ArrayList

interface DownloadCompleteListener {
    fun downloadComplete(result: String)
    fun addCoinstoMap(map: MapboxMap?)
    fun loadWallettoDeivce()
}
