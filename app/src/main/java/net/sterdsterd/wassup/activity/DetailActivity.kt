package net.sterdsterd.wassup.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.adapter.DetailAdapter
import java.sql.Timestamp
import java.util.*
import kotlin.math.roundToInt

class DetailActivity : AppCompatActivity() {

    lateinit var mMinewBeaconManager: MinewBeaconManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        setSupportActionBar(toolBar)
        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val classStr = pref.getString("class", "Null")

        val taskName = intent.getStringExtra("taskName")
        val id = intent.getStringExtra("id")
        val date = intent.getStringExtra("date")
        val filteredArray = intent.getStringArrayExtra("filtered").toList()
        val checked = intent.getStringArrayExtra("checked").toList()

        val listcpy = mutableListOf<MemberData>()

        SharedData.studentList.forEach {
            val tmp = if (checked.contains(it.id)) it.copy(isBus = true) else it.copy(isBus = false)
            if (filteredArray.contains(it.id)) listcpy.add(tmp)
        }

        collapsingToolBar.title = taskName
        supportActionBar?.title = taskName
        description.text = "확인 된 인원 ${listcpy.filter { it.isBus }.size}명"
        val firestore = FirebaseFirestore.getInstance()

        val count = ((this.resources?.displayMetrics!!.widthPixels / this.resources?.displayMetrics!!.density) - 54) / 92 - 0.3
        findList?.layoutManager = GridLayoutManager(this, count.roundToInt())
        findList?.adapter = DetailAdapter(this, listcpy)
        findList?.adapter?.notifyDataSetChanged()

        val cal = Calendar.getInstance()
        val nowDate = "${cal.get(Calendar.YEAR)}${cal.get(Calendar.MONTH) + 1}${cal.get(Calendar.DAY_OF_MONTH)}"
        if (date != nowDate) {
            fab.hide()
            switchActivate.visibility = View.GONE
        }
        fab.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("taskName", taskName)
            intent.putExtra("id", id)
            startActivity(intent)
        }
        var isChecked = false
        switchActivate.setOnCheckedChangeListener { _, b -> isChecked = b }

        mMinewBeaconManager = MinewBeaconManager.getInstance(this)
        try {
            mMinewBeaconManager.startScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mMinewBeaconManager.setDeviceManagerDelegateListener(object : MinewBeaconManagerListener {
            override fun onRangeBeacons(minewBeacons: List<MinewBeacon>) {
                runOnUiThread {
                    Log.d("dext-listcpy", listcpy.toString())
                    for (i in 0 until listcpy.size) {
                        var rssiSeq = listOf<MinewBeacon>()
                        if(listcpy.isNotEmpty()) rssiSeq = minewBeacons.filter { it.getBeaconValue(
                            BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue == listcpy[i].mac }
                        if(rssiSeq.isNotEmpty()){
                            listcpy[i].rssi = rssiSeq[0].getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue
                            listcpy[i].isDetected = true
                            listcpy[i].undetected = 0
                        }
                        else {
                            listcpy[i].undetected++
                            if (listcpy[i].undetected > 5)
                                listcpy[i].isDetected = false
                        }
                    }
                    findList?.adapter?.notifyDataSetChanged()

                    if (isChecked) {
                        minewBeacons.filter {
                            it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).stringValue.startsWith("MiniBeacon")
                                    && it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue > -55
                        }.forEach {
                            val mac = it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue
                            val info = mapOf(SharedData.studentList.first { tr -> tr.mac == mac }.id to Timestamp(System.currentTimeMillis()))
                            Log.e("Beacon", info.toString())
                            firestore.collection("class").document(classStr).collection(date).document(id).set(info, SetOptions.merge())
                            SharedData.studentList.find { it0 -> it0.mac == mac }?.isBus = true
                            listcpy.find { it0 -> it0.mac == mac }?.isBus = true
                        }
                    }
                    description.text = "확인 된 인원 ${listcpy.filter { it.isBus }.size}명"
                }
            }

            override fun onAppearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onDisappearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onUpdateState(state: BluetoothState) { }
        })

    }

}
