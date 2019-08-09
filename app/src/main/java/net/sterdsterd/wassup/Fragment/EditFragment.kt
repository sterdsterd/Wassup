package net.sterdsterd.wassup.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_edit.*
import net.sterdsterd.wassup.Activity.MainActivity
import net.sterdsterd.wassup.Adapter.EditAdapter
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData


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
        editList?.adapter = EditAdapter(activity as MainActivity, SharedData.studentList)
        editList?.adapter?.notifyDataSetChanged()
        /*
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
                    (activity as MainActivity).startActivityForResult(intent, 1)
                }
            }

        }*/

    }

}