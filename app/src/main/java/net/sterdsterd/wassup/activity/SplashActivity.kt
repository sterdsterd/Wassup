package net.sterdsterd.wassup.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData
import com.naver.maps.map.style.sources.ImageSource
import android.R.attr.bitmap
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import net.sterdsterd.wassup.Attendance
import net.sterdsterd.wassup.service.BeaconService
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth가 지원되지 않는 기기입니다. 다른 기기를 사용해주세요.", Toast.LENGTH_LONG).show()
        } else {
            if (!mBluetoothAdapter.isEnabled) {
                startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
                Toast.makeText(this, "Bluetooth를 켜주세요.", Toast.LENGTH_LONG).show()
            }
        }

        TedPermission.with(this).setPermissionListener(object: PermissionListener {
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                finish()
            }

            override fun onPermissionGranted() {
                if (pref.getString("id", "Null") == "Null") {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                } else if (pref.getString("role", "None") == "lead") {
                    startActivity(Intent(this@SplashActivity, BusActivity::class.java))
                    finish()
                }
                else {
                    val firestore = FirebaseFirestore.getInstance()
                    val cal = Calendar.getInstance()
                    val nowDate = "${cal.get(Calendar.YEAR)}${cal.get(Calendar.MONTH) + 1}${cal.get(Calendar.DAY_OF_MONTH)}"
                    firestore.collection("class").document(pref.getString("class", "Null")).collection(nowDate).get().addOnCompleteListener {
                        val taskList = mutableListOf<Triple<String, String, String>>()
                        it.result?.forEach { snap ->
                            taskList.add(Triple(snap.id, snap.getString("id")!!, snap.getString("icon")!!))
                        }
                        SharedData.attendanceSet.add(Attendance(nowDate, taskList))
                        SharedData.studentList.clear()
                        firestore.collection("class").document(pref.getString("class", "Null")).collection("memberList").orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener { t ->
                            if (t.isComplete) {
                                val v = t.result?.documents?.size as Int
                                var cnt = 0
                                for (i in 0 until v) {
                                    Log.d(
                                        "dex",
                                        "${t.result?.documents?.get(i)?.getString("name")} LOADED"
                                    )
                                    /*
                                    SharedData.studentList.add(
                                        MemberData(
                                            t.result?.documents?.get(i)?.id!!,
                                            t.result?.documents?.get(i)?.getString("name")!!,
                                            t.result?.documents?.get(i)?.getString("parentPhone")!!,
                                            t.result?.documents?.get(i)?.getString("mac"),
                                            t.result?.documents?.get(i)?.getString("hash")!!
                                        )
                                    )*/

                                    val intent = Intent(this@SplashActivity, BeaconService::class.java)
                                    stopService(intent)
                                    startService(intent)

                                    if (File(this@SplashActivity.applicationContext?.externalCacheDir.toString()).listFiles().filter {
                                            it.name == "${t.result?.documents?.get(
                                                i
                                            )?.id}${t.result?.documents?.get(i)?.getString("hash")}.jpg"
                                        }.isEmpty()) {
                                        val storage = FirebaseStorage.getInstance().reference
                                        storage.child("profile/${t.result?.documents?.get(i)?.id}.jpeg")
                                            .downloadUrl.addOnSuccessListener {
                                            Log.d(
                                                "dex",
                                                "${t.result?.documents?.get(i)?.getString("name")} DOWNLOADED"
                                            )
                                            Glide.with(this@SplashActivity.applicationContext)
                                                .asBitmap().load(it)
                                                .into(object : CustomTarget<Bitmap>() {
                                                    override fun onResourceReady(
                                                        resource: Bitmap,
                                                        transition: Transition<in Bitmap>?
                                                    ) {
                                                        saveImg(
                                                            resource,
                                                            t.result?.documents?.get(i)?.id + t.result?.documents?.get(
                                                                i
                                                            )?.getString("hash")
                                                        )
                                                        cnt++
                                                        if (cnt == v) {
                                                            startActivity(
                                                                Intent(
                                                                    this@SplashActivity,
                                                                    MainActivity::class.java
                                                                )
                                                            )
                                                            finish()
                                                        }
                                                    }

                                                    override fun onLoadCleared(placeholder: Drawable?) {}
                                                })
                                        }.addOnFailureListener {
                                            cnt++
                                            if (cnt == v) {
                                                startActivity(
                                                    Intent(
                                                        this@SplashActivity,
                                                        MainActivity::class.java
                                                    )
                                                )
                                                finish()
                                            }
                                        }
                                    } else {
                                        Log.d(
                                            "dex",
                                            "${t.result?.documents?.get(i)?.getString("name")} ALREADY CACHED"
                                        )
                                        cnt++
                                        if (cnt == v) {
                                            startActivity(
                                                Intent(
                                                    this@SplashActivity,
                                                    MainActivity::class.java
                                                )
                                            )
                                            finish()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }).setRationaleMessage("Beacon의 정보를 읽어들이기 위해 권한이 필요해요")
            .setDeniedMessage("않이;;")
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .check()

    }

    fun saveImg(bitmap: Bitmap, id: String?) {
        val file = File(applicationContext?.externalCacheDir, "$id.jpg")
        try {
            file.createNewFile()
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}