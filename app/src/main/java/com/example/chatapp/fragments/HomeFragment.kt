package com.example.chatapp.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.chatapp.App
import com.example.chatapp.R
import com.example.chatapp.adapters.RecentConversationsAdapter
import com.example.chatapp.databinding.FragmentHomeBinding
import com.example.chatapp.model.ChatMessage
import com.example.chatapp.model.User
import com.example.chatapp.util.Constants
import com.example.chatapp.util.showToast
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging


class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding
    private var list: ArrayList<ChatMessage> = arrayListOf()
    private val database = FirebaseFirestore.getInstance()
    private var adapter = RecentConversationsAdapter(this::click, list)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.conversationsRecyclerView.adapter = adapter
        loadUserDetails()
        getToken()
        setListeners()
        listenConversations()
    }


    private fun loadUserDetails() {
        binding.textName.text = App.prefs.getString(Constants.KEY_NAME)
        val bytes: ByteArray =
            Base64.decode(App.prefs.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }


    private fun updateToken(token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTIONS_USERS).document(
            App.prefs.getString(Constants.KEY_USER_ID)
        )
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
            .addOnFailureListener { _ -> showToast("Unable to update token") }
    }

    private fun listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, App.prefs.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_RECEIVED_ID, App.prefs.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }


    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    }

    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            for (documentChange: DocumentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {

                    val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                    val receiverId = documentChange.document.getString(Constants.KEY_RECEIVED_ID)
                    var chatMessage = ChatMessage()

                    if (App.prefs.getString(Constants.KEY_USER_ID) == senderId) {
                        chatMessage = ChatMessage(
                            senderId = senderId.toString(),
                            receivedId = receiverId.toString(),
                            conversionId = documentChange.document.getString(
                                Constants.KEY_RECEIVED_ID
                            ),
                            conversionName = documentChange.document.getString(Constants.KEY_RECEIVER_NAME),
                            conversionImage = documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE)

                        )
                    } else {
                        chatMessage = ChatMessage(
                            senderId = senderId.toString(),
                            receivedId = receiverId.toString(),
                            conversionId = documentChange.document.getString(
                                Constants.KEY_SENDER_ID
                            ),
                            conversionName = documentChange.document.getString(Constants.KEY_SENDER_NAME),
                            conversionImage = documentChange.document.getString(Constants.KEY_SENDER_IMAGE)

                        )
                    }
                    chatMessage.message =
                        documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                    chatMessage.dateObject =
                        documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                    if (!list.any { it.conversionId == chatMessage.conversionId }) {
                        list.add(chatMessage)
                    }

                } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                    for (items in list) {
                        val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                        val receiverId =
                            documentChange.document.getString(Constants.KEY_RECEIVED_ID)
                        if (items.senderId.equals(senderId) && items.receivedId.equals(receiverId)) {
                            items.message =
                                documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                            items.dateObject =
                                documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                            break
                        }
                    }
                }
            }
            list.sortWith(compareBy { it.dateObject })
            adapter.notifyDataSetChanged()
            binding.conversationsRecyclerView.smoothScrollToPosition(0)
            binding.conversationsRecyclerView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }


    private fun setListeners() {
        binding.imageSignOut.setOnClickListener {
            signOut()
        }
        binding.fabNewChat.setOnClickListener {
            findNavController().navigate(R.id.usersFragment)
        }
    }

    private fun click(user: User) {
        val bundle = Bundle()
        bundle.putSerializable(Constants.KEY_USER, user)
        findNavController().navigate(R.id.chatFragment, bundle)
    }


    private fun signOut() {
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTIONS_USERS).document(
            App.prefs.getString(Constants.KEY_USER_ID)
        )
        val updates = HashMap<String, Any>()
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete())
        documentReference.update(updates)
            .addOnSuccessListener { unused ->
                App.prefs.clear()
                findNavController().navigate(R.id.signInFragment)
            }
            .addOnFailureListener { e -> showToast("Unable to sign out") }


    }
}