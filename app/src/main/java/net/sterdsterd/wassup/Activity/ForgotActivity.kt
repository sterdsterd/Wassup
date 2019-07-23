package net.sterdsterd.wassup.Activity

import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
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

            override fun onCodeSent(s: String?, forceResendingToken: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(s, forceResendingToken)
                mVerificationId = s!!
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

        }

        check.setOnClickListener {
            verify(etCode.text.toString())
        }
    }

    fun verify(code: String) {
        val credential = PhoneAuthProvider.getCredential(mVerificationId, code)
        auth.signInWithCredential(credential).addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            } else {
                textInputCode.error = "아닌데요"
            }
        }
    }

}
