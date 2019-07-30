package net.sterdsterd.wassup.Activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import android.bluetooth.BluetoothAdapter

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TedPermission.with(this).setPermissionListener(object: PermissionListener {
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                //Toast.makeText(this@SplashActivity, "않이;;", Toast.LENGTH_LONG).show()
            }

            override fun onPermissionGranted() {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }

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

    }

}
