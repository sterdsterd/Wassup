package net.sterdsterd.wassup.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.marcoscg.dialogsheet.DialogSheet
import kotlinx.android.synthetic.main.fragment_edit.*
import kotlinx.android.synthetic.main.fragment_info.*
import net.sterdsterd.wassup.activity.MainActivity
import net.sterdsterd.wassup.adapter.EditAdapter
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.activity.EditActivity
import net.sterdsterd.wassup.activity.SplashActivity


class InfoFragment : Fragment() {

    companion object {
        fun newInstance(): InfoFragment {
            return InfoFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    lateinit var classStr: String
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = this.activity?.getSharedPreferences("User", Context.MODE_PRIVATE)
        classStr = pref!!.getString("class", "Null")
        tvId.text = pref!!.getString("id", "Null")
        tvName.text = pref!!.getString("name", "Null")
        tvMobile.text = pref!!.getString("mobile", "Null")
        tvRole.text = if (pref!!.getString("role", "Null") == "class") "담임 교사" else "지도 교사"
        tvClass.text = classStr

        logout.setOnClickListener {
            val dialogSheet: DialogSheet = DialogSheet(it.context)
                .setTitle("진짜 로그아웃 하실래요?")
                .setMessage("다시 로그인하셔야 해요")
                .setColoredNavigationBar(true)
                .setCancelable(true)
                .setPositiveButton("네, 할래요") {
                    val pref = activity?.getSharedPreferences("User", Context.MODE_PRIVATE)
                    pref?.edit()?.clear()?.commit()
                    startActivity(Intent(activity, SplashActivity::class.java))
                    activity?.finish()
                }
                .setNegativeButton("아니요, 안 할래요") {

                }
                .setRoundedCorners(true)
                .setBackgroundColor(Color.parseColor("#323445"))
            dialogSheet.show()
        }
    }

    fun refresh() {
        val pref = this.activity?.getSharedPreferences("User", Context.MODE_PRIVATE)
        classStr = pref!!.getString("class", "Null")
        tvId.text = pref!!.getString("id", "Null")
        tvName.text = pref!!.getString("name", "Null")
        tvMobile.text = pref!!.getString("mobile", "Null")
        tvRole.text = if (pref!!.getString("role", "Null") == "class") "담임 교사" else "지도 교사"
        tvClass.text = classStr
    }

}