package net.sterdsterd.wassup

data class OrderData(
    val tableNo: Int,
    val time: String,
    val orderMenu: String,
    val orderQt: String,
    val key: String?
)

data class MenuData(
    val id: Int,
    val name: String,
    val price: Int,
    val src: String
)