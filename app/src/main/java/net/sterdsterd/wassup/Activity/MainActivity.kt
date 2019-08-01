package net.sterdsterd.wassup.Activity

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.ActionBar
import androidx.core.content.res.ResourcesCompat
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
import kotlinx.android.synthetic.main.activity_main.*
import net.sterdsterd.wassup.SharedData


class MainActivity : AppCompatActivity() {

    var now = R.id.nav_attandance
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_attandance -> {
                collapsingToolBar.title = resources.getString(R.string.attandance)
                description.text = "햇반"
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, AttandanceFragment())
                    .commit()
                now = R.id.nav_attandance
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_edit -> {
                collapsingToolBar.title = resources.getString(R.string.edit)
                description.text = resources.getString(R.string.description_edit, SharedData.studentList.size)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, EditFragment())
                    .commit()
                now = R.id.nav_edit
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_find -> {
                collapsingToolBar.title = resources.getString(R.string.find)
                description.text = resources.getString(R.string.description_find_no)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, FindFragment())
                    .commit()
                now = R.id.nav_find
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

    fun Toolbar.changeToolbarFont(){
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view is TextView && view.text == title) {
                view.typeface = Typeface.createFromAsset(view.context.assets, "font/spoqa_bold")
                break
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolBar)

        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.title = resources.getString(R.string.attandance)
        description.text = "햇반"

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, AttandanceFragment())
            .commit()

        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().subscribeToTopic("all")

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
                        if(SharedData.studentList.isNotEmpty()) rssiSeq = minewBeacons.filter { it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue == SharedData.studentList[i].mac }
                        if(rssiSeq.isNotEmpty()) SharedData.studentList[i].rssi = rssiSeq[0].getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue
                        if(now == R.id.nav_find) (supportFragmentManager.findFragmentById(R.id.fragment) as FindFragment).update()
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
        val classStr = "하늘반"
        val progress = Progress(this)
        progress.setBackgroundColor(Color.parseColor("#323445"))
            .setMessage("Loading")
            .show()
        firestore.collection("class").document(classStr).collection("memberList").orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener { t ->
            if(t.isComplete) {
                val v = t.result?.documents?.size as Int
                for (i in 0..(v - 1))
                    SharedData.studentList.add(MemberData(t.result?.documents?.get(i)?.id!!,
                                     t.result?.documents?.get(i)?.getString("name")!!,
                                     t.result?.documents?.get(i)?.getString("mac")!!,
                                     0))
                progress.dismiss()
                if(con) supportFragmentManager.beginTransaction().replace(R.id.fragment, EditFragment()).commit()
            }
        }
    }

}
