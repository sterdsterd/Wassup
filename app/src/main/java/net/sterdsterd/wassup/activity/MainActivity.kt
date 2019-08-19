package net.sterdsterd.wassup.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.LinearLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginBottom
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import net.sterdsterd.wassup.fragment.AttandanceFragment
import net.sterdsterd.wassup.fragment.EditFragment
import net.sterdsterd.wassup.fragment.MapFragment
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.minew.beacon.*
import com.naver.maps.geometry.LatLng
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.activity_main.*
import net.sterdsterd.wassup.SharedData
import java.text.SimpleDateFormat
import java.util.*




class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_attandance -> {
                appBarLayout.setExpanded(true)
                collapsingToolBar.title = "$classStr ${resources.getString(R.string.attandance)}"
                description.text = SimpleDateFormat("yyyy년 MM월 dd일").format(Calendar.getInstance().time)
                supportFragmentManager.beginTransaction().replace(R.id.fragment, AttandanceFragment()).commit()
                btnToolbar.text = ""
                fab.show()
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
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_map -> {
                appBarLayout.setExpanded(false)
                collapsingToolBar.title = resources.getString(R.string.activity)
                description.text = ""
                supportFragmentManager.beginTransaction().replace(R.id.fragment, MapFragment()).commit()
                btnToolbar.text = ""
                fab.hide()
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
            startActivity(Intent(this, AddActivity::class.java))
        }

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

                    if (nav_view.selectedItemId == R.id.nav_map) (supportFragmentManager.findFragmentById(R.id.fragment) as MapFragment).update()
                }
            }

            override fun onAppearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onDisappearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onUpdateState(state: BluetoothState) { }
        })

    }

    fun toolBarHeight() : Int {
        val attrs = intArrayOf(R.attr.actionBarSize)
        val ta = this.obtainStyledAttributes(attrs)
        val toolBarHeight = ta.getDimensionPixelSize(0, -1)
        ta.recycle()
        return toolBarHeight
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
                            t.result?.documents?.get(i)?.getString("hash")!!
                        )
                    )
                }
                progress.dismiss()
                if(con) supportFragmentManager.beginTransaction().replace(R.id.fragment, EditFragment()).commit()
            }
        }
    }

}
