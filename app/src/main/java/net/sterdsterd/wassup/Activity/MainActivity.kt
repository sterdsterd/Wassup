package net.sterdsterd.wassup.Activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import net.sterdsterd.wassup.Fragment.AttandanceFragment
import net.sterdsterd.wassup.Fragment.EditFragment
import net.sterdsterd.wassup.Fragment.FindFragment
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R
import com.google.firebase.firestore.Query
import com.minew.beacon.*
import io.github.pierry.progress.Progress


class MainActivity : AppCompatActivity() {

    private lateinit var textMessage: TextView
    var now = R.id.nav_attandance
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_attandance -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, AttandanceFragment())
                    .commit()
                now = R.id.nav_attandance
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_edit -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, EditFragment())
                    .commit()
                now = R.id.nav_edit
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_find -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, FindFragment())
                    .commit()
                now = R.id.nav_find
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    val s = mutableListOf<MemberData>()


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode){
            1 -> update(true)
            //2 -> update(false)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, AttandanceFragment())
            .commit()


        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().subscribeToTopic("all")

        startActivity(Intent(this, LoginActivity::class.java))

        val mMinewBeaconManager = MinewBeaconManager.getInstance(this)
        try {
            mMinewBeaconManager.startScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mMinewBeaconManager.setDeviceManagerDelegateListener(object : MinewBeaconManagerListener {
            override fun onRangeBeacons(minewBeacons: List<MinewBeacon>) {
                runOnUiThread {
                    for (i in 0..(s.size - 1)) {
                        var rssiSeq = listOf<MinewBeacon>()
                        if(s.isNotEmpty()) rssiSeq = minewBeacons.filter { it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue == s[i].mac }
                        if(rssiSeq.isNotEmpty()) s[i].rssi = rssiSeq[0].getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue
                        if(now == R.id.nav_find) (supportFragmentManager.findFragmentById(R.id.fragment) as FindFragment).update()
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

        update(false)

    }

    fun update(con: Boolean) {
        s.clear()
        val firestore = FirebaseFirestore.getInstance()
        val classStr = "하늘반"
        val progress = Progress(this)
        progress.setBackgroundColor(Color.parseColor("#323445"))
            .setMessage("Loading")
            .show()
        firestore.collection("class").document(classStr).collection("memberList").orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener { t ->
            if(t.isComplete) {
                val v = t.result?.documents?.size as Int
                for (i in 0..(v - 1))
                    s.add(MemberData(t.result?.documents?.get(i)?.id!!,
                                     t.result?.documents?.get(i)?.getString("name")!!,
                                     t.result?.documents?.get(i)?.getString("mac")!!,
                                     0))
                progress.dismiss()
                if(con) supportFragmentManager.beginTransaction().replace(R.id.fragment, EditFragment()).commit()
            }
        }
    }

}
