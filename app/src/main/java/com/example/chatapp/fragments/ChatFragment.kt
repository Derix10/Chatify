package com.example.chatapp.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.chatapp.App
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.model.ChatMessage
import com.example.chatapp.model.User
import com.example.chatapp.util.Constants
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

class ChatFragment : BaseFragment() {
    private lateinit var binding: FragmentChatBinding
    private var user: User? = null
    private var list: ArrayList<ChatMessage> = arrayListOf()
    private var adapter: ChatAdapter? = null
    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var conversionId: String? = null
    private var isReceiverAvailable: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadReceiverDetails()
        init()
        setListeners()
        listenMessages()

    }

    private fun loadReceiverDetails() {
        user = requireArguments().getSerializable(Constants.KEY_USER) as User
        binding.textName.text = user!!.name
    }

    private fun init() {
        adapter = ChatAdapter(
            list,
            getBitmapFromEncodeString(user!!.image),
            App.prefs.getString(Constants.KEY_USER_ID)
        )
        database = FirebaseFirestore.getInstance()
        binding.chatRecyclerView.adapter = adapter
    }

    private fun sendMessage() {
        val message: HashMap<String, Any> = HashMap()
        message[Constants.KEY_SENDER_ID] = App.prefs.getString(Constants.KEY_USER_ID)
        message[Constants.KEY_RECEIVED_ID] = user!!.id
        message[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
        message[Constants.KEY_TIMESTAMP] = Date()
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message)
        if (conversionId != null) {
            updateConversion(binding.inputMessage.text.toString())
        } else {
            val conversion = HashMap<String, Any>()
            conversion.put(Constants.KEY_SENDER_ID, App.prefs.getString(Constants.KEY_USER_ID))
            conversion.put(Constants.KEY_SENDER_NAME, App.prefs.getString(Constants.KEY_NAME))
            conversion.put(Constants.KEY_SENDER_IMAGE, App.prefs.getString(Constants.KEY_IMAGE))
            conversion.put(Constants.KEY_RECEIVED_ID, user!!.id)
            conversion.put(Constants.KEY_RECEIVER_NAME, user!!.name)
            conversion.put(Constants.KEY_RECEIVER_IMAGE, user!!.image)
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.text.toString())
            conversion.put(Constants.KEY_TIMESTAMP, Date())
            addConversion(conversion)
        }
        binding.inputMessage.text = null
    }

    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            val count = list.size
            for (documentChange: DocumentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val chatModel = ChatMessage(
                        senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                            .toString(),
                        receivedId = documentChange.document.getString(Constants.KEY_RECEIVED_ID)
                            .toString(),
                        message = documentChange.document.getString(Constants.KEY_MESSAGE)
                            .toString(),
                        dateTime = getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!),
                        dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                    )
                    list.add(chatModel)
                }
            }
            list.sortWith(compareBy { it.dateObject })
            if (count == 0) {
                adapter?.notifyDataSetChanged()
            } else {
                adapter?.notifyItemRangeChanged(list.size, list.size)
                binding.chatRecyclerView.smoothScrollToPosition(list.size - 1)
            }
            binding.chatRecyclerView.visibility = View.VISIBLE


        }
        binding.progressBar.visibility = View.GONE
        if (conversionId == null) {
            checkConversion()
        }
    }

    private fun listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTIONS_USERS).document(
            user!!.id,
        ).addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    val availability = Objects.requireNonNull(
                        value.getLong(Constants.KEY_AVAILABILITY)
                    )?.toInt()
                    isReceiverAvailable = availability == 1
                }
            }

            if (isReceiverAvailable) {
                binding.textOnline.text = ":   В сети"
                binding.textOnline.setTextColor(Color.GREEN)
            } else {
                binding.textOnline.text = ":   Не в сети"
                binding.textOnline.setTextColor(Color.RED)
            }
        }
    }

    private fun listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, App.prefs.getString(Constants.KEY_USER_ID))
            .whereEqualTo(Constants.KEY_RECEIVED_ID, user?.id)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, user?.id)
            .whereEqualTo(Constants.KEY_RECEIVED_ID, App.prefs.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private fun getBitmapFromEncodeString(encodeImage: String): Bitmap {
        val bytes: ByteArray = Base64.decode(encodeImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { findNavController().navigateUp() }
        binding.layoutSend.setOnClickListener { sendMessage() }
    }

    private fun getReadableDateTime(date: Date): String {

        return SimpleDateFormat("MMMM dd - hh:mm a", Locale.getDefault()).format(date)

    }

    private fun addConversion(conversion: HashMap<String, Any>) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener { conversionId = it.id }

    }

    private fun updateConversion(message: String) {
        val docReference =
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document(conversionId.toString())
        docReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, Date())


    }

    private fun checkConversion() {
        if (list.size != 0) {
            checkForConversionRemotely(
                App.prefs.getString(Constants.KEY_USER_ID),
                user!!.id
            )
            checkForConversionRemotely(
                user!!.id,
                App.prefs.getString(Constants.KEY_USER_ID)
            )
        }
    }

    private fun checkForConversionRemotely(senderId: String, receiverId: String) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVED_ID, receiverId)
            .get()
            .addOnCompleteListener(conversionOnCompleteListener)
    }

    private val conversionOnCompleteListener = OnCompleteListener<QuerySnapshot> { task ->
        if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
            val docSnapshot = task.result.documents[0]
            conversionId = docSnapshot.id
        }
    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }
}