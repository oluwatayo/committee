package com.james.project.committee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.james.project.committee.adapters.CommitteePagerAdapter
import com.james.project.committee.databinding.FragmentCommitteeDetailsBinding

class CommitteeDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCommitteeDetailsBinding.inflate(inflater, container, false)
        val frags =
            arrayOf(CommitteeUsersFragment.newInstance(), CommitteeFilesFragment.newInstance())
        val titles = arrayOf("Members", "Files")
        binding.viewpager.adapter = CommitteePagerAdapter(requireActivity(), frags)
        TabLayoutMediator(binding.tabs, binding.viewpager) { tab, pos ->
            tab.text = titles[pos]
        }.attach()
        return binding.root
    }
}