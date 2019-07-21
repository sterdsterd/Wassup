package net.sterdsterd.wassup.Activity

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

        signup.setOnClickListener { v ->
            firestore.collection("member").document(etId.text.toString()).get().addOnCompleteListener { t ->
                if (t.isSuccessful()) {
                    if(t.getResult()?.exists() == false) {
                        val user = mapOf(
                            "name" to "test",
                            "id" to etId.text.toString(),
                            "pwd" to BCrypt.withDefaults().hashToString(12, etPwd.text.toString().toCharArray())
                        )
                        firestore.collection("member").document(etId.text.toString()).set(user)
                        Toast.makeText(this, "가입 완료", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(this, "이미 사용 중인 ID입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        signin.setOnClickListener { v ->
            firestore.collection("member").document(etId.text.toString()).get().addOnCompleteListener { t ->
                if (t.isSuccessful()) {
                    Toast.makeText(this, BCrypt.verifyer().verify(etPwd.text.toString().toCharArray(), t.getResult()?.data?.get("pwd").toString()).verified.toString(), Toast.LENGTH_LONG).show()
                }

            }
        }


    }

}
