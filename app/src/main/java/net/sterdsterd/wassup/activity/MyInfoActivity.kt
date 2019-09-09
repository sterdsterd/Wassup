package net.sterdsterd.wassup.activity

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_my_info.*
import kotlinx.android.synthetic.main.activity_my_info.collapsingToolBar
import kotlinx.android.synthetic.main.activity_my_info.etClass
import kotlinx.android.synthetic.main.activity_my_info.etId
import kotlinx.android.synthetic.main.activity_my_info.etName
import kotlinx.android.synthetic.main.activity_my_info.etNum
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
            val name = etName.text
            val cl = etClass.text
            val num = etNum.text
        }


    }

}
