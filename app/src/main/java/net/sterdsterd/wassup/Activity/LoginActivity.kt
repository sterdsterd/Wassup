package net.sterdsterd.wassup.Activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
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
import androidx.core.content.ContextCompat
import io.github.pierry.progress.Progress


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val editor = pref.edit()

        val firestore = FirebaseFirestore.getInstance()
        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        signin.setOnClickListener {
            val progress = Progress(this)
            progress.setBackgroundColor(Color.parseColor("#323445"))
                .setMessage("Loading")
                .show()
            firestore.collection("member").document(etId.text.toString()).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    progress.dismiss()
                    if (BCrypt.verifyer().verify(etPwd.text.toString().toCharArray(), it.result?.data?.get("pwd").toString()).verified) {
                        editor.putString("id", etId.text.toString())
                        editor.putString("role", it.result!!.get("role").toString())
                        if (it.result!!.get("role").toString() == "class")
                            editor.putString("class", it.result!!.get("class").toString())
                        editor.apply()
                        startActivity(Intent(this, SplashActivity::class.java))
                        finish()
                    } else textInputId.error = "아닌데요"
                }
            }
        }

        findBtn.setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
        }

        val tw = object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if(etId.text.isEmpty() || etPwd.text.isEmpty()) {
                    signin.isEnabled = false
                    signin.isClickable = false
                    signin.isFocusable = false
                    signin.setCardBackgroundColor(Color.parseColor("#11ffffff"))
                    signin.cardElevation = 0f
                    textLogin.setTextColor(Color.parseColor("#55ffffff"))
                }
                else {
                    signin.isEnabled = true
                    signin.isClickable = true
                    signin.isFocusable = true
                    signin.setCardBackgroundColor(ContextCompat.getColor(this@LoginActivity, R.color.colorAccent))
                    signin.cardElevation = 12f
                    textLogin.setTextColor(Color.parseColor("#ffffff"))
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }

        etId.addTextChangedListener(tw)
        etPwd.addTextChangedListener(tw)
        signin.isEnabled = false

    }

}
