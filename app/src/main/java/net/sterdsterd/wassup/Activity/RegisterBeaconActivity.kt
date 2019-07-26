package net.sterdsterd.wassup.Activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore
import com.minew.beacon.*
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_register_beacon.*
import net.sterdsterd.wassup.RSSIComp
import java.util.*
import kotlin.math.min

class RegisterBeaconActivity : AppCompatActivity() {


    lateinit var filtered: List<MinewBeacon>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_beacon)

        ripple.startRippleAnimation()
        val mMinewBeaconManager = MinewBeaconManager.getInstance(this)
        try {
            mMinewBeaconManager.startScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val firestore = FirebaseFirestore.getInstance()
        val classStr = "하늘반"

        register.setOnClickListener {
            firestore.collection("class").document(classStr).collection("memberList").document(intent.getStringExtra("id")).update(
                mapOf("mac" to filtered[0].getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue))
            Toast.makeText(this, "등록되었습니다", Toast.LENGTH_SHORT).show()
            finish()
        }

        mMinewBeaconManager.setDeviceManagerDelegateListener(object : MinewBeaconManagerListener {
            override fun onRangeBeacons(minewBeacons: List<MinewBeacon>) {
                runOnUiThread {
                    filtered = minewBeacons.filter { it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).stringValue.startsWith("MiniBeacon") }
                    Collections.sort(filtered, RSSIComp())
                    if(filtered.isEmpty() || filtered[0].getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue < -55) {
                        register.visibility = View.GONE
                        registerDisabled.visibility = View.VISIBLE
                    } else {
                        register.visibility = View.VISIBLE
                        registerDisabled.visibility = View.GONE
                    }
                }
            }

            override fun onAppearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onDisappearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onUpdateState(state: BluetoothState) {
                when (state) {
                    BluetoothState.BluetoothStatePowerOn -> Toast.makeText(
                        applicationContext,
                        "BluetoothStatePowerOn",
                        Toast.LENGTH_SHORT
                    ).show()
                    BluetoothState.BluetoothStatePowerOff -> Toast.makeText(
                        applicationContext,
                        "BluetoothStatePowerOff",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

}
