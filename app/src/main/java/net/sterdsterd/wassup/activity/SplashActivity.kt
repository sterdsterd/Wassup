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
import android.R.attr.start
import android.app.PendingIntent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.firestore.SetOptions
import net.sterdsterd.wassup.Attendance
import net.sterdsterd.wassup.service.BeaconService
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*


class SplashActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        val mNfcAdapter = NfcAdapter.getDefaultAdapter(this@SplashActivity)
        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val ndefDetected = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val techDetected = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)

        val nfcIntentFilter = arrayOf(techDetected, tagDetected, ndefDetected)

        val pendingIntent = PendingIntent.getActivity(this@SplashActivity, 0, Intent(this@SplashActivity, SplashActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)

        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this@SplashActivity, pendingIntent, nfcIntentFilter, null)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val dark = pref!!.getBoolean("dark", true)
        delegate.localNightMode = if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        val intent = Intent(this, BeaconService::class.java)
        stopService(intent)
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
                } else {

                    val firestore = FirebaseFirestore.getInstance()
                    val cal = Calendar.getInstance()
                    val classStr = pref.getString("class", "Null")
                    val nowDate = "${cal.get(Calendar.YEAR)}${cal.get(Calendar.MONTH) + 1}${cal.get(Calendar.DAY_OF_MONTH)}"
                    firestore.collection("class").document(classStr).collection(nowDate).get().addOnCompleteListener {
                        firestore.collection("class").document(classStr).collection("memberList").orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener { t ->
                            if (t.isComplete) {
                                val v = t.result?.documents?.size as Int
                                val taskList = mutableListOf<Triple<String, String, String>>()
                                it.result?.forEach { snap ->
                                    taskList.add(Triple(snap.id, snap.getString("id")!!, snap.getString("icon")!!))
                                }
                                if (!taskList.contains(Triple("00bus", "셔틀 버스", "bus"))) {
                                    firestore.collection("class").document(classStr).collection(nowDate).document("00bus")
                                        .set(mapOf("icon" to "bus", "id" to "셔틀 버스"))
                                    for (i in 0 until v) {
                                        Log.d("dextest", "${t.result?.documents?.get(i)?.id} -> ${t.result?.documents?.get(i)?.getString("type")}")
                                        if (t.result?.documents?.get(i)?.getString("type") == "shuttle")
                                            firestore.collection("class").document(classStr).collection(nowDate).document("00bus").collection("info").document("filter").set(
                                                mapOf(t.result?.documents?.get(i)?.id to true), SetOptions.merge())
                                    }
                                    taskList.add(Triple("00bus", "셔틀 버스", "bus"))
                                }
                                if (!taskList.contains(Triple("00class", "교실", "school"))) {
                                    firestore.collection("class").document(classStr).collection(nowDate).document("00class")
                                        .set(mapOf("icon" to "school", "id" to "교실"))
                                    for (i in 0 until v) {
                                            firestore.collection("class").document(classStr).collection(nowDate).document("00class").collection("info").document("filter").set(
                                                mapOf(t.result?.documents?.get(i)?.id to true), SetOptions.merge())
                                    }
                                    taskList.add(Triple("00class", "교실", "school"))
                                }
                                SharedData.attendanceSet.add(Attendance(nowDate, taskList))
                                var cnt = 0
                                for (i in 0 until v) {
                                    Log.d("dex", "${t.result?.documents?.get(i)?.getString("name")} LOADED")

                                    if (File(this@SplashActivity.applicationContext?.externalCacheDir.toString()).listFiles().filter {
                                            it.name == "${t.result?.documents?.get(i)?.id}${t.result?.documents?.get(i)?.getString("hash")}.jpg"
                                        }.isEmpty()) {
                                        val storage = FirebaseStorage.getInstance().reference
                                        storage.child("profile/${t.result?.documents?.get(i)?.id}.jpeg")
                                            .downloadUrl.addOnSuccessListener { Log.d("dex", "${t.result?.documents?.get(i)?.getString("name")} DOWNLOADED")
                                            Glide.with(this@SplashActivity.applicationContext)
                                                .asBitmap().load(it)
                                                .into(object : CustomTarget<Bitmap>() {
                                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                        saveImg(resource, t.result?.documents?.get(i)?.id + t.result?.documents?.get(i)?.getString("hash"))
                                                        cnt++
                                                        if (cnt == v) {
                                                            startService(intent)
                                                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                                            finish()
                                                        }
                                                    }

                                                    override fun onLoadCleared(placeholder: Drawable?) {}
                                                })
                                        }.addOnFailureListener {
                                            cnt++
                                            if (cnt == v) {
                                                startService(intent)
                                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
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
                                            startService(intent)
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val tag: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)

        if(tag != null) {
            Toast.makeText(this, "DEECTED", Toast.LENGTH_SHORT).show()
            val ndef = Ndef.get(tag)
            onNfcDetected(ndef)
        }
    }

    fun onNfcDetected(ndef: Ndef) {
        readFromNFC(ndef)
    }

    private fun readFromNFC(ndef: Ndef) {
        try {
            ndef.connect()
            val ndefMessage = ndef.getNdefMessage()
            val msg = String(ndefMessage.records[0].payload)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            ndef.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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