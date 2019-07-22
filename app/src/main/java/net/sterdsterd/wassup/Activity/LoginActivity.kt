package net.sterdsterd.wassup.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

import net.sterdsterd.wassup.R





class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val firestore = FirebaseFirestore.getInstance()
        registerBtn.setOnClickListener { v ->
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        signin.setOnClickListener { v ->
            firestore.collection("member").document(etId.text.toString()).get().addOnCompleteListener { t ->
                if (t.isSuccessful()) {
                    if (BCrypt.verifyer().verify(etPwd.text.toString().toCharArray(), t.getResult()?.data?.get("pwd").toString()).verified) {
                        Toast.makeText(this, "Signed In", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

}
