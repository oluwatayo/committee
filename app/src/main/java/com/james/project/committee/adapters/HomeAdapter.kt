package com.james.project.committee.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.james.project.committee.databinding.CommitteeItemBinding
import com.james.project.committee.models.Committee

class HomeAdapter(options: FirestoreRecyclerOptions<Committee>) :
    FirestoreRecyclerAdapter<Committee, HomeViewHolder>(options) {
    private var _selectedItem: MutableLiveData<Committee> = MutableLiveData()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder.from(parent, _selectedItem)
    }

    val selectedItem: LiveData<Committee>
        get() = _selectedItem

    fun resetSelected() {
        _selectedItem.value = null
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int, model: Committee) {
        holder.bind(model)
    }
}

class HomeViewHolder(
    private val binding: CommitteeItemBinding,
    private val selectedItem: MutableLiveData<Committee>
) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(
            parent: ViewGroup,
            selectedItem: MutableLiveData<Committee>
        ): HomeViewHolder {
            return HomeViewHolder(
                CommitteeItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                selectedItem
            )
        }
    }

    fun bind(committee: Committee) {
        binding.committee = committee
        itemView.setOnClickListener {
            selectedItem.value = committee
        }
    }
}