package net.sterdsterd.wassup.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import gun0912.tedbottompicker.TedBottomPicker
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.activity_edit.*

import net.sterdsterd.wassup.R
import android.provider.MediaStore
import android.util.Log
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import net.sterdsterd.wassup.SharedData
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


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


        val file = File(applicationContext?.externalCacheDir.toString()).listFiles().firstOrNull { it.name == "$id${SharedData.studentList.firstOrNull { it0 -> it0.id == id }?.hash}.jpg" }
        Glide.with(this)
            .asBitmap()
            .load(file)
            .error(R.drawable.ic_profile)
            .apply(RequestOptions.circleCropTransform())
            .into(profile)

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


        selectImg.setOnClickListener {
            TedBottomPicker.with(this).show {
                val progress = Progress(this)
                progress.setBackgroundColor(Color.parseColor("#323445"))
                    .setMessage("Compressing Image")
                    .show()

                val byteArrayOutputStream = ByteArrayOutputStream()
                MediaStore.Images.Media.getBitmap(this.contentResolver, it).compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
                val data = byteArrayOutputStream.toByteArray()
                progress.setMessage("Uploading Image")
                storage.child("profile/$id.jpeg").putBytes(data).addOnSuccessListener {
                    Log.d("dex", "BEFORE ${SharedData.studentList.filter { it0 -> it0.id == id }[0].hash}")
                    SharedData.studentList.filter { it0 -> it0.id == id }[0].hash = "${SharedData.studentList.filter { it0 -> it0.id == id }[0].hash.toInt() + 1}"
                    Log.d("dex", "AFTER ${SharedData.studentList.filter { it0 -> it0.id == id }[0].hash}")
                    val info = mapOf(
                        "hash" to SharedData.studentList.filter { it0 -> it0.id == id }[0].hash
                    )
                    firestore.collection("class").document(classStr).collection("memberList").document(id).update(info)
                    val storage = FirebaseStorage.getInstance().reference
                    storage.child("profile/$id.jpeg")
                        .downloadUrl.addOnSuccessListener { uri ->
                        progress.setMessage("Caching Image")
                        Glide.with(this.applicationContext).asBitmap().load(uri)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    Log.d("dex", "$id${SharedData.studentList.filter { it0 -> it0.id == id }[0].hash} CACHED")
                                    saveImg(resource, id + SharedData.studentList.filter { it0 -> it0.id == id }[0].hash)
                                    progress.setMessage("Applying Image")
                                    Glide.with(this@EditActivity)
                                        .asBitmap()
                                        .load(File(applicationContext?.externalCacheDir.toString()).listFiles().filter { it.name == "$id${SharedData.studentList.filter { it0 -> it0.id == id }[0].hash}.jpg" }.firstOrNull())
                                        .error(R.drawable.ic_profile)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(profile)
                                    Toast.makeText(this@EditActivity, "Success", Toast.LENGTH_SHORT).show()
                                    progress.dismiss()
                                }
                                override fun onLoadCleared(placeholder: Drawable?) {}
                            })
                    }

                }
            }
        }
    }

    fun saveImg(bitmap: Bitmap, id: String?) {
        val file = File(applicationContext?.externalCacheDir, "$id.jpg")
        try {
            file.createNewFile()
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
