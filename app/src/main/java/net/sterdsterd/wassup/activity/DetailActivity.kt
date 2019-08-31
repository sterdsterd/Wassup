package net.sterdsterd.wassup.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.minew.beacon.*
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_detail.collapsingToolBar
import kotlinx.android.synthetic.main.activity_detail.findList
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.adapter.DetailAdapter
import java.sql.Timestamp
import kotlin.math.roundToInt

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

        collapsingToolBar.title = intent.getStringExtra("taskName")
        description.text = "현재 탑승 인원 ${SharedData.studentList.filter { it.isBus }.size}명"
        val firestore = FirebaseFirestore.getInstance()

        val count = ((this.resources?.displayMetrics!!.widthPixels / this.resources?.displayMetrics!!.density) - 54) / 92 - 0.3
        findList?.layoutManager = GridLayoutManager(this, count.roundToInt())
        findList?.adapter = DetailAdapter(this, SharedData.studentList)
        findList?.adapter?.notifyDataSetChanged()

        var isChecked = false
        switchActivate.setOnCheckedChangeListener { _, b -> isChecked = b }

        val mMinewBeaconManager = MinewBeaconManager.getInstance(this)
        try {
            mMinewBeaconManager.startScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mMinewBeaconManager.setDeviceManagerDelegateListener(object : MinewBeaconManagerListener {
            override fun onRangeBeacons(minewBeacons: List<MinewBeacon>) {
                runOnUiThread {
                    for (i in 0 until SharedData.studentList.size) {
                        var rssiSeq = listOf<MinewBeacon>()
                        if(SharedData.studentList.isNotEmpty()) rssiSeq = minewBeacons.filter { it.getBeaconValue(
                            BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue == SharedData.studentList[i].mac }
                        if(rssiSeq.isNotEmpty()){
                            SharedData.studentList[i].rssi = rssiSeq[0].getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue
                            SharedData.studentList[i].isDetected = true
                            SharedData.studentList[i].undetected = 0
                        }
                        else {
                            SharedData.studentList[i].undetected++
                            if (SharedData.studentList[i].undetected > 5)
                                SharedData.studentList[i].isDetected = false
                        }
                    }
                    findList?.adapter?.notifyDataSetChanged()

                    if (isChecked) {
                        minewBeacons.filter {
                            it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).stringValue.startsWith("MiniBeacon")
                                    && it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue > -55
                        }.forEach {
                            val mac = it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue
                            val info = mapOf(mac to Timestamp(System.currentTimeMillis()))
                            Log.e("Beacon", info.toString())
                            firestore.collection("list").document("bus").set(info, SetOptions.merge())
                            SharedData.studentList.find { it0 -> it0.mac == mac }?.isBus = true
                        }
                    }
                    description.text = "현재 탑승 인원 ${SharedData.studentList.filter { it.isBus }.size}명"
                }
            }

            override fun onAppearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onDisappearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onUpdateState(state: BluetoothState) { }
        })
    }

}
