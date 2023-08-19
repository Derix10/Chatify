package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.chatapp.databinding.ItemContainerReceivedMessageBinding
import com.example.chatapp.databinding.ItemContainerSentMessageBinding
import com.example.chatapp.model.ChatMessage

class ChatAdapter(
    val list: List<ChatMessage>,
    val receiverProfileImage: Bitmap,
    val senderId: String
) : RecyclerView.Adapter<ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }


    override fun getItemViewType(position: Int): Int {
        if (list[position].senderId.equals(senderId)) {
            return VIEW_TYPE_SENT
        } else {
            return VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemContainerSentMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SentMessageViewHolder(binding)
        } else {
            val binding = ItemContainerReceivedMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            val sendHolder = holder as SentMessageViewHolder
            sendHolder.onBind(list[position])
        } else {
            val receivedHolder = holder as ReceivedMessageViewHolder
            receivedHolder.onBind(list[position])
        }
    }

    override fun getItemCount(): Int = list.size


    class SentMessageViewHolder(val binding: ItemContainerSentMessageBinding) :
        ViewHolder(binding.root) {
        fun onBind(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
        }
    }

    inner class ReceivedMessageViewHolder(val binding: ItemContainerReceivedMessageBinding) :
        ViewHolder(binding.root) {
        fun onBind(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
            binding.imageProfile.setImageBitmap(receiverProfileImage)
        }
    }

}