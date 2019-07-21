package net.sterdsterd.wassup.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_edit.*
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
        editList?.layoutManager = GridLayoutManager(activity, 1)
        editList?.adapter = EditAdapter(MutableList(9) { 1 })


    }

}