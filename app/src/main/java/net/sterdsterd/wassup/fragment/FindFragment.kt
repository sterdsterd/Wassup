package net.sterdsterd.wassup.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_find.*
import androidx.recyclerview.widget.GridLayoutManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.CompassView
import net.sterdsterd.wassup.activity.MainActivity
import net.sterdsterd.wassup.adapter.FindAdapter
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.activity.RestrictionActivity
import net.sterdsterd.wassup.SharedData
import kotlin.math.roundToInt


class FindFragment : Fragment(), OnMapReadyCallback {

    companion object {
        fun newInstance(): FindFragment {
            return FindFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find, container, false)
    }
    val LOCATION_PERMISSION_REQUEST_CODE = 1000
    lateinit var locationSource: FusedLocationSource


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map_view.onCreate(savedInstanceState)
        map_view.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)


    }

    override fun onMapReady(p0: NaverMap) {
        locationbuttonview.map = p0

        p0.locationSource = locationSource
        p0.locationTrackingMode = LocationTrackingMode.Follow
        p0.isIndoorEnabled = true
        p0.cameraPosition = CameraPosition(p0.cameraPosition.target, 18.0)

        for (i in SharedData.studentList) {
            val marker = Marker()
            marker.position = i.position
            marker.map = p0
        }
    }

    /*
    fun update() {
        findList?.adapter?.notifyDataSetChanged()
    }*/
}