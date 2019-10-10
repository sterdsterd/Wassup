package net.sterdsterd.wassup.activity

import akndmr.github.io.colorprefutil.ColorPrefUtil
import android.annotation.TargetApi
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import net.sterdsterd.wassup.fragment.AttendanceFragment
import net.sterdsterd.wassup.fragment.EditFragment
import net.sterdsterd.wassup.fragment.MapFragment
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.marcoscg.dialogsheet.DialogSheet
import com.minew.beacon.*
import com.naver.maps.geometry.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_attendance.*
import net.sterdsterd.wassup.Attendance
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.fragment.InfoFragment
import net.sterdsterd.wassup.service.BeaconService
import net.sterdsterd.wassup.service.PushService
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.abs


class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_attendance -> {
                appBarLayout.setExpanded(true)
                collapsingToolBar.title = "$classStr ${resources.getString(R.string.attendance)}"
                description.text = SimpleDateFormat("yyyy년 M월 d일").format(Calendar.getInstance().time)
                supportFragmentManager.beginTransaction().replace(R.id.fragment, AttendanceFragment()).commitAllowingStateLoss()
                btnToolbar.text = "날짜"
                val cal = Calendar.getInstance(TimeZone.getDefault())
                btnToolbar.setOnClickListener {

                    val listener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
                        val warn: DialogSheet = DialogSheet(this@MainActivity)
                            .setColoredNavigationBar(true)
                            .setCancelable(false)
                            .setRoundedCorners(true)
                            .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cardBg, theme))
                            .setTitle("날짜가 유효하지 않아요")
                            .setMessage("날짜를 다시 선택 해주세요")
                            .setPositiveButton("다시 선택") {
                                btnToolbar.performClick()
                            }
                        if (y > cal.get(Calendar.YEAR)) {
                            Log.d("dextr", "Y : $y -> ${cal.get(Calendar.YEAR)}")
                            warn.show()
                            return@OnDateSetListener
                        } else if (m > cal.get(Calendar.MONTH)) {
                            Log.d("dextr", "M : ${m} -> ${cal.get(Calendar.MONTH)}")
                            warn.show()
                            return@OnDateSetListener
                        } else if (d > cal.get(Calendar.DATE)) {
                            Log.d("dextr", "M : ${m} -> ${cal.get(Calendar.MONTH)}")
                            Log.d("dextr", "D : $d -> ${cal.get(Calendar.DATE)}")
                            warn.show()
                            return@OnDateSetListener
                        }

                        val progress: DialogSheet = DialogSheet(this@MainActivity)
                            .setColoredNavigationBar(true)
                            .setCancelable(false)
                            .setRoundedCorners(true)
                            .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cardBg, theme))
                            .setView(R.layout.bottom_sheet_progress)
                        progress.show()
                        description.text = SimpleDateFormat("${y}년 ${m + 1}월 ${d}일").format(Calendar.getInstance().time)
                        if (Calendar.getInstance().get(Calendar.YEAR) == y
                            && Calendar.getInstance().get(Calendar.MONTH) == m
                            && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == d) fab.show()
                        else fab.hide()

                        val firestore = FirebaseFirestore.getInstance().collection("class").document(classStr).collection("$y${m + 1}$d")
                        firestore.get().addOnCompleteListener {
                            val taskList = mutableListOf<Triple<String, String, String>>()
                            it.result?.forEach { snap ->
                                taskList.add(Triple(snap.id, snap.getString("id")!!, snap.getString("icon")!!))
                            }
                            SharedData.attendanceSet.add(Attendance("$y${m + 1}$d", taskList))
                            (supportFragmentManager.findFragmentById(R.id.fragment) as AttendanceFragment).setAdapter("$y${m + 1}$d", SharedData.attendanceSet.firstOrNull { it.date == "$y${m + 1}$d" }?.taskList)
                            progress.dismiss()
                        }

                        Log.d("dex", "$y${m + 1}$d")
                    }

                    if (Build.VERSION.SDK_INT >= 24) {
                        DatePickerDialog(this, R.style.DialogTheme, listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    }

                }
                fab.show()
                btnShare.visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_edit -> {
                appBarLayout.setExpanded(true)
                collapsingToolBar.title = resources.getString(R.string.edit)
                description.text = resources.getString(R.string.description_edit, SharedData.studentList.size)
                supportFragmentManager.beginTransaction().replace(R.id.fragment, EditFragment()).commit()
                btnToolbar.text = "추가"
                btnToolbar.setOnClickListener {
                    (supportFragmentManager.findFragmentById(R.id.fragment) as EditFragment).add()
                }
                fab.hide()
                btnShare.visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_map -> {
                startService(Intent(this, BeaconService::class.java))
                appBarLayout.setExpanded(false)
                collapsingToolBar.title = resources.getString(R.string.activity)
                description.text = ""
                supportFragmentManager.beginTransaction().replace(R.id.fragment, MapFragment()).commit()
                btnToolbar.text = ""
                fab.hide()
                btnShare.visibility = View.VISIBLE
                btnShare.setOnClickListener {
                    (supportFragmentManager.findFragmentById(R.id.fragment) as MapFragment).share()
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_info -> {
                appBarLayout.setExpanded(true)
                collapsingToolBar.title = resources.getString(R.string.myinfo)
                description.text = "내 정보를 확인할 수 있어요"
                supportFragmentManager.beginTransaction().replace(R.id.fragment, InfoFragment()).commit()
                btnToolbar.text = "수정"
                btnToolbar.setOnClickListener {
                    startActivityForResult(Intent(this, MyInfoActivity::class.java), 3)
                }
                fab.hide()
                btnShare.visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode){
            1 -> update()
            2 -> refresh()
            3 -> (supportFragmentManager.findFragmentById(R.id.fragment) as InfoFragment).refresh()
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
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val dark = pref!!.getBoolean("dark", true)
        delegate.localNightMode = if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        setContentView(R.layout.activity_main)
        if (!dark) appBarLayout.outlineProvider = null
        startService(Intent(this, BeaconService::class.java))
        fab.hide()
        val progress: DialogSheet = DialogSheet(this@MainActivity)
            .setColoredNavigationBar(true)
            .setCancelable(false)
            .setRoundedCorners(true)
            .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cardBg, theme))
            .setView(R.layout.bottom_sheet_progress)
        //progress.show()

        setSupportActionBar(toolBar)
        btnShare.visibility = View.GONE

        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        classStr = pref.getString("class", "Null")

        var mar = (fab.layoutParams) as CoordinatorLayout.LayoutParams
        mar.bottomMargin = (this.resources.displayMetrics.density * 16).toInt() + toolBarHeight()
        fab.layoutParams = mar

        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().subscribeToTopic("all")

        fab.setOnClickListener {
            startActivityForResult(Intent(this, AddActivity::class.java), 2)
        }

        mMinewBeaconManager = MinewBeaconManager.getInstance(this)
        try {
            mMinewBeaconManager.startScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val mainHandler = Handler(Looper.getMainLooper())

        var attend = mutableListOf<String>()
        var bus = mutableListOf<String>()


        nav_view.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        val cal = Calendar.getInstance()
        val nowDate = "${cal.get(Calendar.YEAR)}${cal.get(Calendar.MONTH) + 1}${cal.get(Calendar.DAY_OF_MONTH)}"

        var isLoaded = true
        var delay = 333L
        mainHandler.post(object : Runnable {
            override fun run() {
                if (SharedData.studentList.isNotEmpty() && isLoaded) {
                    nav_view.selectedItemId = nav_view.selectedItemId
                    isLoaded = false
                    delay = 1000L
                }
                if (nav_view.selectedItemId == R.id.nav_map)
                    (supportFragmentManager.findFragmentById(R.id.fragment) as MapFragment).update()

                FirebaseFirestore.getInstance().collection("class").document(classStr)
                    .collection(nowDate).document("00class").get().addOnCompleteListener { ta ->
                        attend.clear()
                        ta?.result?.data?.filter { it.key != "icon" && it.key != "id" }?.forEach {
                            attend.add(it.key)
                        }
                    }

                Log.d("dextr-abs(Att)", attend.toString())
                FirebaseFirestore.getInstance().collection("class").document(classStr)
                    .collection(nowDate).document("00bus").get().addOnCompleteListener { ta ->
                        ta?.result?.data?.filter { it.key != "icon" && it.key != "id" }?.forEach {
                            bus.add(it.key)
                        }
                    }

                SharedData.absentList.clear()
                SharedData.studentList.filter { !attend.contains(it.id) }.map { it.id }.forEach {
                    if (bus.contains(it))
                        SharedData.absentList[it] = "WARNING"
                    else SharedData.absentList[it] = "ABSENT"
                }

                badgeText?.text = SharedData.absentList.size.toString()
                Log.d("dextr-abs", SharedData.absentList.toList().toString())

                mainHandler.postDelayed(this, delay)
            }
        })

    }

    private fun toolBarHeight() : Int {
        val attrs = intArrayOf(R.attr.actionBarSize)
        val ta = this.obtainStyledAttributes(attrs)
        val toolBarHeight = ta.getDimensionPixelSize(0, -1)
        ta.recycle()
        return toolBarHeight
    }

    private fun refresh() {
        var locationProviderClient = getFusedLocationProviderClient(this)
        mMinewBeaconManager.setDeviceManagerDelegateListener(object : MinewBeaconManagerListener {
            override fun onRangeBeacons(minewBeacons: List<MinewBeacon>) {
                Thread {
                    Log.d("dext-service", "ON RANGE")
                    for (i in 0 until SharedData.studentList.size) {
                        var rssiSeq = listOf<MinewBeacon>()
                        if(SharedData.studentList.isNotEmpty()) rssiSeq = minewBeacons.filter { it.getBeaconValue(
                            BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue == SharedData.studentList[i].mac }
                        if(rssiSeq.isNotEmpty()) {
                            SharedData.studentList[i].rssi = rssiSeq[0].getBeaconValue(
                                BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue

                            if (ContextCompat.checkSelfPermission( this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
                                locationProviderClient.lastLocation.addOnSuccessListener {
                                    SharedData.studentList[i].vec = Pair(LatLng(it.latitude, it.longitude), Calendar.getInstance().time)
                                }
                            }

                            SharedData.studentList[i].isDetected = true
                            SharedData.studentList[i].undetected = 0
                        } else {
                            SharedData.studentList[i].undetected++
                            if (SharedData.studentList[i].undetected > 5)
                                SharedData.studentList[i].isDetected = false
                        }
                    }
                    if (ContextCompat.checkSelfPermission( this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
                        locationProviderClient.lastLocation.addOnSuccessListener {
                            if (it != null)
                                SharedData.tracking.add(LatLng(it.latitude, it.longitude))
                        }
                    }
                    Log.d("dext", SharedData.studentList.toString())
                }.start()
            }

            override fun onAppearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onDisappearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onUpdateState(state: BluetoothState) { }
        })
        (supportFragmentManager.findFragmentById(R.id.fragment) as AttendanceFragment).refresh()
    }

    private fun update() {
        SharedData.studentList.clear()
        val firestore = FirebaseFirestore.getInstance()
        val progress: DialogSheet = DialogSheet(this@MainActivity)
            .setColoredNavigationBar(true)
            .setCancelable(true)
            .setRoundedCorners(true)
            .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cardBg, theme))
            .setView(R.layout.bottom_sheet_progress)
        progress.show()
        firestore.collection("class").document(classStr).collection("memberList").orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener { t ->
            if(t.isComplete) {
                val v = t.result?.documents?.size as Int
                for (i in 0 until v) {
                    SharedData.studentList.add(
                        MemberData(
                            t.result?.documents?.get(i)?.id!!,
                            t.result?.documents?.get(i)?.getString("name")!!,
                            t.result?.documents?.get(i)?.getString("parentPhone")!!,
                            t.result?.documents?.get(i)?.getString("mac"),
                            t.result?.documents?.get(i)?.getString("hash")!!,
                            t.result?.documents?.get(i)?.getString("type")!!
                        )
                    )
                }
                val cal = Calendar.getInstance()
                val nowDate = "${cal.get(Calendar.YEAR)}${cal.get(Calendar.MONTH) + 1}${cal.get(Calendar.DAY_OF_MONTH)}"
                firestore.collection("class").document(classStr).collection(nowDate)
                    .document("00bus").collection("info")
                    .document("filter").delete()
                SharedData.studentList.filter { f -> f.type == "shuttle" }.forEach { q ->
                    firestore.collection("class").document(classStr).collection(nowDate).document("00bus").collection("info").document("filter").set(
                        mapOf(q.id to true), SetOptions.merge())
                }
                progress.dismiss()
                supportFragmentManager.beginTransaction().replace(R.id.fragment, EditFragment()).commit()
            }
        }
    }

}
