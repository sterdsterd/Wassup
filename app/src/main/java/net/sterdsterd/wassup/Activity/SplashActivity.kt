package net.sterdsterd.wassup.Activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

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

    }

}
