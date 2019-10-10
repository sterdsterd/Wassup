package net.sterdsterd.wassup.activity

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_forgot.*
import java.util.concurrent.TimeUnit

class ForgotActivity : AppCompatActivity() {


    lateinit var mVerificationId: String
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
        setContentView(R.layout.activity_forgot)

        val firestore = FirebaseFirestore.getInstance()

        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("ko")

        val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    etCode.setText(code)
                    verify(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@ForgotActivity, e.message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                mVerificationId = p0
            }

        }
        send.setOnClickListener {
            firestore.collection("member").document(etId.text.toString()).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+82" + it?.result?.data?.get("mobile"), 60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallbacks
                    )
                }
            }
            textInputCode.visibility = View.VISIBLE
            check.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
            check.isEnabled = true
            check.isClickable = true
            check.isFocusable = true
            check.cardElevation = 12f
            tvVerify.setTextColor(ResourcesCompat.getColor(resources, R.color.colorText, null))

        }
        check.isEnabled = false
        change.isEnabled = false

        check.setOnClickListener {
            verify(etCode.text.toString())
        }

        etPwd.addTextChangedListener( object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (etPwd.text.isNotEmpty()) {
                    change.setCardBackgroundColor(ContextCompat.getColor(this@ForgotActivity, R.color.colorAccent))
                    change.cardElevation = 12f
                    change.isFocusable = true
                    change.isClickable = true
                    change.isEnabled = true
                    tvChange.setTextColor(ResourcesCompat.getColor(resources, R.color.colorText, null))
                } else {
                    change.setCardBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorText11, null))
                    change.cardElevation = 0f
                    change.isFocusable = false
                    change.isClickable = false
                    change.isEnabled = false
                    tvChange.setTextColor(ResourcesCompat.getColor(resources, R.color.colorText55, null))
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        change.setOnClickListener {
            firestore.collection("member").document(etId.text.toString())
                .update(mapOf("pwd" to BCrypt.withDefaults().hashToString(12, etPwd.text.toString().toCharArray())))
                .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "변경 완료", Toast.LENGTH_LONG).show()
                    finish()
                }
            }

        }

    }

    fun verify(code: String) {
        val credential = PhoneAuthProvider.getCredential(mVerificationId, code)
        auth.signInWithCredential(credential).addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "인증 완료", Toast.LENGTH_SHORT).show()
                cardPwd.visibility = View.VISIBLE
            } else {
                textInputCode.error = "아닌데요"
            }
        }
    }

}
