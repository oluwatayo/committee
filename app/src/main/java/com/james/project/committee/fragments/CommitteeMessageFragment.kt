package com.james.project.committee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.james.project.committee.databinding.FragmentCommitteeMessageBinding
import com.james.project.committee.viewmodels.CommitteeViewModel


class CommitteeMessageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCommitteeMessageBinding.inflate(inflater, container, false)
        val committeeViewModel: CommitteeViewModel by activityViewModels()
        binding.viewmodel = committeeViewModel
        binding.lifecycleOwner = this
        val manager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        manager.isItemPrefetchEnabled = true
        binding.messageRv.layoutManager = manager
        committeeViewModel.messageAdapterObserver.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.messageRv.adapter = it
                binding.messageRv.smoothScrollToPosition(0)
                it.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        super.onItemRangeInserted(positionStart, itemCount)
                        binding.messageRv.smoothScrollToPosition(0)
                    }
                })
            }
        })
        committeeViewModel.showCommitteeDetails.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigate(CommitteeMessageFragmentDirections.actionCommitteeMessageFragmentToCommitteeDetailsFragment())
                committeeViewModel.showCommitteeDetails(false)
            }
        })
        return binding.root
    }
}