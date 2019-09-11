package net.sterdsterd.wassup.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_edit.*
import net.sterdsterd.wassup.activity.MainActivity
import net.sterdsterd.wassup.adapter.EditAdapter
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.activity.EditActivity


class EditFragment : Fragment() {

    companion object {
        fun newInstance(): EditFragment {
            return EditFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    lateinit var classStr: String
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = this.activity?.getSharedPreferences("User", Context.MODE_PRIVATE)
        classStr = pref!!.getString("class", "Null")

        editList.layoutManager = LinearLayoutManager(view.context)
        editList.adapter = EditAdapter(activity as MainActivity, SharedData.studentList)
        editList.adapter?.notifyDataSetChanged()
    }

    fun add() {
        val firestore = FirebaseFirestore.getInstance()
        val user = mapOf(
            "name" to "",
            "studentPhone" to "",
            "parentPhone" to "",
            "hash" to "1",
            "type" to "shuttle"
        )
        firestore.collection("class").document(classStr).collection("memberList").add(user).addOnCompleteListener { t ->
            if(t.isComplete) {
                val intent = Intent(context, EditActivity::class.java)
                intent.putExtra("id", t.result?.id)
                (activity as MainActivity).startActivityForResult(intent, 1)
            }
        }
    }

}