package com.james.project.committee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.james.project.committee.databinding.FragmentHomeBinding
import com.james.project.committee.viewmodels.CommitteeViewModel
import com.james.project.committee.viewmodels.HomeViewModel

class HomeFragment : Fragment() {

    private val newCommittee = "new_committer"
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val viewModel: HomeViewModel by activityViewModels()
        val committeeViewModel: CommitteeViewModel by activityViewModels()
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.homeRv.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        viewModel.addNewCommittee.observe(viewLifecycleOwner, Observer {
            if (it) {
                val fragment = childFragmentManager.findFragmentByTag(newCommittee)
                if (fragment != null)
                    childFragmentManager.beginTransaction().remove(fragment)
                val newCommitteeBottomSheetFragment = NewCommitteeBottomSheetFragment.newInstance()
                newCommitteeBottomSheetFragment.isCancelable = false
                newCommitteeBottomSheetFragment.show(childFragmentManager, newCommittee)
                viewModel.endAddNewCommittee()
            }
        })
        viewModel.homeAdapter.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.homeRv.adapter = it
            }
        })
        viewModel.selectedCommittee.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                committeeViewModel.setSelectedCommittee(it)
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToCommitteeMessageFragment(
                        it
                    )
                )
                viewModel.endNavigation()
            }
        })
        return binding.root
    }
}