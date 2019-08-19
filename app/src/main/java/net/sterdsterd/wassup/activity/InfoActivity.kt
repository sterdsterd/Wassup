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
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_info.profile
import net.sterdsterd.wassup.SharedData
import java.io.File


class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)


        val progress = Progress(this)
        progress.setBackgroundColor(Color.parseColor("#323445"))
            .setMessage("Loading")
            .show()

        val id = intent.extras.getString("id")
        val data = SharedData.studentList.filter { it0 -> it0.id == id }[0]

        tvTitle.text = data.name

        val file = File(cacheDir.toString()).listFiles().filter { it.name == "$id${data.hash}.jpg" }[0]
        Glide.with(this)
            .asBitmap()
            .load(file)
            .error(R.drawable.ic_profile)
            .apply(RequestOptions.circleCropTransform())
            .into(profile)

        val firestore = FirebaseFirestore.getInstance()

        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val classStr = pref.getString("class", "Null")
        firestore.collection("class").document(classStr).collection("memberList").document(id).get().addOnCompleteListener { t ->
            if(t.isComplete) {
                tvName.setText(t.result?.getString("name"))
                tvStuPhone.setText(PhoneNumberUtils.formatNumber(t.result?.getString("studentPhone"), Locale.getDefault().getCountry()))
                tvParentPhone.setText(PhoneNumberUtils.formatNumber(t.result?.getString("parentPhone"), Locale.getDefault().getCountry()))

                callStudent.setOnClickListener{
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", t.result?.getString("studentPhone"), null)))
                }
                callParent.setOnClickListener{
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", t.result?.getString("parentPhone"), null)))
                }
                progress.dismiss()
            }
        }

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, mutableListOf("출석", "결석", "탑승"))

        back.setOnClickListener {
            super.finish()
        }

    }

}
