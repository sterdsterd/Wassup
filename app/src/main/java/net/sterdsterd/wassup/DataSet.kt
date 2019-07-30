package net.sterdsterd.wassup

data class MemberData(
    val id: String,
    val name: String,
    val mac: String,
    var rssi: Int
)

class SharedData {
    companion object {
        val studentList = mutableListOf<MemberData>()
    }
}