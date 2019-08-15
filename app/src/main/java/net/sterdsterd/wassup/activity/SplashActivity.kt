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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData

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
                var target = if (pref.getString("id", "Null") == "Null") LoginActivity::class.java
                else if (pref.getString("role", "None") == "lead") BusActivity::class.java
                else MainActivity::class.java
                if (target == MainActivity::class.java) {
                    val firestore = FirebaseFirestore.getInstance()
                    firestore.collection("class").document(pref.getString("class", "Null")).collection("memberList").orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener { t ->
                        if(t.isComplete) {
                            val v = t.result?.documents?.size as Int
                            for (i in 0 until v)
                                SharedData.studentList.add(
                                    MemberData(t.result?.documents?.get(i)?.id!!,
                                        t.result?.documents?.get(i)?.getString("name")!!,
                                        t.result?.documents?.get(i)?.getString("parentPhone")!!,
                                        t.result?.documents?.get(i)?.getString("mac")
                                    )
                                )
                        }
                    }
                }
                startActivity(Intent(this@SplashActivity, target))
                finish()
            }

        }).setRationaleMessage("Beacon의 정보를 읽어들이기 위해 권한이 필요해요")
            .setDeniedMessage("않이;;")
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .check()

    }

}
