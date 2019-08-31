package net.sterdsterd.wassup.activity

import android.content.Context
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.firestore.FirebaseFirestore
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.activity_add.collapsingToolBar
import kotlinx.android.synthetic.main.activity_detail.*
import net.sterdsterd.wassup.Attendance
import net.sterdsterd.wassup.SharedData
import java.util.*

class AddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val classStr = pref.getString("class", "Null")

        val cal = Calendar.getInstance()
        val nowDate = "${cal.get(Calendar.YEAR)}${cal.get(Calendar.MONTH) + 1}${cal.get(Calendar.DAY_OF_MONTH)}"
        val firestore = FirebaseFirestore.getInstance().collection("class").document(classStr).collection(nowDate)
        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

        add.setOnClickListener {
            SharedData.attendanceSet.filter { it.date == nowDate }.first().taskList.add(etName.text.toString())
            firestore.document(etName.text.toString()).set(mapOf("id" to etName.text.toString()))
            finish()
        }
    }

}
