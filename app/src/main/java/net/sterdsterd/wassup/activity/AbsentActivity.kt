package net.sterdsterd.wassup.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_absent.*
import kotlinx.android.synthetic.main.activity_absent.collapsingToolBar
import kotlinx.android.synthetic.main.activity_absent.toolBar
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.adapter.AbsentAdapter



class AbsentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val dark = pref!!.getBoolean("dark", true)
        delegate.localNightMode = if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        setContentView(R.layout.activity_absent)
        setSupportActionBar(toolBar)
        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

        val bus = SharedData.absentList.filter { it.value == "WARNING" }.size
        val abs = SharedData.absentList.filter { it.value == "ABSENT" }.size

        description.text = String.format(resources.getString(R.string.desc_abs), bus, abs)
        absList.layoutManager = LinearLayoutManager(this)
        absList.adapter = AbsentAdapter(this, SharedData.absentList.toList().toMutableList())
        absList.adapter?.notifyDataSetChanged()
    }

}
