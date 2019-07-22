package net.sterdsterd.wassup.Activity

import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.TaskExecutor
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_register.*
import java.util.concurrent.TimeUnit
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential


class RegisterActivity : AppCompatActivity() {

    lateinit var mVerificationId: String
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val firestore = FirebaseFirestore.getInstance()

        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("ko")

        val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

                val code = phoneAuthCredential.smsCode

                if (code != null) {
                    etCode.setText(code)
                    verifyVerificationCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@RegisterActivity, e.message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(s: String?, forceResendingToken: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(s, forceResendingToken)
                mVerificationId = s!!
            }
        }

        send.setOnClickListener { v ->
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+82" + etNum.text, 60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallbacks
            )
        }

        check.setOnClickListener { v ->
            verifyVerificationCode(etCode.text.toString())
        }

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

    }

    fun verifyVerificationCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(mVerificationId, code)

        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show()
                    }
                }
    }

}
