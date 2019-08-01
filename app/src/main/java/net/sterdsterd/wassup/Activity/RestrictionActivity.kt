package net.sterdsterd.wassup.Activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_restriction.*
import net.sterdsterd.wassup.R
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.SeekParams




class RestrictionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restriction)


        val pref = getSharedPreferences("Restriction", Context.MODE_PRIVATE)
        val editor = pref.edit()

        when (pref.getString("checked", "None")) {
            "in" -> {
                radioIn.isChecked = true
                seekBar.isEnabled = false
            }
            "out" -> radioOut.isChecked = true
            "None" -> radioIn.isChecked = true
        }

        seekBar.setProgress(pref.getInt("distance", 0).toFloat())

        seekBar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                editor.putInt("distance", seekBar.progress)
            }

            override fun onSeeking(seekParams: SeekParams) { }
            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) { }
        }

        save.setOnClickListener {
            super.finish()
            editor.apply()
        }

        radioIn.setOnClickListener {
            radioOut.isChecked = false
            editor.putString("checked", "in")
            seekBar.isEnabled = false
        }

        radioOut.setOnClickListener {
            radioIn.isChecked = false
            editor.putString("checked", "out")
            seekBar.isEnabled = true
        }

    }

}
