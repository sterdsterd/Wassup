package net.sterdsterd.wassup.activity

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.location.LocationServices
import com.minew.beacon.*
import com.naver.maps.geometry.LatLng
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_detail.collapsingToolBar
import kotlinx.android.synthetic.main.activity_detail.findList
import kotlinx.android.synthetic.main.item_find.*
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.adapter.FindAdapter
import kotlin.math.roundToInt

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

        description.text = "현재 출석 인원 ${SharedData.studentList.size}명"

        val count = ((this?.resources?.displayMetrics!!.widthPixels / this?.resources?.displayMetrics!!.density) - 54) / 92 - 0.3
        findList?.layoutManager = GridLayoutManager(this, count.roundToInt())
        findList?.adapter = FindAdapter(this, SharedData.studentList)
        findList?.adapter?.notifyDataSetChanged()

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
                        //if(now == R.id.nav_find) (supportFragmentManager.findFragmentById(R.id.fragment) as FindFragment).update()
                    }
                    findList?.adapter?.notifyDataSetChanged()
                }
            }

            override fun onAppearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onDisappearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onUpdateState(state: BluetoothState) { }
        })
    }

}
