package net.sterdsterd.wassup


import com.minew.beacon.MinewBeacon
import com.minew.beacon.BeaconValueIndex

import java.util.Comparator

class RSSIComp : Comparator<MinewBeacon> {

    override fun compare(t0: MinewBeacon, t1: MinewBeacon): Int {
        val rssi1 = t0.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).floatValue
        val rssi2 = t1.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).floatValue
        return when {
            rssi1 < rssi2 -> 1
            rssi1 == rssi2 -> 0
            else -> -1
        }
    }
}
