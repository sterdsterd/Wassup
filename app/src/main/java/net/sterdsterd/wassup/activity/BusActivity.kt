package net.sterdsterd.wassup.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_bus.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.minew.beacon.*
import java.sql.Timestamp


class BusActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus)

        val mMinewBeaconManager = MinewBeaconManager.getInstance(this)

        val firestore = FirebaseFirestore.getInstance()

        switchActivate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                ripple.startRippleAnimation()
                try {
                    mMinewBeaconManager.startScan()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                ripple.stopRippleAnimation()
                ripple.startRippleAnimation()
                ripple.stopRippleAnimation()
                try {
                    mMinewBeaconManager.stopScan()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        mMinewBeaconManager.setDeviceManagerDelegateListener(object : MinewBeaconManagerListener {
            override fun onRangeBeacons(minewBeacons: List<MinewBeacon>) {
                runOnUiThread {
                    for (i in minewBeacons.filter { it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).stringValue.startsWith("MiniBeacon")
                                && it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue > -55}) {
                        val info = mapOf(i.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue to Timestamp(System.currentTimeMillis()))
                        Log.e("Beacon", info.toString())
                        firestore.collection("list").document("bus").set(info, SetOptions.merge())
                    }
                }
            }

            override fun onAppearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onDisappearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onUpdateState(state: BluetoothState) { }
        })

    }

}
