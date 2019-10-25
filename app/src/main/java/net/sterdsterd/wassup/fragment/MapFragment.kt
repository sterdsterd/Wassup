package net.sterdsterd.wassup.fragment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
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
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData
import java.text.SimpleDateFormat
import com.google.common.io.Flushables.flush
import android.graphics.Bitmap
import android.os.Environment.getExternalStorageDirectory
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.RingtoneManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.marcoscg.dialogsheet.DialogSheet
import com.naver.maps.map.*
import com.naver.maps.map.overlay.PathOverlay
import net.sterdsterd.wassup.activity.InfoActivity
import net.sterdsterd.wassup.activity.MainActivity
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
    var locationSource: FusedLocationSource? = null

    lateinit var mapFragment: com.naver.maps.map.MapFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)


        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as com.naver.maps.map.MapFragment?
            ?: com.naver.maps.map.MapFragment.newInstance(NaverMapOptions().locationButtonEnabled(true)).also {
                childFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)


    }

    override fun onDestroy() {
        super.onDestroy()
        locationSource = null
    }

    private val markerList = mutableListOf<Pair<Marker, CircleOverlay>>()

    fun update() {
        //TODO : Change Redundant Code to function
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
                marker.setOnClickListener { _ ->
                    val intent = Intent(activity, InfoActivity::class.java)
                    intent.putExtra("id", it.id)
                    startActivity(intent)
                    true
                }
                val circleOverlay = CircleOverlay(it.vec.first, 50.0)
                if (it.undetected > 5) {
                    marker.map = p0
                    circleOverlay.map = p0
                    circleOverlay.color = ContextCompat.getColor(activity!!.applicationContext, R.color.colorAccentTransparent)
                    if (!it.isNotified) {
                        it.isNotified = true
                        val intent = Intent(activity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("Notification", "${it.name}이 벗어났습니다")
                        }

                        val CHANNEL_ID = "childNotification"
                        val CHANNEL_NAME = "아이 정보"
                        val description = "아이의 정보를 알려드립니다."
                        val importance = NotificationManager.IMPORTANCE_HIGH

                        var notificationManager: NotificationManager =
                            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
                            channel.description = description
                            channel.enableLights(true)
                            channel.lightColor = Color.CYAN
                            channel.enableVibration(true)
                            channel.setShowBadge(true)
                            notificationManager.createNotificationChannel(channel)
                        }

                        var pendingIntent =
                            PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                        val notificationSound =
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                        var notificationBuilder = NotificationCompat.Builder(context!!, CHANNEL_ID)
                            .setLargeIcon(
                                BitmapFactory.decodeResource(context!!.resources, R.mipmap.ic_launcher)
                            )
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Wassup")
                            .setContentText("${it.name}이 벗어났습니다")
                            .setAutoCancel(true)
                            .setSound(notificationSound)
                            .setContentIntent(pendingIntent)

                        notificationManager.notify(0, notificationBuilder.build())
                    }
                } else {
                    it.isNotified = false
                    marker.map = null
                    circleOverlay.map = null
                    circleOverlay.color = Color.parseColor("#00000000")
                }

                markerList.add(Pair(marker, circleOverlay))

            }

            if (SharedData.tracking.size >= 2) {
                val pathOverlay = PathOverlay().also {
                    it.coords = SharedData.tracking
                    it.width = resources.getDimensionPixelSize(R.dimen.path_overlay_width)
                    it.outlineWidth = 0
                    it.color = ResourcesCompat.getColor(resources, R.color.colorPrimary, activity?.theme)
                    it.outlineColor = ResourcesCompat.getColor(resources, R.color.colorAccent, activity?.theme)
                    it.passedColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, activity?.theme)
                    it.passedOutlineColor = ResourcesCompat.getColor(resources, R.color.colorAccent, activity?.theme)
                    it.progress = 1.0
                    it.map = p0
                }
            }
        }
    }

    override fun onMapReady(p0: NaverMap) {
        zoomcontrol.map = p0
        locationbuttonview.map = p0

        p0.uiSettings.isZoomControlEnabled = false
        p0.uiSettings.isLocationButtonEnabled = false

        p0.locationSource = locationSource
        p0.locationTrackingMode = LocationTrackingMode.NoFollow
        p0.isIndoorEnabled = true
        if (ContextCompat.checkSelfPermission(activity!!.applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            LocationServices.getFusedLocationProviderClient(activity!!.applicationContext).lastLocation.addOnSuccessListener {
                if (it != null)
                    p0.cameraPosition = CameraPosition(LatLng(it.latitude, it.longitude), 18.0)
            }
        }

        locationSource?.isCompassEnabled = true

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

    fun share() {
        val progress: DialogSheet = DialogSheet(activity)
            .setColoredNavigationBar(true)
            .setCancelable(false)
            .setRoundedCorners(true)
            .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cardBg, null))
            .setView(R.layout.bottom_sheet_progress)
        progress.show()
        progress.inflatedView.findViewById<TextView>(R.id.title).text = "지도 사진을 저장하고 있어요"
        mapFragment.getMapAsync { p0 ->
            p0.takeSnapshot {
                try {
                    val file = File(activity?.applicationContext?.externalCacheDir, "maps")
                    file.mkdirs()
                    val fout = FileOutputStream("$file/asdf.png")
                    it.compress(Bitmap.CompressFormat.PNG, 100, fout)
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
                    progress.dismiss()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}