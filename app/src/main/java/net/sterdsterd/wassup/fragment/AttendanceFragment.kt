package net.sterdsterd.wassup.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_attendance.*
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.activity.DetailActivity

class AttendanceFragment : Fragment() {

    companion object {
        fun newInstance(): AttendanceFragment {
            return AttendanceFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_attendance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardBoard.setOnClickListener {
            startActivity(Intent(activity, DetailActivity::class.java))
        }

        badgeBus.text = "${SharedData.studentList.filter { it.isBus }.size}"

    }

}