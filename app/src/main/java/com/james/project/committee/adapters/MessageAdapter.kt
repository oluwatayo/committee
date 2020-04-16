package com.james.project.committee.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.james.project.committee.databinding.MessageItemMeBinding
import com.james.project.committee.databinding.MessageItemNotificationBinding
import com.james.project.committee.databinding.MessageItemOthersBinding
import com.james.project.committee.models.Message
import com.james.project.committee.models.MessageType

class MessageAdapter(options: FirestoreRecyclerOptions<Message>) :
    FirestoreRecyclerAdapter<Message, MessageViewHolder>(options) {

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: Message) {
        holder.bind(model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return when (viewType) {
            MessageType.MESSAGE_ME.viewType -> MessageMeViewHolder.from(parent)
            MessageType.MESSAGE_OTHERS.viewType -> MessageOthersViewHolder.from(parent)
            MessageType.MESSAGE_NOTIFICATIONS.viewType -> MessageNotificationViewHolder.from(parent)
            else -> MessageNotificationViewHolder.from(parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).messageType.viewType
    }

}

open class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    open fun bind(model: Message) {

    }
}

class MessageMeViewHolder(var binding: MessageItemMeBinding) : MessageViewHolder(binding.root) {

    override fun bind(model: Message) {
        binding.message = model
    }

    companion object {
        fun from(parent: ViewGroup): MessageViewHolder {
            return MessageMeViewHolder(
                MessageItemMeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }
}

class MessageOthersViewHolder(var binding: MessageItemOthersBinding) :
    MessageViewHolder(binding.root) {
    override fun bind(model: Message) {
        binding.message = model
    }

    companion object {
        fun from(parent: ViewGroup): MessageViewHolder {
            return MessageOthersViewHolder(
                MessageItemOthersBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }
}

class MessageNotificationViewHolder(var binding: MessageItemNotificationBinding) :
    MessageViewHolder(binding.root) {
    override fun bind(model: Message) {
        binding.message = model
    }

    companion object {
        fun from(parent: ViewGroup): MessageViewHolder {
            return MessageNotificationViewHolder(
                MessageItemNotificationBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }
}