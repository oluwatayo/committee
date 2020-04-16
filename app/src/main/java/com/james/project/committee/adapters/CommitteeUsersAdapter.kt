package com.james.project.committee.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.james.project.committee.databinding.CommitteeUserItemBinding
import com.james.project.committee.models.Committee
import com.james.project.committee.models.CommitteeUser
import com.james.project.committee.models.Users

class CommitteeUsersAdapter(
    options: FirestoreRecyclerOptions<CommitteeUser>,
    val committee: Committee
) :
    FirestoreRecyclerAdapter<CommitteeUser, CommitteeUserViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommitteeUserViewHolder {
        return CommitteeUserViewHolder.from(parent, committee)
    }

    override fun onBindViewHolder(
        holder: CommitteeUserViewHolder,
        position: Int,
        model: CommitteeUser
    ) {
        holder.bind(model)
    }
}

class CommitteeUserViewHolder(val committee: Committee, val binding: CommitteeUserItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup, committee: Committee): CommitteeUserViewHolder {
            return CommitteeUserViewHolder(
                committee,
                CommitteeUserItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        }
    }

    fun bind(committeeUser: CommitteeUser) {
        binding.removeUser.visibility =
            when {
                committeeUser.users?.id == committee.createdBy -> View.GONE
                committee.createdBy!! == Users.FIREBASE_UID -> View.VISIBLE
                else -> View.GONE
            }
        binding.user = committeeUser
    }
}