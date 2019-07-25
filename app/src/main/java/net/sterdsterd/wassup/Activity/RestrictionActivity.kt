package net.sterdsterd.wassup.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_restriction.*
import net.sterdsterd.wassup.R

class RestrictionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restriction)

        save.setOnClickListener {
            super.finish()
        }

        radioIn.setOnClickListener {
            radioOut.isChecked = false
        }

        radioOut.setOnClickListener {
            radioIn.isChecked = false
        }

    }

}
