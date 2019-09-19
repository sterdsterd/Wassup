package net.sterdsterd.wassup.activity

import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_info.*

import net.sterdsterd.wassup.R
import java.util.*
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.marcoscg.dialogsheet.DialogSheet
import kotlinx.android.synthetic.main.activity_info.profile
import net.sterdsterd.wassup.SharedData
import java.io.File
import java.sql.Timestamp


class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val dark = pref!!.getBoolean("dark", true)
        delegate.localNightMode = if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        setContentView(R.layout.activity_info)
        if (!dark) appBarLayout.outlineProvider = null

        val progress: DialogSheet = DialogSheet(this)
            .setColoredNavigationBar(true)
            .setCancelable(false)
            .setRoundedCorners(true)
            .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cardBg, theme))
            .setView(R.layout.bottom_sheet_progress)
        //progress.show()

        val id = intent.extras.getString("id")
        val taskName = intent.extras.getString("taskName")
        val data = SharedData.studentList.firstOrNull { it0 -> it0.id == id }

        val firestore = FirebaseFirestore.getInstance()
        val classStr = pref.getString("class", "Null")
        val cal = Calendar.getInstance()
        val nowDate = "${cal.get(Calendar.YEAR)}${cal.get(Calendar.MONTH) + 1}${cal.get(Calendar.DAY_OF_MONTH)}"

        if (taskName.isNullOrEmpty()) confirm.visibility = View.GONE
        else {
            val fb = firestore.collection("class").document(classStr).collection(nowDate).document(taskName)
            fb.get().addOnSuccessListener {
                var data = it.get(id)
                if (data != null) {
                    switchChk.isChecked = !(data as com.google.firebase.Timestamp).toString().isNullOrEmpty()
                } else switchChk.isChecked = false
            }
        }

        collapsingToolBar.title = data?.name
        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

        val file = File(applicationContext?.externalCacheDir.toString()).listFiles().filter { it.name == "$id${data?.hash}.jpg" }.firstOrNull()
        Glide.with(this)
            .asBitmap()
            .load(file)
            .error(R.drawable.ic_profile)
            .into(profile)


        firestore.collection("class").document(classStr).collection("memberList").document(id).get().addOnCompleteListener { t ->
            if(t.isComplete) {
                tvStuPhone.setText(PhoneNumberUtils.formatNumber(t.result?.getString("studentPhone"), Locale.getDefault().country))
                tvParentPhone.setText(PhoneNumberUtils.formatNumber(t.result?.getString("parentPhone"), Locale.getDefault().country))

                callStudent.setOnClickListener{
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", t.result?.getString("studentPhone"), null)))
                }
                callParent.setOnClickListener{
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", t.result?.getString("parentPhone"), null)))
                }
                //progress.dismiss()
            }
        }

        switchChk.setOnCheckedChangeListener { _, b ->
            val fb = firestore.collection("class").document(classStr).collection(nowDate).document(taskName)
            if (b) {
                fb.set(mapOf(id to Timestamp(System.currentTimeMillis())), SetOptions.merge())
                SharedData.tmpList.find { it0 -> it0.id == id }?.isBus = true
                SharedData.tmpList.find { it0 -> it0.id == id }?.isBus = true
            } else {
                fb.set(mapOf(id to FieldValue.delete()), SetOptions.merge())
                SharedData.tmpList.find { it0 -> it0.id == id }?.isBus = false
                SharedData.tmpList.find { it0 -> it0.id == id }?.isBus = false
            }
        }

        btnToolbar.setOnClickListener {
            super.finish()
        }

    }

}
