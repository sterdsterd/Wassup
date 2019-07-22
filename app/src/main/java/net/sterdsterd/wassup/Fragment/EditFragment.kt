package net.sterdsterd.wassup.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_edit.*
import net.sterdsterd.wassup.Activity.EditActivity
import net.sterdsterd.wassup.Activity.MainActivity
import net.sterdsterd.wassup.Adapter.EditAdapter
import net.sterdsterd.wassup.R

class EditFragment : Fragment() {

    companion object {
        fun newInstance(): EditFragment {
            return EditFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val classStr = "하늘반"
        editList?.layoutManager = LinearLayoutManager(activity)
        editList?.adapter = EditAdapter((activity as MainActivity).s)
        add.setOnClickListener { v ->
            val firestore = FirebaseFirestore.getInstance()
            val user = mapOf(
                "name" to "",
                "studentPhone" to "",
                "parentPhone" to ""
            )
            firestore.collection("class").document(classStr).collection("memberList").add(user).addOnCompleteListener { t ->
                if(t.isComplete) {
                    val intent = Intent(v.context, EditActivity::class.java)
                    intent.putExtra("id", t.result?.id)
                    v.context.startActivity(intent)
                }
            }

        }


    }

}