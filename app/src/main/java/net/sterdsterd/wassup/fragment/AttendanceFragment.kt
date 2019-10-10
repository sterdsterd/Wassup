package net.sterdsterd.wassup.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_attendance.*
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.activity.AbsentActivity
import net.sterdsterd.wassup.activity.DetailActivity
import net.sterdsterd.wassup.activity.MainActivity
import net.sterdsterd.wassup.adapter.AttendanceAdapter
import java.text.SimpleDateFormat
import java.util.*

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

        attendanceList.layoutManager = LinearLayoutManager(view.context)
        val nowDate = SimpleDateFormat("yyyyMd").format(Calendar.getInstance().time)
        setAdapter(nowDate, SharedData.attendanceSet.firstOrNull { it.date == nowDate }?.taskList)

        cardAbs.setOnClickListener {
            context?.startActivity(Intent(context, AbsentActivity::class.java))
        }
    }

    fun setAdapter(date: String, list: MutableList<Triple<String, String, String>>?) {
        if (list?.size == 2 || list?.size == 0) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = if(date == SimpleDateFormat("yyyyMd").format(Calendar.getInstance().time)) "아래의 '+' 버튼을 눌러 작업을 추가해보세요"
            else "추가된 작업이 없어요"
        } else tvEmpty.visibility = View.GONE
        if (list != null)
            attendanceList.adapter = AttendanceAdapter(activity as MainActivity, date, list)
        attendanceList.adapter?.notifyDataSetChanged()
        Log.d("dex", list.toString())
        Log.d("dex", date)
    }

    fun refresh() {
        attendanceList.adapter?.notifyDataSetChanged()
    }

}