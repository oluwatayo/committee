package com.james.project.committee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.james.project.committee.databinding.LayoutNewCommitteeBinding
import com.james.project.committee.viewmodels.HomeViewModel

class NewCommitteeBottomSheetFragment : BaseBottomSheetDialogFragment() {

    companion object {
        fun newInstance(): NewCommitteeBottomSheetFragment {
            return NewCommitteeBottomSheetFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = LayoutNewCommitteeBinding.inflate(inflater, container, false)
        val viewModel: HomeViewModel by activityViewModels()
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        binding.closeDialog.setOnClickListener {
            dialog?.dismiss()
        }
        viewModel.createDone.observe(viewLifecycleOwner, Observer {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    "Setting up your new committee, please wait!!!",
                    Toast.LENGTH_LONG
                ).show()
                binding.closeDialog.performClick()
                viewModel.resetCreate()
            }
        })
        return binding.root
    }
}