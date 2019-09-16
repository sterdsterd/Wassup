package net.sterdsterd.wassup.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.marcoscg.dialogsheet.DialogSheet
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_my_info.*
import kotlinx.android.synthetic.main.activity_my_info.collapsingToolBar
import kotlinx.android.synthetic.main.activity_my_info.etClass
import kotlinx.android.synthetic.main.activity_my_info.etId
import kotlinx.android.synthetic.main.activity_my_info.etName
import kotlinx.android.synthetic.main.activity_my_info.etNum
import kotlinx.android.synthetic.main.activity_my_info.etPwd
import kotlinx.android.synthetic.main.activity_my_info.radioHr
import kotlinx.android.synthetic.main.activity_register.*

class MyInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_info)
        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)

        etId.text = SpannableStringBuilder(pref.getString("id", "Null"))
        etId.keyListener = null
        etName.text = SpannableStringBuilder(pref.getString("name", "Null"))
        etClass.text = SpannableStringBuilder(pref.getString("class", "Null"))
        etNum.text = SpannableStringBuilder(pref.getString("mobile", "Null"))
        radioHr.isChecked = true

        save.setOnClickListener {
            val name = etName.text.toString()
            val cl = etClass.text.toString()
            val num = etNum.text.toString()
            val pwd = etPwd.text.toString()

            val confirmation: DialogSheet = DialogSheet(this@MyInfoActivity)
                .setColoredNavigationBar(true)
                .setCancelable(true)
                .setRoundedCorners(true)
                .setTitle("암호를 변경하려면 확인이 필요해요")
                .setMessage("기존에 쓰던 비밀번호를 입력해주세요")
                .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cardBg, theme))
                .setView(R.layout.bottom_sheet_confirm)

            val etPwd = confirmation.inflatedView.findViewById<EditText>(R.id.etPwd)

            val progress: DialogSheet = DialogSheet(this@MyInfoActivity)
                .setColoredNavigationBar(true)
                .setCancelable(true)
                .setRoundedCorners(true)
                .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cardBg, theme))
                .setView(R.layout.bottom_sheet_progress)

            confirmation.setPositiveButton("확인") {
                progress.show()
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("member").document(etId.text.toString()).get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        //TODO : PHONE VERIFICATION
                        if (BCrypt.verifyer().verify(etPwd.text.toString().toCharArray(), it.result?.data?.get("pwd").toString()).verified) {
                            val map = mutableMapOf("name" to name,
                                "mobile" to num,
                                "class" to cl)
                            if (pwd.isNotEmpty()) map["pwd"] = BCrypt.withDefaults().hashToString(12, pwd.toCharArray())
                            val editor = pref.edit()
                            editor.putString("name", name)
                            editor.putString("mobile", num)
                            editor.putString("class", cl)
                            editor.apply()
                            firestore.collection("member").document(etId.text.toString()).set(map, SetOptions.merge()).addOnSuccessListener {
                                val correct: DialogSheet = DialogSheet(this@MyInfoActivity)
                                    .setColoredNavigationBar(true)
                                    .setCancelable(true)
                                    .setRoundedCorners(true)
                                    .setTitle("변경했어요")
                                    .setMessage("ㅇㅇ")
                                    .setPositiveButton("닫기") {
                                        finish()
                                    }
                                    .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cardBg, theme))
                                correct.show()
                            }
                        } else {
                            val wrong: DialogSheet = DialogSheet(this@MyInfoActivity)
                                .setColoredNavigationBar(true)
                                .setCancelable(true)
                                .setRoundedCorners(true)
                                .setTitle("암호가 틀렸어요")
                                .setMessage("다시 시도해주세요")
                                .setPositiveButton("재시도") {
                                    save.performClick()
                                }
                                .setNegativeButton("취소") { }
                                .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cardBg, theme))
                            wrong.show()
                        }
                        progress.dismiss()
                    }
                }

            }
            .setNegativeButton("취소") { }

            confirmation.show()

        }


    }

}
