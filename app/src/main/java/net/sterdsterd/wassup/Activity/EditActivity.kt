package net.sterdsterd.wassup.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_edit.*

import net.sterdsterd.wassup.R

class EditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        val id = intent.extras.getString("id")

        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        firestore.firestoreSettings = settings

        val classStr = "하늘반"
        firestore.collection("class").document(classStr).collection("memberList").document(id).get().addOnCompleteListener { t ->
            if(t.isComplete) {
                etName.setText(t.result?.getString("name"))
                etNumSt.setText(t.result?.getString("studentPhone"))
                etNumPa.setText(t.result?.getString("parentPhone"))
            }
        }

        save.setOnClickListener { v ->
            val info = mapOf(
                "name" to etName.text.toString(),
                "studentPhone" to etNumSt.text.toString(),
                "parentPhone" to etNumPa.text.toString()
            )
            firestore.collection("class").document(classStr).collection("memberList").document(id).set(info)
            finish()
        }

        deleteInfo.setOnClickListener { v ->
            firestore.collection("class").document(classStr).collection("memberList").document(id).delete().addOnSuccessListener { t ->
                finish()
            }
        }
    }

}
