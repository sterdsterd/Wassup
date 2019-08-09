package net.sterdsterd.wassup.Activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.circleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import gun0912.tedbottompicker.TedBottomPicker
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.activity_edit.*

import net.sterdsterd.wassup.R
import android.view.Menu
import android.view.MenuItem


class EditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        setSupportActionBar(toolBar)
        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

        val progress = Progress(this)
        progress.setBackgroundColor(Color.parseColor("#323445"))
            .setMessage("Loading")
            .show()

        val id = intent?.extras?.getString("id")

        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance().reference

        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val classStr = pref.getString("class", "Null")
        firestore.collection("class").document(classStr).collection("memberList").document(id!!).get().addOnCompleteListener { t ->
            if(t.isComplete) {
                etName.setText(t.result?.getString("name"))
                etNumSt.setText(t.result?.getString("studentPhone"))
                etNumPa.setText(t.result?.getString("parentPhone"))
                progress.dismiss()
            }
        }

        storage.child("profile/$id.png").downloadUrl.addOnSuccessListener {
            Glide.with(this).load(it).apply(RequestOptions.circleCropTransform()).into(profile)
        }

        save.setOnClickListener {
            val info = mapOf(
                "name" to etName.text.toString(),
                "studentPhone" to etNumSt.text.toString(),
                "parentPhone" to etNumPa.text.toString()
            )
            firestore.collection("class").document(classStr).collection("memberList").document(id).update(info)
            finish()
        }

        deleteInfo.setOnClickListener {
            firestore.collection("class").document(classStr).collection("memberList").document(id).delete().addOnSuccessListener {
                finish()
            }
        }

        registerBeacon.setOnClickListener {
            val intent = Intent(this, RegisterBeaconActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }

        selectImg.setOnClickListener { v ->
            TedBottomPicker.with(this).show {
                val progress = Progress(this)
                progress.setBackgroundColor(Color.parseColor("#323445"))
                    .setMessage("Uploading Image")
                    .show()
                storage.child("profile/$id.png").putFile(it).addOnSuccessListener {
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                    progress.dismiss()
                }
            }
        }
    }

}
