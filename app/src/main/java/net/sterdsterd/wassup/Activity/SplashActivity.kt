package net.sterdsterd.wassup.Activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.Log

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        TedPermission.with(this).setPermissionListener(object: PermissionListener {
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                finish()
            }

            override fun onPermissionGranted() { }

        }).setRationaleMessage("Beacon의 정보를 읽어들이기 위해 권한이 필요해요")
            .setDeniedMessage("않이;;")
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .check()

        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth가 지원되지 않는 기기입니다. 다른 기기를 사용해주세요.", Toast.LENGTH_LONG).show()
        } else {
            if (!mBluetoothAdapter.isEnabled) {
                startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
                Toast.makeText(this, "Bluetooth를 켜주세요.", Toast.LENGTH_LONG).show()
            }
        }

        if (pref.getString("id", "Null") != "Null") {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            //startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }

    }

}
