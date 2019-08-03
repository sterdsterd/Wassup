package net.sterdsterd.wassup.Activity

import android.graphics.Color
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    var isNotValidated = true
    var role = ""
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
            textAuth.setTextColor(Color.parseColor("#ffffff"))
            check.isClickable = true
            check.isFocusable = true
            check.setCardBackgroundColor(ContextCompat.getColor(this@RegisterActivity, R.color.colorAccent))
            check.cardElevation = 12f
            check.isEnabled = true
            textInputCode.visibility = View.VISIBLE
        }

        check.setOnClickListener {
            verify(etCode.text.toString())
        }

        signup.setOnClickListener {
            firestore.collection("member").document(etId.text.toString()).get().addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    if(t.result?.exists() == false) {
                        val user = mapOf(
                            "name" to etName.text.toString(),
                            "id" to etId.text.toString(),
                            "pwd" to BCrypt.withDefaults().hashToString(12, etPwd.text.toString().toCharArray()),
                            "mobile" to Regex("[^0-9]").replace(etNum.text.toString(), ""),
                            "role" to role
                        )
                        firestore.collection("member").document(etId.text.toString()).set(user)
                        Toast.makeText(this, "가입 완료", Toast.LENGTH_SHORT).show()
                        finish()
                    } else textInputId.error = "이미 사용 중인 ID입니다"
                }
            }
        }

        etNum.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        radioGroup.setOnCheckedChangeListener { _, i ->
            role = if (i == R.id.radioLead) "lead" else "class"
            textInputClass.visibility = if (role == "class") View.VISIBLE else View.GONE
            check()
        }

        val tw = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                check()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }
        etId.addTextChangedListener(tw)
        etPwd.addTextChangedListener(tw)
        etName.addTextChangedListener(tw)
        etClass.addTextChangedListener(tw)

        check.isEnabled = false
        signup.isEnabled = false
        textInputCode.visibility = View.GONE

    }

    fun check() {
        if((etId.text.isEmpty() || etPwd.text.isEmpty() || etName.text.isEmpty() || isNotValidated || role.isEmpty()) || (role == "class" && etClass.text.isEmpty())) {
            signup.cardElevation = 0f
            signup.setCardBackgroundColor(Color.parseColor("#11ffffff"))
            signup.isClickable = false
            signup.isFocusable = false
            signup.isEnabled = false
            textSignUp.setTextColor(Color.parseColor("#55ffffff"))
        } else {
            signup.cardElevation = 12f
            signup.setCardBackgroundColor(ContextCompat.getColor(this@RegisterActivity, R.color.colorAccent))
            signup.isClickable = true
            signup.isFocusable = true
            signup.isEnabled = true
            textSignUp.setTextColor(Color.parseColor("#ffffff"))
        }
    }

    fun verify(code: String) {
        val credential = PhoneAuthProvider.getCredential(mVerificationId, code)
        auth.signInWithCredential(credential).addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "인증 완료", Toast.LENGTH_SHORT).show()
                isNotValidated = false
                check()
            } else {
                textInputCode.error = "아닌데요"
            }
        }
    }

}
