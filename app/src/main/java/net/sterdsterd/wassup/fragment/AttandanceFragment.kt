package net.sterdsterd.wassup.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_attandance.*
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.activity.DetailActivity

class AttandanceFragment : Fragment() {

    companion object {
        fun newInstance(): AttandanceFragment {
            return AttandanceFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_attandance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardBoard.setOnClickListener {
            startActivity(Intent(activity, DetailActivity::class.java))
        }
    }

}