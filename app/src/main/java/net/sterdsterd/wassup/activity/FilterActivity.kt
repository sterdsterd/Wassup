package net.sterdsterd.wassup.activity

import android.content.Context
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager

import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.activity_filter.collapsingToolBar
import net.sterdsterd.wassup.*
import net.sterdsterd.wassup.adapter.FilterAdapter

class FilterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val dark = pref!!.getBoolean("dark", true)
        delegate.localNightMode = if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        setContentView(R.layout.activity_filter)
        if (!dark) appBarLayout.outlineProvider = null

        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

        filterList.layoutManager = LinearLayoutManager(this)
        filterList.adapter = FilterAdapter(this, SharedData.studentList)
        filterList.adapter?.notifyDataSetChanged()

        select.setOnClickListener {
            finish()
        }
    }

}
