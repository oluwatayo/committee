package com.james.project.committee.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.james.project.committee.R
import com.james.project.committee.databinding.LayoutComDetBinding
import com.james.project.committee.databinding.LayoutNewUserBinding
import com.james.project.committee.viewmodels.CommitteeViewModel

class CommitteeUsersFragment : Fragment() {

    companion object {
        fun newInstance(): CommitteeUsersFragment {
            return CommitteeUsersFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = LayoutComDetBinding.inflate(inflater, container, false)
        val viewmodel: CommitteeViewModel by activityViewModels()
        binding.fab.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_add_user))
        binding.viewmodel = viewmodel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.rv.layoutManager = GridLayoutManager(context!!, 2)
        viewmodel.showAddNewUserButton.observe(viewLifecycleOwner, Observer {
            binding.fab.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewmodel.committeeUsersAdapter.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.rv.adapter = it
            }
        })
        val dialogBinding = LayoutNewUserBinding.inflate(inflater, null, false)
        dialogBinding.viewmodel = viewmodel
        dialogBinding.lifecycleOwner = viewLifecycleOwner
        val dialog = AlertDialog.Builder(context)
            .setCancelable(false)
            .setView(dialogBinding.root)
            .create()
        binding.fab.setOnClickListener {
            dialog.show()
        }
        dialogBinding.closeDialog.setOnClickListener {
            if (dialog.isShowing)
                dialog.cancel()
        }
        viewmodel.closeCreateUserDialog.observe(viewLifecycleOwner, Observer {
            if (it && dialog.isShowing) {
                dialog.cancel()
            }
        })
        return binding.root
    }
}




