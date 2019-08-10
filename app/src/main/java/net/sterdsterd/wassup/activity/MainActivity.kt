package net.sterdsterd.wassup.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import net.sterdsterd.wassup.fragment.AttandanceFragment
import net.sterdsterd.wassup.fragment.EditFragment
import net.sterdsterd.wassup.fragment.FindFragment
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R
import com.google.firebase.firestore.Query
import com.minew.beacon.*
import com.naver.maps.geometry.LatLng
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.activity_main.*
import net.sterdsterd.wassup.SharedData
import java.text.SimpleDateFormat
import java.util.*




class MainActivity : AppCompatActivity() {

    var now = R.id.nav_attandance
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_attandance -> {
                collapsingToolBar.title = resources.getString(R.string.attandance)
                description.text = classStr
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, AttandanceFragment())
                    .commit()
                now = R.id.nav_attandance
                btnToolbar.text = "설정"
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_edit -> {
                collapsingToolBar.title = resources.getString(R.string.edit)
                description.text = resources.getString(R.string.description_edit, SharedData.studentList.size)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, EditFragment())
                    .commit()
                now = R.id.nav_edit
                btnToolbar.text = "추가"
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_find -> {
                appBarLayout.setExpanded(false)
                collapsingToolBar.title = resources.getString(R.string.find)
                description.text = resources.getString(R.string.description_find_no)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, FindFragment())
                    .commit()
                now = R.id.nav_find
                btnToolbar.text = ""
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode){
            1 -> update(true)
            //2 -> update(false)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        try {
            mMinewBeaconManager.stopScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            mMinewBeaconManager.startScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    lateinit var mMinewBeaconManager: MinewBeaconManager

    lateinit var classStr: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolBar)

        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.title = resources.getString(R.string.attandance)
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        classStr = pref.getString("class", "Null")
        description.text = classStr

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, AttandanceFragment())
            .commit()

        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().subscribeToTopic("all")

        mMinewBeaconManager = MinewBeaconManager.getInstance(this)
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
                        if(SharedData.studentList.isNotEmpty()) rssiSeq = minewBeacons.filter { it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue == SharedData.studentList[i].mac }
                        if(rssiSeq.isNotEmpty()) {
                            SharedData.studentList[i].rssi = rssiSeq[0].getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue
                            var locationProviderClient = getFusedLocationProviderClient(this@MainActivity)
                            if (ContextCompat.checkSelfPermission( this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
                                locationProviderClient.lastLocation.addOnSuccessListener {
                                    SharedData.studentList[i].position = LatLng(it.latitude, it.longitude)
                                }
                            }
                            SharedData.studentList[i].isDetected = true
                            SharedData.studentList[i].undetected = 0
                        } else {
                            SharedData.studentList[i].undetected++
                            if (SharedData.studentList[i].undetected > 5)
                                SharedData.studentList[i].isDetected = false
                        }
                        //if(now == R.id.nav_find) (supportFragmentManager.findFragmentById(R.id.fragment) as FindFragment).update()

                    }
                }
            }

            override fun onAppearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onDisappearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onUpdateState(state: BluetoothState) { }
        })

        update(false)

    }

    fun update(con: Boolean) {
        SharedData.studentList.clear()
        val firestore = FirebaseFirestore.getInstance()
        val progress = Progress(this)
        progress.setBackgroundColor(Color.parseColor("#323445"))
            .setMessage("Loading")
            .show()
        firestore.collection("class").document(classStr).collection("memberList").orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener { t ->
            if(t.isComplete) {
                val v = t.result?.documents?.size as Int
                for (i in 0 until v)
                    SharedData.studentList.add(MemberData(t.result?.documents?.get(i)?.id!!,
                                     t.result?.documents?.get(i)?.getString("name")!!,
                                     t.result?.documents?.get(i)?.getString("mac")!!,
                                     0, LatLng(0.0, 0.0), false, 0
                    ))
                progress.dismiss()
                if(con) supportFragmentManager.beginTransaction().replace(R.id.fragment, EditFragment()).commit()
            }
        }
    }

}
