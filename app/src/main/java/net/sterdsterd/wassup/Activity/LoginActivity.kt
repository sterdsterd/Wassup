package net.sterdsterd.wassup.Activity

import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

import net.sterdsterd.wassup.R
import android.text.Editable
import android.view.View


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
            firestore.collection("member").document(etId.text.toString()).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    if (BCrypt.verifyer().verify(etPwd.text.toString().toCharArray(), it.result?.data?.get("pwd").toString()).verified) {

                        Toast.makeText(this, "Signed In", Toast.LENGTH_SHORT).show()
                    } else textInputId.error = "아닌데요"
                }
            }
        }

        findBtn.setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
            finish()
        }

        val tw = object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if(etId.text.isEmpty() || etPwd.text.isEmpty()) {
                    signin.visibility = View.GONE
                    signinDisabled.visibility = View.VISIBLE
                }
                else {
                    signin.visibility = View.VISIBLE
                    signinDisabled.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }

        etId.addTextChangedListener(tw)
        etPwd.addTextChangedListener(tw)

    }

}
