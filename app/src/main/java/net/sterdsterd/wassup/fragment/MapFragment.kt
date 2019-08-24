package net.sterdsterd.wassup.fragment

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import kotlinx.android.synthetic.main.fragment_map.*
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import io.github.pierry.progress.Progress
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData
import java.text.SimpleDateFormat
import com.google.common.io.Flushables.flush
import android.graphics.Bitmap
import android.os.Environment.getExternalStorageDirectory
import android.content.Intent
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.naver.maps.map.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    val LOCATION_PERMISSION_REQUEST_CODE = 1000
    lateinit var locationSource: FusedLocationSource
    lateinit var progress: Progress

    lateinit var mapFragment: com.naver.maps.map.MapFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = activity?.supportFragmentManager?.findFragmentById(R.id.map_fragment) as com.naver.maps.map.MapFragment?
            ?: com.naver.maps.map.MapFragment.newInstance(NaverMapOptions().locationButtonEnabled(true)).also {
                activity?.supportFragmentManager?.beginTransaction()?.add(R.id.map_fragment, it)?.commit()
            }
        mapFragment.getMapAsync(this)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

    }

    private val markerList = mutableListOf<Pair<Marker, CircleOverlay>>()

    fun update() {
        mapFragment.getMapAsync { p0 ->
            markerList.forEach {
                it.first.map = null
                it.second.map = null
            }
            markerList.clear()
            SharedData.studentList.forEach {
                val marker = Marker()
                marker.position = it.vec.first
                marker.captionText = it.name
                marker.subCaptionText = SimpleDateFormat("hh:mm:ss에 마지막으로 감지").format(it.vec.second)
                marker.captionOffset = 35
                val circleOverlay = CircleOverlay(it.vec.first, 50.0)
                if (it.undetected > 5) {
                    marker.map = p0
                    circleOverlay.map = p0
                    circleOverlay.color = ContextCompat.getColor(activity!!.applicationContext, R.color.colorAccentTransparent)
                } else {
                    marker.map = null
                    circleOverlay.map = null
                    circleOverlay.color = Color.parseColor("#00000000")
                }

                markerList.add(Pair(marker, circleOverlay))
            }
        }
    }

    override fun onMapReady(p0: NaverMap) {
        locationbuttonview.map = p0

        p0.locationSource = locationSource
        p0.locationTrackingMode = LocationTrackingMode.NoFollow
        p0.isIndoorEnabled = true
        if (ContextCompat.checkSelfPermission(activity!!.applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            LocationServices.getFusedLocationProviderClient(activity!!.applicationContext).lastLocation.addOnSuccessListener {
                p0.cameraPosition = CameraPosition(LatLng(it.latitude, it.longitude), 18.0)
            }
        }

        locationSource?.isCompassEnabled = true

        btnShare.setOnClickListener {
            p0.takeSnapshot {
                share(it)
            }
        }

        SharedData.studentList.forEach {
            val marker = Marker()
            marker.position = it.vec.first
            marker.captionText = it.name
            marker.subCaptionText = SimpleDateFormat("hh:mm:ss에 마지막으로 감지").format(it.vec.second)
            marker.captionOffset = 35
            val circleOverlay = CircleOverlay(it.vec.first, 50.0)
            if (it.undetected > 5) {
                marker.map = p0
                circleOverlay.map = p0
                circleOverlay.color = ContextCompat.getColor(activity!!.applicationContext, R.color.colorAccentTransparent)
            } else {
                marker.map = null
                circleOverlay.map = null
                circleOverlay.color = Color.parseColor("#00000000")
            }

            markerList.add(Pair(marker, circleOverlay))
        }

    }

    private fun share(bitmap: Bitmap) {

        try {
            val file = File(activity?.applicationContext?.externalCacheDir, "maps")
            file.mkdirs()
            val fout = FileOutputStream("$file/asdf.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout)
            fout.close()

            val newFile = File(file, "asdf.png")
            val contentUri = FileProvider.getUriForFile(this.requireContext(), "net.sterdsterd.wassup.FileProvider", newFile)

            if (contentUri != null) {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareIntent.type = "image/png"
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                startActivity(Intent.createChooser(shareIntent, "공유할 앱을 선택해주세요"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    /*
    fun update() {
        findList?.adapter?.notifyDataSetChanged()
    }*/
}