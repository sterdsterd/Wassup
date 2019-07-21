package net.sterdsterd.wassup.Activity

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import net.sterdsterd.wassup.Fragment.AttandanceFragment
import net.sterdsterd.wassup.Fragment.EditFragment
import net.sterdsterd.wassup.Fragment.FindFragment
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R

class MainActivity : AppCompatActivity() {

    private lateinit var textMessage: TextView
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, AttandanceFragment())
                    .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, EditFragment())
                    .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, FindFragment())
                    .commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    val s = mutableListOf<MemberData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, AttandanceFragment())
            .commit()


        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().subscribeToTopic("all")

        //startActivity(Intent(this, LoginActivity::class.java))

        val firestore = FirebaseFirestore.getInstance()
        val classStr = "하늘반"

        firestore.collection("class").document(classStr).collection("memberList").get().addOnCompleteListener { t ->
            if(t.isComplete) {
                var v = t.getResult()?.documents?.size as Int
                for (i in 0..(v - 1))
                    s.add(MemberData(t.getResult()?.documents?.get(i)?.id!!))
            }
        }

    }
}
