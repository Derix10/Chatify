package com.example.chatapp.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.chatapp.App
import com.example.chatapp.util.Constants
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

open class BaseFragment : Fragment() {

    private var documentReference: DocumentReference? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataBase = FirebaseFirestore.getInstance()
        documentReference = dataBase.collection(Constants.KEY_COLLECTIONS_USERS)
            .document(App.prefs.getString(Constants.KEY_USER_ID))
    }

    override fun onPause() {
        super.onPause()
        documentReference?.update(Constants.KEY_AVAILABILITY, 0)
    }

    override fun onResume() {
        super.onResume()
        documentReference?.update(Constants.KEY_AVAILABILITY, 1)
    }
}