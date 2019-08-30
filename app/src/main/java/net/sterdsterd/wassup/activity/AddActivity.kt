package net.sterdsterd.wassup.activity

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat
import net.sterdsterd.wassup.R

import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.activity_add.collapsingToolBar
import kotlinx.android.synthetic.main.activity_detail.*

class AddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        collapsingToolBar.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))
        collapsingToolBar.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.spoqa_bold))

    }

}
