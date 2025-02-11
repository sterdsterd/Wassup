package net.sterdsterd.wassup.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.GridLayout
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_icon.*
import kotlinx.android.synthetic.main.activity_icon.collapsingToolBar
import net.sterdsterd.wassup.IconSet
import net.sterdsterd.wassup.adapter.IconAdapter

class IconActivity : AppCompatActivity() {

    var mIntent = Intent()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val dark = pref!!.getBoolean("dark", true)
        delegate.localNightMode = if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        setContentView(R.layout.activity_icon)
        if (!dark) appBarLayout.outlineProvider = null

        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

        val iconList = mutableListOf(
            IconSet("flower", R.drawable.ic_flower),
            IconSet("moon", R.drawable.ic_moon),
            IconSet("school", R.drawable.ic_school),
            IconSet("basketball", R.drawable.ic_basketball),
            IconSet("football", R.drawable.ic_football),
            IconSet("bday", R.drawable.ic_bday),
            IconSet("swim", R.drawable.ic_swim),
            IconSet("beach", R.drawable.ic_beach),
            IconSet("dining", R.drawable.ic_dining)
        )

        rview.layoutManager = GridLayoutManager(this, 5)
        rview.adapter = IconAdapter(this, iconList)
        rview.adapter?.notifyDataSetChanged()

    }

}
