package net.sterdsterd.wassup.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.activity_add.collapsingToolBar
import net.sterdsterd.wassup.Attendance
import net.sterdsterd.wassup.SharedData
import java.util.*

class AddActivity : AppCompatActivity() {

    var selectedIcon = ""
    var iconSrc = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val classStr = pref.getString("class", "Null")

        val cal = Calendar.getInstance()
        val nowDate = "${cal.get(Calendar.YEAR)}${cal.get(Calendar.MONTH) + 1}${cal.get(Calendar.DAY_OF_MONTH)}"
        val firestore = FirebaseFirestore.getInstance().collection("class").document(classStr).collection(nowDate)
        setSupportActionBar(toolBar)
        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

        val taskName = intent.getStringExtra("taskName")
        val id = if (intent.getStringExtra("id").isNullOrEmpty()) ""  else intent.getStringExtra("id")
        if (id.isNotEmpty()) {
            supportActionBar?.title = "이벤트 수정"
            firestore.document(id).get().addOnCompleteListener {
                selectedIcon = it.result?.getString("icon")!!
                iconSrc = it.result?.getString("icon")!!
                icon.setImageResource(resources.getIdentifier("ic_$selectedIcon", "drawable", packageName))
            }
            etName.setText(taskName)
            add.text = "수정"
        }

        add.setOnClickListener {
            if (id.isEmpty()) {
                firestore.add(mapOf("id" to etName.text.toString(), "icon" to selectedIcon))
                    .addOnCompleteListener { ta ->
                        SharedData.attendanceSet.first { it.date == nowDate }.taskList.add(
                            Triple(
                                ta.result?.id!!,
                                etName.text.toString(),
                                selectedIcon
                            )
                        )
                        val map = mutableMapOf<String, Boolean>()
                        SharedData.studentList.forEach {
                            if (it.isChecked) {
                                map[it.id] = true
                            }
                        }
                        firestore.document(ta.result?.id!!).collection("info").document("filter")
                            .set(map).addOnSuccessListener {
                                SharedData.studentList.forEach {
                                    it.isChecked = true
                                    finish()
                                }
                            }
                    }
            } else {
                firestore.document(id).set(mapOf("id" to etName.text.toString(), "icon" to selectedIcon), SetOptions.merge())
                    .addOnCompleteListener {
                        val data = SharedData.attendanceSet.first { it.date == nowDate }.taskList
                        data.remove(Triple(id, taskName, iconSrc))
                        data.add(Triple(id, etName.text.toString(), selectedIcon))

                        val map = mutableMapOf<String, Boolean>()
                        SharedData.studentList.forEach {
                            if (it.isChecked) {
                                map[it.id] = true
                            }
                        }
                        firestore.document(id).collection("info").document("filter")
                            .set(map).addOnSuccessListener {
                                SharedData.studentList.forEach {
                                    it.isChecked = true
                                    finish()
                                }
                            }
                    }
            }

        }

        selectIcon.setOnClickListener {
            startActivityForResult(Intent(this, IconActivity::class.java), 1)
        }

        filter.setOnClickListener {
            startActivity(Intent(this, FilterActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == 1) {
            icon.setImageResource(data?.getIntExtra("res", R.drawable.ic_add)!!)
            selectedIcon = data.getStringExtra("name")
        }
    }

}
