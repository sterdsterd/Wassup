package net.sterdsterd.wassup.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

import net.sterdsterd.wassup.R
import android.text.Editable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.marcoscg.dialogsheet.DialogSheet


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
            val progress: DialogSheet = DialogSheet(this)
                .setColoredNavigationBar(true)
                .setCancelable(false)
                .setRoundedCorners(true)
                .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cardBg, theme))
                .setView(R.layout.bottom_sheet_progress)
            progress.show()
            firestore.collection("member").document(etId.text.toString()).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    progress.dismiss()
                    if (BCrypt.verifyer().verify(etPwd.text.toString().toCharArray(), it.result?.data?.get("pwd").toString()).verified) {
                        editor.putString("id", etId.text.toString())
                        editor.putString("role", it.result!!.get("role").toString())
                        if (it.result!!.get("role").toString() == "class")
                            editor.putString("class", it.result!!.get("class").toString())
                        editor.putString("name", it.result!!.get("name").toString())
                        editor.putString("mobile", it.result!!.get("mobile").toString())
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
                    signin.setCardBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorText11, null))
                    signin.cardElevation = 0f
                    textLogin.setTextColor(ResourcesCompat.getColor(resources, R.color.colorText55, null))
                }
                else {
                    signin.isEnabled = true
                    signin.isClickable = true
                    signin.isFocusable = true
                    signin.setCardBackgroundColor(ContextCompat.getColor(this@LoginActivity, R.color.colorAccent))
                    signin.cardElevation = 12f
                    textLogin.setTextColor(ResourcesCompat.getColor(resources, R.color.colorText, null))
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
