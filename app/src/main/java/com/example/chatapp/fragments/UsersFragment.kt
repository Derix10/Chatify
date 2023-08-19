package com.example.chatapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.chatapp.App
import com.example.chatapp.R
import com.example.chatapp.adapters.UsersAdapter
import com.example.chatapp.databinding.FragmentUsersBinding
import com.example.chatapp.model.User
import com.example.chatapp.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class UsersFragment : BaseFragment() {
    private lateinit var binding: FragmentUsersBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUsers()
        setListeners()
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun getUsers() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTIONS_USERS)
            .get()
            .addOnCompleteListener { task ->
                loading(false)
                val currentUserId = App.prefs.getString(Constants.KEY_USER_ID)
                if (task.isSuccessful && task.result != null) {
                    val users = ArrayList<User>()
                    for (queryDocumentSnapshot: QueryDocumentSnapshot in task.result) {
                        if (currentUserId == queryDocumentSnapshot.id) {
                            continue
                        }
                        val user = User(
                            id = queryDocumentSnapshot.id,
                            name = queryDocumentSnapshot.getString(Constants.KEY_NAME).toString(),
                            image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE).toString(),
                            token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN)
                                .toString(),
                            email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL).toString(),
                        )
                        users.add(user)

                        if (users.size > 0) {
                            Log.d("ololo", "List: $users")
                            val adapter = UsersAdapter(this::click, users)
                            binding.usersRecyclerView.adapter = adapter
                            binding.usersRecyclerView.visibility = View.VISIBLE
                        } else {
                            showErrorMessage()
                        }
                    }
                } else {
                    showErrorMessage()
                }

            }
    }

    private fun showErrorMessage() {
        binding.textErrorMessage.text = String.format("%s", "No user available")
    }

    private fun click(user: User) {
        val bundle = Bundle()
        bundle.putSerializable(Constants.KEY_USER, user)
        findNavController().navigate(R.id.chatFragment, bundle)
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}