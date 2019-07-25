package net.sterdsterd.wassup.Activity

import android.content.Intent
import android.graphics.Color
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
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import android.widget.LinearLayout
import android.view.WindowManager
import android.graphics.Color.parseColor
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.fragment_edit.*


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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode){
            1 -> update(true)
            //2 -> update(false)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

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

        startActivity(Intent(this, LoginActivity::class.java))

        update(false)

    }

    fun update(con: Boolean) {
        s.clear()
        val firestore = FirebaseFirestore.getInstance()
        val classStr = "하늘반"
        val progress = Progress(this)
        progress.setBackgroundColor(Color.parseColor("#323445"))
            .setMessage("Loading")
            .show()
        firestore.collection("class").document(classStr).collection("memberList").orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener { t ->
            if(t.isComplete) {
                val v = t.result?.documents?.size as Int
                for (i in 0..(v - 1))
                    s.add(MemberData(t.result?.documents?.get(i)?.id!!, t.result?.documents?.get(i)?.getString("name")!!))
                progress.dismiss()
                if(con) supportFragmentManager.beginTransaction().replace(R.id.fragment, EditFragment()).commit()
            }
        }
    }

}
