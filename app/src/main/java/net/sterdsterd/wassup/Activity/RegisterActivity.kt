package net.sterdsterd.wassup.Activity

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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
import kotlinx.android.synthetic.main.activity_register.collapsingToolBar


class RegisterActivity : AppCompatActivity() {

    lateinit var mVerificationId: String
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        collapsingToolBar.title = resources.getString(R.string.signup)
        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

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
                Toast.makeText(this@RegisterActivity, e.message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(s: String?, forceResendingToken: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(s, forceResendingToken)
                mVerificationId = s!!
            }
        }

        send.setOnClickListener {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+82" + etNum.text, 60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallbacks
            )
        }

        check.setOnClickListener {
            verify(etCode.text.toString())
        }

        signup.setOnClickListener { v ->
            firestore.collection("member").document(etId.text.toString()).get().addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    if(t.result?.exists() == false) {
                        val user = mapOf(
                            "name" to etName.text.toString(),
                            "id" to etId.text.toString(),
                            "pwd" to BCrypt.withDefaults().hashToString(12, etPwd.text.toString().toCharArray()),
                            "mobile" to Regex("[^0-9]").replace(etNum.text.toString(), "")
                        )
                        firestore.collection("member").document(etId.text.toString()).set(user)
                        Toast.makeText(this, "가입 완료", Toast.LENGTH_SHORT).show()
                    } else textInputId.error = "이미 사용 중인 ID입니다"
                }
            }
        }

        etNum.addTextChangedListener(PhoneNumberFormattingTextWatcher())

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
