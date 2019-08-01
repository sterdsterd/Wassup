package net.sterdsterd.wassup.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_find.*
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import net.sterdsterd.wassup.Activity.MainActivity
import net.sterdsterd.wassup.Adapter.FindAdapter
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.Activity.RestrictionActivity
import net.sterdsterd.wassup.SharedData


class FindFragment : Fragment() {

    companion object {
        fun newInstance(): FindFragment {
            return FindFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val count = ((context?.resources?.displayMetrics!!.widthPixels / context?.resources?.displayMetrics!!.density) - 54) / 92
        findList?.layoutManager = GridLayoutManager(activity, count.toInt())
        findList?.adapter = FindAdapter(activity as MainActivity, SharedData.studentList)
        findList?.adapter?.notifyDataSetChanged()

        restriction.setOnClickListener {
            startActivity(Intent(activity, RestrictionActivity::class.java))
        }

    }

    fun update() {
        findList?.adapter?.notifyDataSetChanged()
    }
}