package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.chatapp.databinding.ItemContainerRecentConversationBinding
import com.example.chatapp.model.ChatMessage
import com.example.chatapp.model.User

class RecentConversationsAdapter(val click: (user: User) -> Unit, val list: List<ChatMessage>) :
    RecyclerView.Adapter<RecentConversationsAdapter.ConversationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemContainerRecentConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    private fun getConversionImage(encodedImage: String): Bitmap {
        val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    inner class ConversationViewHolder(private val binding: ItemContainerRecentConversationBinding) :
        ViewHolder(binding.root) {
        fun onBind(chatMessage: ChatMessage) {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage.toString()))
            binding.textName.text = chatMessage.conversionName
            binding.textRecentMessage.text = chatMessage.message
            binding.root.setOnClickListener {
                val user = User(
                    id = chatMessage.conversionId.toString(),
                    name = chatMessage.conversionName.toString(),
                    image = chatMessage.conversionImage.toString()
                )
                click(user)
            }

        }
    }


}