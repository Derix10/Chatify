package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.chatapp.databinding.ItemContainerUserBinding
import com.example.chatapp.model.User

class UsersAdapter(val click: (user: User) -> Unit, val users: List<User>) :
    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {
    class UsersViewHolder(val binding: ItemContainerUserBinding) : ViewHolder(binding.root) {
        fun onBind(user: User) {
            binding.textEmail.text = user.email
            binding.textName.text = user.name
            binding.imageProfile.setImageBitmap(getUserImage(user.image))
        }

        private fun getUserImage(encodeImage: String): Bitmap {
            val bytes: ByteArray =
                android.util.Base64.decode(encodeImage, android.util.Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val binding =
            ItemContainerUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsersViewHolder(binding)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.onBind(users[position])
        holder.binding.root.setOnClickListener {
            click(users[position])
        }
    }


}