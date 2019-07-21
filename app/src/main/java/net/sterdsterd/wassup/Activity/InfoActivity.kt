package net.sterdsterd.wassup.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;

import net.sterdsterd.wassup.R

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

    }

    fun back() {
        super.finish()
    }

}
