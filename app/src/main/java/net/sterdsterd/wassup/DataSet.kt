package net.sterdsterd.wassup

import com.naver.maps.geometry.LatLng

data class MemberData(
    val id: String,
    val name: String,
    val mac: String,
    var rssi: Int,
    var position: LatLng,
    var isDetected: Boolean,
    var undetected: Int
)

class SharedData {
    companion object {
        val studentList = mutableListOf<MemberData>()
    }
}