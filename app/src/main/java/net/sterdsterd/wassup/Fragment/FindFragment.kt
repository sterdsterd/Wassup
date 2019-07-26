package net.sterdsterd.wassup.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_find.*
import androidx.recyclerview.widget.GridLayoutManager
import net.sterdsterd.wassup.Activity.MainActivity
import net.sterdsterd.wassup.Adapter.FindAdapter
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.Activity.RestrictionActivity


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
        findList?.layoutManager = GridLayoutManager(activity, 3)
        findList?.adapter = FindAdapter(activity as MainActivity, (activity as MainActivity).s)
        findList?.adapter?.notifyDataSetChanged()

        restriction.setOnClickListener {
            findList?.adapter?.notifyDataSetChanged()
            //startActivity(Intent(activity, RestrictionActivity::class.java))
        }

    }

    fun update() {
        findList?.adapter?.notifyDataSetChanged()
    }
}