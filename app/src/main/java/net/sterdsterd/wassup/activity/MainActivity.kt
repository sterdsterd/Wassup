package net.sterdsterd.wassup.activity

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
import com.google.firebase.storage.FirebaseStorage
import com.marcoscg.dialogsheet.DialogSheet
import com.minew.beacon.*
import com.naver.maps.geometry.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import net.sterdsterd.wassup.Attendance
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.fragment.InfoFragment
import net.sterdsterd.wassup.service.BeaconService
import net.sterdsterd.wassup.service.PushService
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_attendance -> {
                appBarLayout.setExpanded(true)
                collapsingToolBar.title = "$classStr ${resources.getString(R.string.attendance)}"
                description.text = SimpleDateFormat("yyyy년 M월 d일").format(Calendar.getInstance().time)
                supportFragmentManager.beginTransaction().replace(R.id.fragment, AttendanceFragment()).commit()
                btnToolbar.text = "날짜"
                btnToolbar.setOnClickListener {

                    val listener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
                        val progress: DialogSheet = DialogSheet(this@MainActivity)
                            .setColoredNavigationBar(true)
                            .setCancelable(false)
                            .setRoundedCorners(true)
                            .setBackgroundColor(Color.parseColor("#323445"))
                            .setView(R.layout.bottom_sheet_progress)
                        progress.show()
                        description.text = SimpleDateFormat("${y}년 ${m + 1}월 ${d}일").format(Calendar.getInstance().time)
                        if (Calendar.getInstance().get(Calendar.YEAR) == y
                            && Calendar.getInstance().get(Calendar.MONTH) == m
                            && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == d) fab.show()
                        else fab.hide()

                        val firestore = FirebaseFirestore.getInstance()
                        firestore.collection("class").document(classStr).collection("$y${m + 1}$d").get().addOnCompleteListener {
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
                        val cal = Calendar.getInstance(TimeZone.getDefault())
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
                    startActivity(Intent(this, MyInfoActivity::class.java))
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
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        classStr = pref.getString("class", "Null")

        nav_view.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        nav_view.selectedItemId = nav_view.selectedItemId

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

        mainHandler.post(object : Runnable {
            override fun run() {
                if (nav_view.selectedItemId == R.id.nav_map)
                    (supportFragmentManager.findFragmentById(R.id.fragment) as MapFragment).update()
                mainHandler.postDelayed(this, 1000)
            }
        })

        startService(Intent(this, BeaconService::class.java))

    }

    private fun toolBarHeight() : Int {
        val attrs = intArrayOf(R.attr.actionBarSize)
        val ta = this.obtainStyledAttributes(attrs)
        val toolBarHeight = ta.getDimensionPixelSize(0, -1)
        ta.recycle()
        return toolBarHeight
    }

    private fun refresh() {
        (supportFragmentManager.findFragmentById(R.id.fragment) as AttendanceFragment).refresh()
    }

    private fun update() {
        SharedData.studentList.clear()
        val firestore = FirebaseFirestore.getInstance()
        val progress: DialogSheet = DialogSheet(this@MainActivity)
            .setColoredNavigationBar(true)
            .setCancelable(true)
            .setRoundedCorners(true)
            .setBackgroundColor(Color.parseColor("#323445"))
            .setView(R.layout.bottom_sheet_progress)
        progress.show()
        firestore.collection("class").document(classStr).collection("memberList").orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener { t ->
            if(t.isComplete) {
                val v = t.result?.documents?.size as Int
                for (i in 0 until v) {
                    val storage = FirebaseStorage.getInstance().reference
                    var profile: Bitmap? = null
                    storage.child("profile/${t.result?.documents?.get(i)?.id}.png").downloadUrl.addOnSuccessListener {
                        Glide.with(this).asBitmap().load(it)
                            .listener(object : RequestListener<Bitmap> {
                                override fun onResourceReady(bitmap: Bitmap, o: Any, target: Target<Bitmap>, dataSource: DataSource, b: Boolean): Boolean {
                                    profile = bitmap
                                    return false
                                }
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean) = false
                            }
                            ).submit()
                    }
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
                progress.dismiss()
                supportFragmentManager.beginTransaction().replace(R.id.fragment, EditFragment()).commit()
            }
        }
    }

}
