package net.sterdsterd.wassup

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.naver.maps.geometry.LatLng
import java.util.*

data class MemberData(
    val id: String,
    val name: String,
    val phone: String = "Null",
    val mac: String?,
    var hash: String,
    var rssi: Int = 0,
    var vec: Pair<LatLng, Date> = Pair(LatLng(0.0, 0.0), Date()),
    var isDetected: Boolean = false,
    var undetected: Int = 0,
    var isBus: Boolean = false
)

class SharedData {
    companion object {
        val studentList = mutableListOf<MemberData>()
    }
}